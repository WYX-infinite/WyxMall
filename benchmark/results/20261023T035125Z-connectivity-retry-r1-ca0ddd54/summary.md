# New-order notification benchmark

- testRunId: `20261023T035125Z-connectivity-retry-r1-ca0ddd54`
- stage / round: connectivity-retry / 1
- requests / concurrency / clients: 10 / 1 / 1
- productId / quantity: 5 / 1
- order success: 10/10 (100.000%)
- notification success: 10/10 (100.000%)
- lost messages: 0
- duplicate notifications: 0
- broadcast deliveries: 10/10
- stock: 1084 -> 1074 -> 1075
- cleanup verified: false

| Metric (ms) | Count | Average | P50 | P95 | P99 | Max |
|---|---:|---:|---:|---:|---:|---:|
| MQ consume: consumedAt - sentAt | 7 | 2614.143 | 1089 | 11079 | 11079 | 11079 |
| WebSocket: receivedAt - consumedAt | 7 | 96 | 112 | 113 | 113 | 113 |
| Notification E2E: receivedAt - sentAt | 10 | 3268.6 | 1273 | 11080 | 11080 | 11080 |
| Business perceived: receivedAt - requestAt | 10 | 3335.1 | 1332 | 11140 | 11140 | 11140 |

This is a local functional/lightweight performance result, not a production benchmark. It covers online WebSocket clients only and does not test offline notification or a polling control group.
