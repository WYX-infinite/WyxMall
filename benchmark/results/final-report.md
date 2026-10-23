# 新订单实时通知链路本地量化测试报告

生成时间：2026-10-23（Asia/Shanghai）

## 1. 测试边界与环境

- 链路：订单创建 -> RocketMQ 发送 -> mq-service 消费 -> WebSocket 广播 -> 在线 Node.js 客户端实际接收。
- 环境：Windows、Java 8、Maven Wrapper、Node.js v24.14.0、MySQL、Nacos、RocketMQ 4.7.1。
- RocketMQ：NameServer `192.168.154.130:9876`，Broker `192.168.154.130:10911`。
- 服务端口：product-service 8081、order-service 8082、mq-service 8085。
- 测试商品：productId=5；正式套件每轮库存从 1084 开始，测试订单取消后均恢复到 1084。
- 结论仅适用于本地功能性和轻量性能测试，不代表生产环境容量；只验证在线 WebSocket 广播，没有离线通知和轮询对照组。
- 项目目录及其父目录不是 Git 仓库，因此没有 Git 状态可报告，也未执行提交或推送。

## 2. 埋点与兼容性修改

- 新增 `NewOrderEvent`，包含 `type`、`message`、`eventId`、`orderId`、`testRunId`、`sentAt`、`consumedAt`。
- 下单接口发送结构化 MQ JSON，并在响应中返回 `orderId`、`eventId`、`sentAt` 和 `testRunId`。
- mq-service 收到消息后写入 `consumedAt`，再推送到 WebSocket。
- 普通 `/webSocket` 客户端继续收到原字符串“有新的订单”；只有 `/webSocket?format=structured` 测试客户端收到结构化 JSON，原卖家页面提醒保持兼容。
- WebSocket 会话写入增加会话级串行化，避免多个 MQ 消费线程并发写同一 `BasicRemote`。
- 商品扣减/回补改为数据库原子更新，避免并发读改写造成库存覆盖。
- 修正订单取消调用为按主键更新，避免无条件更新订单表。

## 3. 执行命令

单场景：

```powershell
node benchmark/notification-benchmark.mjs --requests 100 --concurrency 1
```

受保护的完整分阶段套件：

```powershell
node benchmark/run-suite.mjs
```

合并正式阶段原始数据：

```powershell
node benchmark/aggregate-results.mjs
```

构建与测试：

```powershell
.\mvnw.cmd -pl product-service,order-service,mq-service -am test
node --check benchmark\notification-benchmark.mjs
node --check benchmark\run-suite.mjs
node --check benchmark\aggregate-results.mjs
```

## 4. 分阶段实际结果

预热 20 单、并发 1 完成，但不计入正式统计。正式套件共发起 1510 次下单请求，成功创建 1510 单，收到 1510 个唯一订单事件；多客户端阶段另验证了 100 个事件向 5 个客户端产生的 500 次预期投递。

| 场景 | 轮次 | 请求/轮 | 并发 | 客户端 | 下单成功率 | 通知成功率 | 丢失 | 重复 | E2E 平均/P50/P95/P99/最大 ms |
|---|---:|---:|---:|---:|---:|---:|---:|---:|---|
| 连通性 | 1 | 10 | 1 | 1 | 100% | 100% | 0 | 0 | 9.900 / 9 / 13 / 13 / 13 |
| 顺序稳定性 R1 | 1 | 100 | 1 | 1 | 100% | 100% | 0 | 0 | 9.530 / 9 / 14 / 21 / 26 |
| 顺序稳定性 R2 | 2 | 100 | 1 | 1 | 100% | 100% | 0 | 0 | 11.220 / 10 / 23 / 28 / 34 |
| 顺序稳定性 R3 | 3 | 100 | 1 | 1 | 100% | 100% | 0 | 0 | 10.510 / 9 / 19 / 26 / 26 |
| 轻量并发 R1 | 1 | 200 | 5 | 1 | 100% | 100% | 0 | 0 | 10.585 / 9 / 21 / 26 / 31 |
| 轻量并发 R2 | 2 | 200 | 5 | 1 | 100% | 100% | 0 | 0 | 10.095 / 8 / 22 / 33 / 36 |
| 轻量并发 R3 | 3 | 200 | 5 | 1 | 100% | 100% | 0 | 0 | 9.490 / 7 / 21 / 26 / 28 |
| 压力观察 | 1 | 500 | 10 | 1 | 100% | 100% | 0 | 0 | 9.322 / 8 / 20 / 25 / 29 |
| 多客户端广播 | 1 | 100 | 1 | 5 | 100% | 100% | 0 | 0 | 6.676 / 6 / 11 / 14 / 16 |

三轮场景以三轮各自指标的中位数为代表值：

