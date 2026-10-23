# New-order notification benchmark

- testRunId: `20261023T035949Z-multi-client-100-r1-362a4523`
- stage / round: multi-client-100 / 1
- requests / concurrency / clients: 100 / 1 / 5
- productId / quantity: 5 / 1
- order success: 100/100 (100.000%)
- notification success: 100/100 (100.000%)
- lost messages: 0
- duplicate notifications: 0
- broadcast deliveries: 500/500
- stock: 1084 -> 984 -> 1084
- cleanup verified: true

| Metric (ms) | Count | Average | P50 | P95 | P99 | Max |
|---|---:|---:|---:|---:|---:|---:|
| MQ consume: consumedAt - sentAt | 500 | 3.77 | 4 | 7 | 11 | 11 |
| WebSocket: receivedAt - consumedAt | 500 | 2.906 | 3 | 5 | 6 | 6 |
| Notification E2E: receivedAt - sentAt | 500 | 6.676 | 6 | 11 | 14 | 16 |
| Business perceived: receivedAt - requestAt | 100 | 47.76 | 47 | 53 | 57 | 60 |

This is a local functional/lightweight performance result, not a production benchmark. It covers online WebSocket clients only and does not test offline notification or a polling control group.
