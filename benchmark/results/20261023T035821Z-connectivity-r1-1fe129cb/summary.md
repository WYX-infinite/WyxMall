# New-order notification benchmark

- testRunId: `20261023T035821Z-connectivity-r1-1fe129cb`
- stage / round: connectivity / 1
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
| MQ consume: consumedAt - sentAt | 10 | 7.9 | 7 | 10 | 10 | 10 |
| WebSocket: receivedAt - consumedAt | 10 | 2 | 2 | 4 | 4 | 4 |
| Notification E2E: receivedAt - sentAt | 10 | 9.9 | 9 | 13 | 13 | 13 |
| Business perceived: receivedAt - requestAt | 10 | 65.4 | 64 | 77 | 77 | 77 |

This is a local functional/lightweight performance result, not a production benchmark. It covers online WebSocket clients only and does not test offline notification or a polling control group.
