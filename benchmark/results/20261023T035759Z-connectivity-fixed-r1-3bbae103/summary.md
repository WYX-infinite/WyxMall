# New-order notification benchmark

- testRunId: `20261023T035759Z-connectivity-fixed-r1-3bbae103`
- stage / round: connectivity-fixed / 1
- requests / concurrency / clients: 10 / 1 / 1
- productId / quantity: 5 / 1
- order success: 10/10 (100.000%)
- notification success: 10/10 (100.000%)
- lost messages: 0
- duplicate notifications: 0
- broadcast deliveries: 10/10
- stock: 1084 -> 1074 -> 1084
- cleanup verified: true

| Metric (ms) | Count | Average | P50 | P95 | P99 | Max |
|---|---:|---:|---:|---:|---:|---:|
| MQ consume: consumedAt - sentAt | 10 | 1113.2 | 999 | 2097 | 2097 | 2097 |
| WebSocket: receivedAt - consumedAt | 10 | 72.9 | 73 | 74 | 74 | 74 |
| Notification E2E: receivedAt - sentAt | 10 | 1186.1 | 1070 | 2167 | 2167 | 2167 |
| Business perceived: receivedAt - requestAt | 10 | 1401.9 | 1127 | 3811 | 3811 | 3811 |

This is a local functional/lightweight performance result, not a production benchmark. It covers online WebSocket clients only and does not test offline notification or a polling control group.
