# New-order notification benchmark

- testRunId: `20261023T035915Z-concurrent-200-r3-5452fff7`
- stage / round: concurrent-200 / 3
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
| MQ consume: consumedAt - sentAt | 200 | 7.675 | 6 | 19 | 24 | 27 |
| WebSocket: receivedAt - consumedAt | 200 | 1.815 | 2 | 4 | 6 | 9 |
| Notification E2E: receivedAt - sentAt | 200 | 9.49 | 7 | 21 | 26 | 28 |
| Business perceived: receivedAt - requestAt | 200 | 77.255 | 76 | 94 | 101 | 109 |

This is a local functional/lightweight performance result, not a production benchmark. It covers online WebSocket clients only and does not test offline notification or a polling control group.
