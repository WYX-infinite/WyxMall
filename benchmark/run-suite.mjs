import { spawn } from 'node:child_process';
import { readFile, readdir, writeFile } from 'node:fs/promises';
import os from 'node:os';
import path from 'node:path';
import process from 'node:process';

const script = path.resolve('benchmark', 'notification-benchmark.mjs');
const resultsRoot = path.resolve('benchmark', 'results');
const phases = [
  { stage: 'connectivity', requests: 10, concurrency: 1, rounds: 1 },
  { stage: 'warmup', requests: 20, concurrency: 1, rounds: 1, warmup: true },
  { stage: 'sequential-100', requests: 100, concurrency: 1, rounds: 3 },
  { stage: 'concurrent-200', requests: 200, concurrency: 5, rounds: 3 },
  { stage: 'pressure-500', requests: 500, concurrency: 10, rounds: 1, pressure: true },
  { stage: 'multi-client-100', requests: 100, concurrency: 1, clients: 5, rounds: 1 }
];

function median(values) {
  const usable = values.filter(value => value !== null && value !== undefined).sort((a, b) => a - b);
  if (usable.length === 0) return null;
  const middle = Math.floor(usable.length / 2);
  return usable.length % 2 ? usable[middle] : (usable[middle - 1] + usable[middle]) / 2;
}

async function existingResultDirectories() {
  try {
    return new Set(await readdir(resultsRoot));
  } catch {
    return new Set();
  }
}

async function newestNewSummary(before) {
  const after = await readdir(resultsRoot, { withFileTypes: true });
  const created = after.filter(entry => entry.isDirectory() && !before.has(entry.name)).map(entry => entry.name).sort();
  if (created.length !== 1) throw new Error(`Expected one new result directory, found ${created.length}`);
  return JSON.parse(await readFile(path.join(resultsRoot, created[0], 'summary.json'), 'utf8'));
}

function execute(args) {
  return new Promise(resolve => {
    const child = spawn(process.execPath, [script, ...args], { stdio: 'inherit', cwd: process.cwd() });
    child.on('exit', code => resolve(code ?? 1));
  });
}

function isStable(summary) {
  return summary.orderSuccessRate === 1 &&
    summary.notificationSuccessRate === 1 &&
    summary.messageLossCount === 0 &&
    summary.duplicateNotificationCount === 0 &&
    summary.clientDeliveryLossCount === 0 &&
    summary.cleanup.verified === true;
}

function representative(summaries) {
  const field = getter => median(summaries.map(getter));
  return {
    rounds: summaries.length,
    requests: field(item => item.requests),
    orderSuccessRate: field(item => item.orderSuccessRate),
    notificationSuccessRate: field(item => item.notificationSuccessRate),
    messageLossCount: field(item => item.messageLossCount),
    duplicateNotificationCount: field(item => item.duplicateNotificationCount),
    latency: {
      mqConsume: Object.fromEntries(['average', 'p50', 'p95', 'p99', 'max'].map(key => [key, field(item => item.latency.mqConsume[key])])),
      websocketPush: Object.fromEntries(['average', 'p50', 'p95', 'p99', 'max'].map(key => [key, field(item => item.latency.websocketPush[key])])),
      notificationE2E: Object.fromEntries(['average', 'p50', 'p95', 'p99', 'max'].map(key => [key, field(item => item.latency.notificationE2E[key])])),
      businessPerceived: Object.fromEntries(['average', 'p50', 'p95', 'p99', 'max'].map(key => [key, field(item => item.latency.businessPerceived[key])]))
    }
  };
}

