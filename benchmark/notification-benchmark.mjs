import { mkdir, writeFile } from 'node:fs/promises';
import path from 'node:path';
import process from 'node:process';

const defaults = {
  requests: 10,
  concurrency: 1,
  clients: 1,
  quantity: 1,
  productId: null,
  orderBaseUrl: 'http://localhost:8082',
  productBaseUrl: 'http://localhost:8081',
  websocketUrl: 'ws://localhost:8085/webSocket?format=structured',
  waitTimeoutMs: 15000,
  requestTimeoutMs: 15000,
  stage: 'manual',
  round: 1,
  warmup: false,
  outputRoot: path.resolve('benchmark', 'results')
};

function parseArgs(argv) {
  const args = { ...defaults };
  const names = {
    '--requests': 'requests',
    '--concurrency': 'concurrency',
    '--clients': 'clients',
    '--quantity': 'quantity',
    '--product-id': 'productId',
    '--order-url': 'orderBaseUrl',
    '--product-url': 'productBaseUrl',
    '--websocket-url': 'websocketUrl',
    '--wait-timeout-ms': 'waitTimeoutMs',
    '--request-timeout-ms': 'requestTimeoutMs',
    '--stage': 'stage',
    '--round': 'round',
    '--output-root': 'outputRoot'
  };
  const numeric = new Set(['requests', 'concurrency', 'clients', 'quantity', 'productId', 'waitTimeoutMs', 'requestTimeoutMs', 'round']);
  for (let i = 0; i < argv.length; i += 1) {
    if (argv[i] === '--warmup') {
      args.warmup = true;
      continue;
    }
    const key = names[argv[i]];
    if (!key || i + 1 >= argv.length) throw new Error(`Unknown or incomplete argument: ${argv[i]}`);
    const value = argv[++i];
    args[key] = numeric.has(key) ? Number(value) : value;
  }
  for (const key of ['requests', 'concurrency', 'clients', 'quantity', 'waitTimeoutMs', 'requestTimeoutMs', 'round']) {
    if (!Number.isInteger(args[key]) || args[key] <= 0) throw new Error(`${key} must be a positive integer`);
  }
  if (args.productId !== null && (!Number.isInteger(args.productId) || args.productId <= 0)) {
    throw new Error('productId must be a positive integer');
  }
  args.concurrency = Math.min(args.concurrency, args.requests);
  args.outputRoot = path.resolve(args.outputRoot);
  return args;
}

function compactTimestamp(date = new Date()) {
  return date.toISOString().replace(/[-:]/g, '').replace(/\.\d{3}Z$/, 'Z');
}

function hashText(text) {
  let hash = 2166136261;
  for (const char of text) {
    hash ^= char.charCodeAt(0);
    hash = Math.imul(hash, 16777619);
  }
  return hash >>> 0;
}

function percentile(values, p) {
  if (values.length === 0) return null;
  const sorted = [...values].sort((a, b) => a - b);
  const rank = Math.ceil((p / 100) * sorted.length) - 1;
  return sorted[Math.max(0, rank)];
}

function latencyStats(values) {
  if (values.length === 0) return { count: 0, average: null, p50: null, p95: null, p99: null, max: null };
  return {
    count: values.length,
    average: Number((values.reduce((sum, value) => sum + value, 0) / values.length).toFixed(3)),
    p50: percentile(values, 50),
    p95: percentile(values, 95),
    p99: percentile(values, 99),
    max: Math.max(...values)
  };
}

async function fetchJson(url, options = {}, timeoutMs = defaults.requestTimeoutMs) {
  const response = await fetch(url, { ...options, signal: AbortSignal.timeout(timeoutMs) });
  const text = await response.text();
  let body = null;
  try {
    body = text ? JSON.parse(text) : null;
  } catch {
    // Keep body null and preserve a short response excerpt for diagnostics.
  }
  return { response, body, text };
}

