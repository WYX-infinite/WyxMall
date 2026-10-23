# New-order notification benchmark

- testRunId: `20261023T035822Z-warmup-r1-15d99bd7`
- stage / round: warmup / 1
- requests / concurrency / clients: 20 / 1 / 1
- productId / quantity: 5 / 1
- order success: 20/20 (100.000%)
- notification success: 20/20 (100.000%)
- lost messages: 0
- duplicate notifications: 0
- broadcast deliveries: 20/20
- stock: 1084 -> 1064 -> 1084
- cleanup verified: true

| Metric (ms) | Count | Average | P50 | P95 | P99 | Max |
|---|---:|---:|---:|---:|---:|---:|
| MQ consume: consumedAt - sentAt | 20 | 6.5 | 6 | 8 | 10 | 10 |
| WebSocket: receivedAt - consumedAt | 20 | 1.95 | 2 | 4 | 5 | 5 |
| Notification E2E: receivedAt - sentAt | 20 | 8.45 | 8 | 10 | 15 | 15 |
| Business perceived: receivedAt - requestAt | 20 | 61.1 | 59 | 67 | 78 | 78 |

This is a local functional/lightweight performance result, not a production benchmark. It covers online WebSocket clients only and does not test offline notification or a polling control group.