function suiteMarkdown(suite) {
  const rows = suite.runs.map(run => `| ${run.stage} | ${run.round} | ${run.requests} | ${run.concurrency} | ${run.clients} | ${(run.orderSuccessRate * 100).toFixed(3)}% | ${run.notificationSuccessRate === null ? 'N/A' : `${(run.notificationSuccessRate * 100).toFixed(3)}%`} | ${run.messageLossCount} | ${run.duplicateNotificationCount} | ${run.latency.notificationE2E.p50 ?? 'N/A'} | ${run.latency.notificationE2E.p95 ?? 'N/A'} | ${run.latency.notificationE2E.p99 ?? 'N/A'} | ${run.latency.notificationE2E.max ?? 'N/A'} | ${run.cleanup.verified} |`).join('\n');
  const reps = Object.entries(suite.representativeByStage).map(([stage, item]) => `| ${stage} | ${item.rounds} | ${(item.orderSuccessRate * 100).toFixed(3)}% | ${item.notificationSuccessRate === null ? 'N/A' : `${(item.notificationSuccessRate * 100).toFixed(3)}%`} | ${item.messageLossCount} | ${item.duplicateNotificationCount} | ${item.latency.notificationE2E.average ?? 'N/A'} | ${item.latency.notificationE2E.p50 ?? 'N/A'} | ${item.latency.notificationE2E.p95 ?? 'N/A'} | ${item.latency.notificationE2E.p99 ?? 'N/A'} | ${item.latency.notificationE2E.max ?? 'N/A'} |`).join('\n');
  return `# Notification benchmark suite\n\n` +
    `Generated: ${suite.generatedAt}\n\n` +
    `## All executed runs\n\n` +
    `| Stage | Round | Requests | Concurrency | Clients | Order success | Notification success | Lost | Duplicates | E2E P50 ms | E2E P95 ms | E2E P99 ms | E2E max ms | Cleanup |\n` +
    `|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---|\n${rows}\n\n` +
    `## Median representative values across rounds\n\n` +
    `| Stage | Rounds | Order success | Notification success | Lost | Duplicates | E2E average ms | E2E P50 ms | E2E P95 ms | E2E P99 ms | E2E max ms |\n` +
    `|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|\n${reps}\n\n` +
    `Local functional/lightweight performance testing only. Online WebSocket delivery only; no offline notification or polling comparison was tested.\n`;
}

const runs = [];
let stoppedReason = null;
for (const phase of phases) {
  if (phase.pressure && os.freemem() < 1.5 * 1024 ** 3) {
    stoppedReason = `Skipped ${phase.stage}: free memory below 1.5 GiB (${Math.round(os.freemem() / 1024 ** 2)} MiB)`;
    continue;
  }
  for (let round = 1; round <= phase.rounds; round += 1) {
    const before = await existingResultDirectories();
    const args = [
      '--requests', String(phase.requests), '--concurrency', String(phase.concurrency),
      '--clients', String(phase.clients ?? 1), '--stage', phase.stage, '--round', String(round)
    ];
    if (phase.warmup) args.push('--warmup');
    const exitCode = await execute(args);
    let summary;
    try {
      summary = await newestNewSummary(before);
      runs.push(summary);
    } catch (error) {
      stoppedReason = `${phase.stage} round ${round} did not produce a readable summary: ${error.message}`;
      break;
    }
    if (exitCode !== 0 || !isStable(summary)) {
      stoppedReason = `${phase.stage} round ${round} was not stable; later load stages were stopped`;
      break;
    }
  }
  if (stoppedReason && !stoppedReason.startsWith('Skipped pressure-500')) break;
}

const reportable = runs.filter(run => !run.warmup);
const grouped = Object.groupBy(reportable, run => run.stage);
const suite = {
  generatedAt: new Date().toISOString(),
  stoppedReason,
  totalRequests: reportable.reduce((sum, run) => sum + run.requests, 0),
  runs,
  representativeByStage: Object.fromEntries(Object.entries(grouped).map(([stage, summaries]) => [stage, representative(summaries)]))
};
await writeFile(path.join(resultsRoot, 'suite-summary.json'), JSON.stringify(suite, null, 2), 'utf8');
await writeFile(path.join(resultsRoot, 'suite-report.md'), suiteMarkdown(suite), 'utf8');
console.log(JSON.stringify({ suiteSummary: path.join(resultsRoot, 'suite-summary.json'), stoppedReason }, null, 2));
if (stoppedReason && !stoppedReason.startsWith('Skipped pressure-500')) process.exitCode = 2;
