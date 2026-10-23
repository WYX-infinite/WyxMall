# New-order notification benchmark

- testRunId: `20261023T035906Z-concurrent-200-r2-0ca1fac7`
- stage / round: concurrent-200 / 2
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
| MQ consume: consumedAt - sentAt | 200 | 8.08 | 6 | 21 | 32 | 35 |
| WebSocket: receivedAt - consumedAt | 200 | 2.015 | 2 | 4 | 5 | 7 |
| Notification E2E: receivedAt - sentAt | 200 | 10.095 | 8 | 22 | 33 | 36 |
| Business perceived: receivedAt - requestAt | 200 | 82.93 | 81 | 104 | 114 | 125 |

This is a local functional/lightweight performance result, not a production benchmark. It covers online WebSocket clients only and does not test offline notification or a polling control group.
