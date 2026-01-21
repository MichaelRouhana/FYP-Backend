
**Services**

| Service Name      | Container Name    | Image / Build           | Purpose                              | Exposed Ports   | Depends On                |
| ----------------- | ----------------- | ----------------------- | ------------------------------------ | --------------- | ------------------------- |
| **app**           | `spring-boot-app` | Built from `Dockerfile` | Spring Boot backend application      | `8080 → 8080`   | mysql, logstash, rabbitmq |
| **mysql**         | `mysql-db`        | `mysql:8.0`             | MySQL database for the application   | `3306 → 3306`   | —                         |
| **rabbitmq**      | `rabbitmq`        | `rabbitmq:3-management` | Message broker (async communication) | `5672`, `15672` | —                         |
| **logstash**      | `logstash`        | `logstash:8.6.0`        | Log processing and forwarding        | `5044`          | elasticsearch             |
| **elasticsearch** | `elasticsearch`   | `elasticsearch:8.6.0`   | Log storage & search engine          | `9200`          | —                         |
| **kibana**        | `kibana`          | `kibana:8.6.0`          | Log visualization & dashboards       | `5601`          | elasticsearch             |