| 场景 | 指标 | 平均 ms | P50 ms | P95 ms | P99 ms | 最大 ms |
|---|---|---:|---:|---:|---:|---:|
| 顺序 100 单 | MQ 消费 | 7.570 | 6 | 17 | 24 | 25 |
| 顺序 100 单 | WebSocket 推送 | 2.900 | 3 | 5 | 8 | 8 |
| 顺序 100 单 | 通知 E2E | 10.510 | 9 | 19 | 26 | 26 |
| 顺序 100 单 | 完整业务感知 | 60.930 | 62 | 74 | 83 | 88 |
| 并发 5、200 单 | MQ 消费 | 8.080 | 6 | 19 | 25 | 27 |
| 并发 5、200 单 | WebSocket 推送 | 2.015 | 2 | 4 | 6 | 9 |
| 并发 5、200 单 | 通知 E2E | 10.095 | 8 | 21 | 26 | 31 |
| 并发 5、200 单 | 完整业务感知 | 79.210 | 77 | 98 | 103 | 124 |

全部 1510 条正式订单合并计算的补充统计：

| 指标 | 平均 ms | P50 ms | P95 ms | P99 ms | 最大 ms |
|---|---:|---:|---:|---:|---:|
| MQ 消费 | 7.228 | 6 | 17 | 24 | 35 |
| WebSocket 推送 | 2.389 | 2 | 5 | 8 | 20 |
| 通知 E2E | 9.617 | 8 | 20 | 27 | 36 |
| 完整业务感知 | 97.315 | 81 | 169 | 200 | 237 |

正式套件汇总：下单成功率 `1510/1510 = 100%`；通知成功率 `1510/1510 = 100%`；消息丢失数 0；重复通知数 0。多客户端的 500 次接收按 `(clientId,eventId)` 判断，500/500 到达，不能把正常广播的 5 份消息误算为重复。

## 5. 测试数据安全与恢复

- 每轮使用唯一 `testRunId`、独立数值 buyer 标识和精确 `BENCH_...` 买家标记。
- 每轮只发现并取消该轮标记下的订单；正式套件所有清理均通过，最终商品 5 库存为 1084，活动状态的 benchmark 订单为 0。
- 正式测试订单保留为已取消记录，没有删除业务数据。

在正式套件前的诊断运行中暴露了一个严重的已有缺陷：取消接口使用 `update(entity, null)`，一次取消无 WHERE 更新了 `order_master` 的 21 行。测试当即停止扩压；随后依据 MySQL ROW/FULL binlog 的 before-image 精确恢复 11 条历史订单，并回补缺少的 9 件库存。修复为 `updateById` 后重新验证。最终历史 buyer 27 的 11 条订单状态分布为：5 条 `order_status=0/pay_status=0`、1 条 `1/1`、5 条 `2/1`，与事故前一致。相关事故日志保留在 `benchmark/runtime-logs/*incident-20261023*`。

另一次最初连通性运行因为 `buyer_name varchar(32)` 而测试标记过长，10/10 请求失败、没有写入订单且库存未变化；之后缩短标记。还发现 MQ 消费线程并发写同一 WebSocket 会话会造成 3 条消息缺失 `consumedAt`，通过会话级发送锁修复后重跑通过。这些诊断失败不计入正式 1510 次请求，但原始结果均保留，没有删除或美化。

## 6. 验证结果与已知问题

- Maven Reactor：BUILD SUCCESS；本次相关测试 6 个通过（product-service 3、order-service 1、mq-service 2），0 失败、0 错误。
- 三个 Node.js 脚本均通过 `node --check`。
- Maven 仍报告根 POM 将两个 BOM 的 `scope=import` 放在普通 dependencies 中，以及旧 `junit:junit:3.8.1` 无法被 Vintage 正确解析；本次新增测试仍被发现并全部通过。
- order-service 日志中存在 RocketMQ system trace hook 初始化失败，但业务消息发送、消费与 1510 次正式链路验证均成功；这不等于 trace 功能正常。
- 当前消息只向在线 WebSocket 客户端广播；客户端离线时没有持久化、重放或补偿机制。

## 7. 产物

- 正式订单原始数据：`benchmark/results/formal-raw-orders.csv`（1510 行数据）。
- 正式通知原始数据：`benchmark/results/formal-raw-notifications.csv`（1910 行数据，包含五客户端阶段额外广播副本）。
- 套件机器可读汇总：`benchmark/results/suite-summary.json`。
- 套件表格报告：`benchmark/results/suite-report.md`。
- 每轮原始记录、清理结果及单轮报告：`benchmark/results/<testRunId>/`。

## 8. 可用于简历的描述

设计并完成新订单实时通知链路的结构化埋点与本地量化测试，分阶段完成 1510 次下单请求，下单及在线商家端通知成功率均为 100%，消息丢失与重复数均为 0；在 200 单、并发 5 的三轮测试中，端到端通知延迟三轮中位数 P50 为 8 ms、P95 为 21 ms、P99 为 26 ms。
