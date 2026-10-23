# New-order notification benchmark

- testRunId: `20261023T035856Z-concurrent-200-r1-e1670ec0`
- stage / round: concurrent-200 / 1
- requests / concurrency / clients: 200 / 5 / 1
- productId / quantity: 5 / 1
- order success: 200/200 (100.000%)
- notification success: 200/200 (100.000%)
- lost messages: 0
- duplicate notifications: 0
- broadcast deliveries: 200/200
- stock: 1084 -> 884 -> 1084
- cleanup verified: true

| Metric (ms) | Count | Average | P50 | P95 | P99 | Max |
|---|---:|---:|---:|---:|---:|---:|
| MQ consume: consumedAt - sentAt | 200 | 8.31 | 7 | 17 | 25 | 27 |
| WebSocket: receivedAt - consumedAt | 200 | 2.275 | 2 | 5 | 7 | 9 |
| Notification E2E: receivedAt - sentAt | 200 | 10.585 | 9 | 21 | 26 | 31 |
| Business perceived: receivedAt - requestAt | 200 | 79.21 | 77 | 98 | 103 | 124 |

This is a local functional/lightweight performance result, not a production benchmark. It covers online WebSocket clients only and does not test offline notification or a polling control group.
