import { readFile, writeFile } from 'node:fs/promises';
import path from 'node:path';

const resultsRoot = path.resolve('benchmark', 'results');
const suitePath = path.join(resultsRoot, 'suite-summary.json');
const suite = JSON.parse(await readFile(suitePath, 'utf8'));
const formalRuns = suite.runs.filter(run => !run.warmup);

async function mergeCsv(fileName, outputName) {
  let header = null;
  const rows = [];
  for (const run of formalRuns) {
    const content = await readFile(path.join(resultsRoot, run.testRunId, fileName), 'utf8');
    const lines = content.replace(/\r\n/g, '\n').trimEnd().split('\n');
    if (header === null) header = lines.shift();
    else if (lines.shift() !== header) throw new Error(`CSV header mismatch in ${run.testRunId}/${fileName}`);
    rows.push(...lines);
  }
  await writeFile(path.join(resultsRoot, outputName), `${header}\n${rows.join('\n')}\n`, 'utf8');
  return rows.length;
}

const orderRows = await mergeCsv('raw-orders.csv', 'formal-raw-orders.csv');
const notificationRows = await mergeCsv('raw-notifications.csv', 'formal-raw-notifications.csv');
console.log(JSON.stringify({
  formalRuns: formalRuns.length,
  orderRows,
  notificationRows,
  orderCsv: path.join(resultsRoot, 'formal-raw-orders.csv'),
  notificationCsv: path.join(resultsRoot, 'formal-raw-notifications.csv')
}, null, 2));
