
**Services**

| Service Name      | Container Name    | Image / Build           | Purpose                              | Exposed Ports   | Depends On                |
| ----------------- | ----------------- | ----------------------- | ------------------------------------ | --------------- | ------------------------- |
| **app**           | `spring-boot-app` | Built from `Dockerfile` | Spring Boot backend application      | `8080 → 8080`   | mysql, logstash, rabbitmq |
| **mysql**         | `mysql-db`        | `mysql:8.0`             | MySQL database for the application   | `3306 → 3306`   | —                         |
| **rabbitmq**      | `rabbitmq`        | `rabbitmq:3-management` | Message broker (async communication) | `5672`, `15672` | —                         |
| **logstash**      | `logstash`        | `logstash:8.6.0`        | Log processing and forwarding        | `5044`          | elasticsearch             |
| **elasticsearch** | `elasticsearch`   | `elasticsearch:8.6.0`   | Log storage & search engine          | `9200`          | —                         |
| **kibana**        | `kibana`          | `kibana:8.6.0`          | Log visualization & dashboards       | `5601`          | elasticsearch             |


To run you need docker and docker-compose
you need the docker-compose.yml file and application.properties


to deploy run
docker-compose -f docker-compose.yml up -d

to check status
docker ps

to check logs 
docker logs container-id

to check volumes
docker volume ls

to shut down
docker-compose -f docker-compose.yml down


mysql:
username : root
password : root
database : fyp


in application.properties file

api key:
football.api.key=api-key

email:
spring.mail.username=example@gmail.com
spring.mail.password=password

mysql: 
spring.datasource.url=jdbc:mysql://mysql:3306/fyp
spring.datasource.username=root
spring.datasource.password=root

if you want elk
spring.profiles.active=elk

if not 
spring.profiles.active=light


