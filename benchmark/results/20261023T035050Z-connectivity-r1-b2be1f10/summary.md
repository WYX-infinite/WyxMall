# New-order notification benchmark

- testRunId: `20261023T035050Z-connectivity-r1-b2be1f10`
- stage / round: connectivity / 1
- requests / concurrency / clients: 10 / 1 / 1
- productId / quantity: 5 / 1
- order success: 0/10 (0.000%)
- notification success: 0/0 (N/A)
- lost messages: 0
- duplicate notifications: 0
- broadcast deliveries: 0/0
- stock: 1084 -> 1084 -> 1084
- cleanup verified: true

| Metric (ms) | Count | Average | P50 | P95 | P99 | Max |
|---|---:|---:|---:|---:|---:|---:|
| MQ consume: consumedAt - sentAt | 0 | N/A | N/A | N/A | N/A | N/A |
| WebSocket: receivedAt - consumedAt | 0 | N/A | N/A | N/A | N/A | N/A |
| Notification E2E: receivedAt - sentAt | 0 | N/A | N/A | N/A | N/A | N/A |
| Business perceived: receivedAt - requestAt | 0 | N/A | N/A | N/A | N/A | N/A |

This is a local functional/lightweight performance result, not a production benchmark. It covers online WebSocket clients only and does not test offline notification or a polling control group.
