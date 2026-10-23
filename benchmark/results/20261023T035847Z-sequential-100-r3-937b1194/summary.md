# New-order notification benchmark

- testRunId: `20261023T035847Z-sequential-100-r3-937b1194`
- stage / round: sequential-100 / 3
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
| MQ consume: consumedAt - sentAt | 100 | 7.57 | 6 | 17 | 24 | 25 |
| WebSocket: receivedAt - consumedAt | 100 | 2.94 | 3 | 5 | 8 | 8 |
| Notification E2E: receivedAt - sentAt | 100 | 10.51 | 9 | 19 | 26 | 26 |
| Business perceived: receivedAt - requestAt | 100 | 60.93 | 62 | 74 | 83 | 88 |

This is a local functional/lightweight performance result, not a production benchmark. It covers online WebSocket clients only and does not test offline notification or a polling control group.
