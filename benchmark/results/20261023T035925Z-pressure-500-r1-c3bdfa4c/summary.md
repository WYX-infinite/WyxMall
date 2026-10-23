# New-order notification benchmark

- testRunId: `20261023T035925Z-pressure-500-r1-c3bdfa4c`
- stage / round: pressure-500 / 1
- requests / concurrency / clients: 500 / 10 / 1
- productId / quantity: 5 / 1
- order success: 500/500 (100.000%)
- notification success: 500/500 (100.000%)
- lost messages: 0
- duplicate notifications: 0
- broadcast deliveries: 500/500
- stock: 1084 -> 584 -> 1084
- cleanup verified: true

| Metric (ms) | Count | Average | P50 | P95 | P99 | Max |
|---|---:|---:|---:|---:|---:|---:|
| MQ consume: consumedAt - sentAt | 500 | 6.758 | 6 | 15 | 20 | 25 |
| WebSocket: receivedAt - consumedAt | 500 | 2.564 | 2 | 5 | 13 | 20 |
| Notification E2E: receivedAt - sentAt | 500 | 9.322 | 8 | 20 | 25 | 29 |
| Business perceived: receivedAt - requestAt | 500 | 149.382 | 145 | 192 | 209 | 237 |

This is a local functional/lightweight performance result, not a production benchmark. It covers online WebSocket clients only and does not test offline notification or a polling control group.
