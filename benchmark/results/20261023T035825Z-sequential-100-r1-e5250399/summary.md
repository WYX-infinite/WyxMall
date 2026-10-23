# New-order notification benchmark

- testRunId: `20261023T035825Z-sequential-100-r1-e5250399`
- stage / round: sequential-100 / 1
- requests / concurrency / clients: 100 / 1 / 1
- productId / quantity: 5 / 1
- order success: 100/100 (100.000%)
- notification success: 100/100 (100.000%)
- lost messages: 0
- duplicate notifications: 0
- broadcast deliveries: 100/100
- stock: 1084 -> 984 -> 1084
- cleanup verified: true

| Metric (ms) | Count | Average | P50 | P95 | P99 | Max |
|---|---:|---:|---:|---:|---:|---:|
| MQ consume: consumedAt - sentAt | 100 | 6.63 | 6 | 11 | 17 | 23 |
| WebSocket: receivedAt - consumedAt | 100 | 2.9 | 3 | 5 | 5 | 6 |
| Notification E2E: receivedAt - sentAt | 100 | 9.53 | 9 | 14 | 21 | 26 |
| Business perceived: receivedAt - requestAt | 100 | 60.14 | 59 | 72 | 75 | 76 |

This is a local functional/lightweight performance result, not a production benchmark. It covers online WebSocket clients only and does not test offline notification or a polling control group.
