# Notification benchmark suite

Generated: 2026-10-23T03:59:57.372Z

## All executed runs

| Stage | Round | Requests | Concurrency | Clients | Order success | Notification success | Lost | Duplicates | E2E P50 ms | E2E P95 ms | E2E P99 ms | E2E max ms | Cleanup |
|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---|
| connectivity | 1 | 10 | 1 | 1 | 100.000% | 100.000% | 0 | 0 | 9 | 13 | 13 | 13 | true |
| warmup | 1 | 20 | 1 | 1 | 100.000% | 100.000% | 0 | 0 | 8 | 10 | 15 | 15 | true |
| sequential-100 | 1 | 100 | 1 | 1 | 100.000% | 100.000% | 0 | 0 | 9 | 14 | 21 | 26 | true |
| sequential-100 | 2 | 100 | 1 | 1 | 100.000% | 100.000% | 0 | 0 | 10 | 23 | 28 | 34 | true |
| sequential-100 | 3 | 100 | 1 | 1 | 100.000% | 100.000% | 0 | 0 | 9 | 19 | 26 | 26 | true |
| concurrent-200 | 1 | 200 | 5 | 1 | 100.000% | 100.000% | 0 | 0 | 9 | 21 | 26 | 31 | true |
| concurrent-200 | 2 | 200 | 5 | 1 | 100.000% | 100.000% | 0 | 0 | 8 | 22 | 33 | 36 | true |
| concurrent-200 | 3 | 200 | 5 | 1 | 100.000% | 100.000% | 0 | 0 | 7 | 21 | 26 | 28 | true |
| pressure-500 | 1 | 500 | 10 | 1 | 100.000% | 100.000% | 0 | 0 | 8 | 20 | 25 | 29 | true |
| multi-client-100 | 1 | 100 | 1 | 5 | 100.000% | 100.000% | 0 | 0 | 6 | 11 | 14 | 16 | true |

## Median representative values across rounds

| Stage | Rounds | Order success | Notification success | Lost | Duplicates | E2E average ms | E2E P50 ms | E2E P95 ms | E2E P99 ms | E2E max ms |
|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|
| connectivity | 1 | 100.000% | 100.000% | 0 | 0 | 9.9 | 9 | 13 | 13 | 13 |
| sequential-100 | 3 | 100.000% | 100.000% | 0 | 0 | 10.51 | 9 | 19 | 26 | 26 |
| concurrent-200 | 3 | 100.000% | 100.000% | 0 | 0 | 10.095 | 8 | 21 | 26 | 31 |
| pressure-500 | 1 | 100.000% | 100.000% | 0 | 0 | 9.322 | 8 | 20 | 25 | 29 |
| multi-client-100 | 1 | 100.000% | 100.000% | 0 | 0 | 6.676 | 6 | 11 | 14 | 16 |

Local functional/lightweight performance testing only. Online WebSocket delivery only; no offline notification or polling comparison was tested.
