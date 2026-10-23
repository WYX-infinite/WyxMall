# New-order notification benchmark

- testRunId: `20261023T035835Z-sequential-100-r2-4588c200`
- stage / round: sequential-100 / 2
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
| MQ consume: consumedAt - sentAt | 100 | 8.47 | 7 | 17 | 25 | 26 |
| WebSocket: receivedAt - consumedAt | 100 | 2.75 | 2 | 7 | 9 | 9 |
| Notification E2E: receivedAt - sentAt | 100 | 11.22 | 10 | 23 | 28 | 34 |
| Business perceived: receivedAt - requestAt | 100 | 68.39 | 63 | 105 | 119 | 131 |

This is a local functional/lightweight performance result, not a production benchmark. It covers online WebSocket clients only and does not test offline notification or a polling control group.