async function readProduct(args, requiredStock) {
  if (args.productId !== null) {
    const { response, body, text } = await fetchJson(`${args.productBaseUrl}/buyer/product/findById/${args.productId}`, {}, args.requestTimeoutMs);
    if (!response.ok || !body || !Number.isFinite(Number(body.productStock))) {
      throw new Error(`Cannot read product ${args.productId}: HTTP ${response.status} ${text.slice(0, 120)}`);
    }
    return body;
  }
  const { response, body, text } = await fetchJson(`${args.productBaseUrl}/buyer/product/list`, {}, args.requestTimeoutMs);
  if (!response.ok || body?.code !== 0 || !Array.isArray(body.data)) {
    throw new Error(`Cannot list products: HTTP ${response.status} ${text.slice(0, 120)}`);
  }
  const products = body.data.flatMap(category => Array.isArray(category.goods) ? category.goods : []);
  const eligible = products
    .filter(product => Number(product.stock) >= requiredStock)
    .sort((a, b) => Number(b.stock) - Number(a.stock));
  if (eligible.length === 0) throw new Error(`No product has the required stock (${requiredStock})`);
  return { productId: Number(eligible[0].id), productStock: Number(eligible[0].stock), productName: eligible[0].name };
}

async function readStock(args, productId) {
  const { response, body, text } = await fetchJson(`${args.productBaseUrl}/buyer/product/findById/${productId}`, {}, args.requestTimeoutMs);
  if (!response.ok || !body || !Number.isFinite(Number(body.productStock))) {
    throw new Error(`Cannot read stock for product ${productId}: HTTP ${response.status} ${text.slice(0, 120)}`);
  }
  return Number(body.productStock);
}

function connectWebSocket(url, clientId, onNotification, timeoutMs) {
  return new Promise((resolve, reject) => {
    const socket = new WebSocket(url);
    const timer = setTimeout(() => {
      socket.close();
      reject(new Error(`WebSocket client ${clientId} connection timed out`));
    }, timeoutMs);
    socket.addEventListener('message', event => onNotification(clientId, String(event.data)));
    socket.addEventListener('open', () => {
      clearTimeout(timer);
      resolve(socket);
    }, { once: true });
    socket.addEventListener('error', () => {
      clearTimeout(timer);
      reject(new Error(`WebSocket client ${clientId} failed to connect`));
    }, { once: true });
  });
}

async function runWithConcurrency(total, concurrency, worker) {
  let nextIndex = 0;
  async function consume() {
    while (true) {
      const index = nextIndex++;
      if (index >= total) return;
      await worker(index);
    }
  }
  await Promise.all(Array.from({ length: concurrency }, () => consume()));
}

async function waitForNotifications(isComplete, timeoutMs) {
  const deadline = Date.now() + timeoutMs;
  while (!isComplete() && Date.now() < deadline) {
    await new Promise(resolve => setTimeout(resolve, 25));
  }
}

