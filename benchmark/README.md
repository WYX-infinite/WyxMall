# New-order notification benchmark

This dependency-free Node.js 24 benchmark measures:

`order create -> RocketMQ send -> mq-service consume -> WebSocket broadcast -> online client receive`

Prerequisites: MySQL, Nacos, RocketMQ NameServer/Broker, product-service (8081), order-service (8082), and mq-service (8085) must already be running.

Run one scenario from the repository root:

```powershell
node benchmark/notification-benchmark.mjs --requests 100 --concurrency 1
```

Run the guarded staged suite:

```powershell
node benchmark/run-suite.mjs
```

Merge the formal (non-warmup) raw records after the suite finishes:

```powershell
node benchmark/aggregate-results.mjs
```

Each run uses a unique `testRunId`, a dedicated numeric buyer marker, checks stock before and after, and cancels only orders bearing that run's exact buyer name/address marker. A run exits non-zero if order/notification delivery is unstable or inventory restoration cannot be verified. Results are written under `benchmark/results/<testRunId>/`; suite aggregates are `benchmark/results/suite-summary.json` and `benchmark/results/suite-report.md`. The aggregation command writes `formal-raw-orders.csv` and `formal-raw-notifications.csv` under the same results directory.
