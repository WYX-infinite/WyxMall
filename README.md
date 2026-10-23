# WyxMall

WyxMall is a Java 8 e-commerce backend built as a Maven multi-module project with Spring Boot, Spring Cloud, Spring Cloud Alibaba, MyBatis-Plus, Redis, RocketMQ, and Nacos.

## Modules

- `gateway-service`: API gateway
- `account-service`: account and authentication APIs
- `product-service`: product and inventory APIs
- `order-service`: order APIs
- `sms-service`: SMS verification
- `mq-service`: message consumption and WebSocket notifications
- `base-service`, `common-service`, `repository-service`: shared dependencies, models, and persistence support

## Local configuration

Set the environment variables required by the services you run:

- `MYSQL_PASSWORD`
- `REDIS_HOST` and `REDIS_PASSWORD`
- `ROCKETMQ_NAME_SERVER`
- `JWT_SECRET`
- `JDWX_APP_KEY` (only when using the SMS integration)

The default database schema name is currently `uushop` for compatibility with the existing local database.

## Build

```powershell
.\mvnw.cmd test
```

Benchmark scripts and reports are under `benchmark/`. Generated runtime logs are intentionally excluded from version control.