function csvCell(value) {
  if (value === null || value === undefined) return '';
  const string = String(value);
  return /[",\r\n]/.test(string) ? `"${string.replaceAll('"', '""')}"` : string;
}

function toCsv(rows, columns) {
  return [columns.join(','), ...rows.map(row => columns.map(column => csvCell(row[column])).join(','))].join('\r\n') + '\r\n';
}

async function discoverRunOrders(args, buyerId, marker) {
  const pageSize = Math.max(100, args.requests * 2 + 20);
  const { response, body, text } = await fetchJson(`${args.orderBaseUrl}/buyer/order/list/${buyerId}/1/${pageSize}`, {}, args.requestTimeoutMs);
  if (!response.ok || body?.code !== 0 || !Array.isArray(body.data)) {
    throw new Error(`Cannot discover benchmark orders for cleanup: HTTP ${response.status} ${text.slice(0, 120)}`);
  }
  return body.data.filter(order => order.buyerName === marker && order.buyerAddress === marker).map(order => order.orderId);
}

async function cleanupOrders(args, buyerId, orderIds) {
  const results = [];
  for (const orderId of orderIds) {
    try {
      const { response, body, text } = await fetchJson(
        `${args.orderBaseUrl}/buyer/order/cancel/${buyerId}/${encodeURIComponent(orderId)}`,
        { method: 'PUT' },
        args.requestTimeoutMs
      );
      const ok = response.ok && body?.code === 0;
      results.push({ orderId, ok, httpStatus: response.status, error: ok ? null : (body?.msg ?? text.slice(0, 120)) });
    } catch (error) {
      results.push({ orderId, ok: false, httpStatus: null, error: error.message });
    }
  }
  return results;
}

function makeMarkdown(summary) {
  const pct = value => `${(value * 100).toFixed(3)}%`;
  const latency = (name, stats) => `| ${name} | ${stats.count} | ${stats.average ?? 'N/A'} | ${stats.p50 ?? 'N/A'} | ${stats.p95 ?? 'N/A'} | ${stats.p99 ?? 'N/A'} | ${stats.max ?? 'N/A'} |`;
  return `# New-order notification benchmark\n\n` +
    `- testRunId: \`${summary.testRunId}\`\n` +
    `- stage / round: ${summary.stage} / ${summary.round}\n` +
    `- requests / concurrency / clients: ${summary.requests} / ${summary.concurrency} / ${summary.clients}\n` +
    `- productId / quantity: ${summary.productId} / ${summary.quantity}\n` +
    `- order success: ${summary.orderSuccessCount}/${summary.requests} (${pct(summary.orderSuccessRate)})\n` +
    `- notification success: ${summary.uniqueNotifiedOrders}/${summary.orderSuccessCount} (${summary.notificationSuccessRate === null ? 'N/A' : pct(summary.notificationSuccessRate)})\n` +
    `- lost messages: ${summary.messageLossCount}\n` +
    `- duplicate notifications: ${summary.duplicateNotificationCount}\n` +
    `- broadcast deliveries: ${summary.uniqueClientDeliveries}/${summary.expectedClientDeliveries}\n` +
    `- stock: ${summary.stock.before} -> ${summary.stock.afterCreate} -> ${summary.stock.afterCleanup}\n` +
    `- cleanup verified: ${summary.cleanup.verified}\n\n` +
    `| Metric (ms) | Count | Average | P50 | P95 | P99 | Max |\n` +
    `|---|---:|---:|---:|---:|---:|---:|\n` +
    `${latency('MQ consume: consumedAt - sentAt', summary.latency.mqConsume)}\n` +
    `${latency('WebSocket: receivedAt - consumedAt', summary.latency.websocketPush)}\n` +
    `${latency('Notification E2E: receivedAt - sentAt', summary.latency.notificationE2E)}\n` +
    `${latency('Business perceived: receivedAt - requestAt', summary.latency.businessPerceived)}\n\n` +
    `This is a local functional/lightweight performance result, not a production benchmark. ` +
    `It covers online WebSocket clients only and does not test offline notification or a polling control group.\n`;
}

async function main() {
  const args = parseArgs(process.argv.slice(2));
  const testRunId = `${compactTimestamp()}-${args.stage}-r${args.round}-${crypto.randomUUID().slice(0, 8)}`;
  const buyerId = 1500000000 + (hashText(testRunId) % 500000000);
  // order_master.buyer_name is varchar(32); keep the exact cleanup marker well below that limit.
  const marker = `BENCH_${testRunId.slice(-17)}`;
  const outputDir = path.join(args.outputRoot, testRunId);
  await mkdir(outputDir, { recursive: true });

  const requiredStock = args.requests * args.quantity;
  const product = await readProduct(args, requiredStock);
  const productId = Number(product.productId ?? product.id);
  const stockBefore = await readStock(args, productId);
  if (stockBefore < requiredStock) throw new Error(`Insufficient stock: need ${requiredStock}, found ${stockBefore}`);

  const notifications = [];
  const ignoredMessages = [];
  const onNotification = (clientId, rawMessage) => {
    const receivedAt = Date.now();
    try {
      const event = JSON.parse(rawMessage);
      if (event.testRunId !== testRunId) {
        ignoredMessages.push({ clientId, receivedAt, reason: 'foreign-testRunId' });
        return;
      }
      notifications.push({
        clientId,
        eventId: event.eventId ?? null,
        orderId: event.orderId ?? null,
        sentAt: Number(event.sentAt) || null,
        consumedAt: Number(event.consumedAt) || null,
        receivedAt
      });
    } catch {
      ignoredMessages.push({ clientId, receivedAt, reason: 'non-json-message' });
    }
  };

  const sockets = await Promise.all(Array.from({ length: args.clients }, (_, index) =>
    connectWebSocket(args.websocketUrl, `ws-${index + 1}`, onNotification, args.requestTimeoutMs)
  ));

  const records = Array.from({ length: args.requests }, (_, index) => ({
    requestIndex: index + 1,
    testRunId,
    eventId: null,
    orderId: null,
    requestAt: null,
    responseAt: null,
    sentAt: null,
    consumedAt: null,
    receivedAt: null,
    httpStatus: null,
    error: null,
    notificationCount: 0
  }));

  try {
    await runWithConcurrency(args.requests, args.concurrency, async index => {
      const record = records[index];
      record.requestAt = Date.now();
      try {
        const payload = {
          name: marker,
          phone: '13000000000',
          address: marker,
          id: buyerId,
          testRunId,
          items: [{ productId, productQuantity: args.quantity }]
        };
        const { response, body, text } = await fetchJson(
          `${args.orderBaseUrl}/buyer/order/create`,
          { method: 'POST', headers: { 'content-type': 'application/json' }, body: JSON.stringify(payload) },
          args.requestTimeoutMs
        );
        record.responseAt = Date.now();
        record.httpStatus = response.status;
        if (response.ok && body?.code === 0 && body?.data?.orderId) {
          record.orderId = String(body.data.orderId);
          record.eventId = body.data.eventId ? String(body.data.eventId) : null;
          record.sentAt = Number(body.data.sentAt) || null;
        } else {
          record.error = body?.msg ?? text.slice(0, 200) ?? `HTTP ${response.status}`;
        }
      } catch (error) {
        record.responseAt = Date.now();
        record.error = error.message;
      }
    });

    const successful = records.filter(record => record.orderId && record.eventId);
    const expectedEventIds = new Set(successful.map(record => record.eventId));
    const complete = () => {
      const deliveries = new Set(notifications
        .filter(item => expectedEventIds.has(item.eventId))
        .map(item => `${item.clientId}:${item.eventId}`));
      return deliveries.size >= successful.length * args.clients;
    };
    await waitForNotifications(complete, args.waitTimeoutMs);
  } finally {
    for (const socket of sockets) socket.close();
  }

  const stockAfterCreate = await readStock(args, productId);
  let discoveredOrderIds = [];
  let discoveryError = null;
  try {
    discoveredOrderIds = await discoverRunOrders(args, buyerId, marker);
  } catch (error) {
    discoveryError = error.message;
  }
  const knownOrderIds = records.filter(record => record.orderId).map(record => record.orderId);
  const cleanupTargets = [...new Set([...knownOrderIds, ...discoveredOrderIds])];
  const cleanupResults = await cleanupOrders(args, buyerId, cleanupTargets);
  const stockAfterCleanup = await readStock(args, productId);

  const successful = records.filter(record => record.orderId && record.eventId);
  const successfulByEvent = new Map(successful.map(record => [record.eventId, record]));
  const matchingNotifications = notifications.filter(item => successfulByEvent.has(item.eventId));
  for (const record of successful) {
    const matches = matchingNotifications.filter(item => item.eventId === record.eventId).sort((a, b) => a.receivedAt - b.receivedAt);
    record.notificationCount = matches.length;
    if (matches.length > 0) {
      record.sentAt = matches[0].sentAt ?? record.sentAt;
      record.consumedAt = matches[0].consumedAt;
      record.receivedAt = matches[0].receivedAt;
    }
  }

  const uniqueNotifiedOrders = new Set(matchingNotifications.map(item => item.eventId)).size;
  const uniqueClientDeliveries = new Set(matchingNotifications.map(item => `${item.clientId}:${item.eventId}`)).size;
  const duplicateNotificationCount = matchingNotifications.length - uniqueClientDeliveries;
  const mqLatencies = matchingNotifications.filter(item => item.sentAt !== null && item.consumedAt !== null).map(item => item.consumedAt - item.sentAt);
  const wsLatencies = matchingNotifications.filter(item => item.consumedAt !== null).map(item => item.receivedAt - item.consumedAt);
  const e2eLatencies = matchingNotifications.filter(item => item.sentAt !== null).map(item => item.receivedAt - item.sentAt);
  const businessLatencies = successful
    .filter(record => record.receivedAt !== null && record.requestAt !== null)
    .map(record => record.receivedAt - record.requestAt);
  const cleanupFailures = cleanupResults.filter(result => !result.ok);
  const undiscoveredRisk = records.some(record => !record.orderId) && discoveryError !== null;
  const cleanupVerified = cleanupFailures.length === 0 && !undiscoveredRisk && stockAfterCleanup === stockBefore;

  const summary = {
    schemaVersion: 1,
    generatedAt: new Date().toISOString(),
    testRunId,
    stage: args.stage,
    round: args.round,
    warmup: args.warmup,
    requests: args.requests,
    concurrency: args.concurrency,
    clients: args.clients,
    buyerId,
    productId,
    quantity: args.quantity,
    orderSuccessCount: successful.length,
    orderSuccessRate: successful.length / args.requests,
    uniqueNotifiedOrders,
    notificationSuccessRate: successful.length === 0 ? null : uniqueNotifiedOrders / successful.length,
    messageLossCount: successful.length - uniqueNotifiedOrders,
    duplicateNotificationCount,
    expectedClientDeliveries: successful.length * args.clients,
    uniqueClientDeliveries,
    clientDeliveryLossCount: successful.length * args.clients - uniqueClientDeliveries,
    unmatchedNotificationCount: notifications.length - matchingNotifications.length,
    ignoredMessageCount: ignoredMessages.length,
    latency: {
      mqConsume: latencyStats(mqLatencies),
      websocketPush: latencyStats(wsLatencies),
      notificationE2E: latencyStats(e2eLatencies),
      businessPerceived: latencyStats(businessLatencies)
    },
    stock: { before: stockBefore, afterCreate: stockAfterCreate, afterCleanup: stockAfterCleanup },
    cleanup: {
      targetCount: cleanupTargets.length,
      discoveredOrderCount: discoveredOrderIds.length,
      failedCount: cleanupFailures.length,
      discoveryError,
      verified: cleanupVerified
    },
    environment: {
      node: process.version,
      platform: `${process.platform}-${process.arch}`,
      scope: 'local functional and lightweight performance test',
      websocketDelivery: 'online clients only'
    }
  };

  const orderColumns = ['testRunId', 'eventId', 'orderId', 'requestAt', 'responseAt', 'sentAt', 'consumedAt', 'receivedAt', 'httpStatus', 'error', 'notificationCount', 'requestIndex'];
  const notificationColumns = ['testRunId', 'clientId', 'eventId', 'orderId', 'sentAt', 'consumedAt', 'receivedAt'];
  await Promise.all([
    writeFile(path.join(outputDir, 'raw-orders.csv'), toCsv(records, orderColumns), 'utf8'),
    writeFile(path.join(outputDir, 'raw-notifications.csv'), toCsv(matchingNotifications.map(item => ({ testRunId, ...item })), notificationColumns), 'utf8'),
    writeFile(path.join(outputDir, 'cleanup.json'), JSON.stringify({ discoveryError, cleanupResults }, null, 2), 'utf8'),
    writeFile(path.join(outputDir, 'summary.json'), JSON.stringify(summary, null, 2), 'utf8'),
    writeFile(path.join(outputDir, 'summary.md'), makeMarkdown(summary), 'utf8')
  ]);

  console.log(JSON.stringify({ outputDir, summary }, null, 2));
  if (!cleanupVerified || summary.messageLossCount > 0 || summary.duplicateNotificationCount > 0 || summary.orderSuccessRate < 1) {
    process.exitCode = 2;
  }
}

main().catch(error => {
  console.error(error.stack ?? error.message);
  process.exitCode = 1;
});
