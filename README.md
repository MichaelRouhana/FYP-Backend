# Project – Production Setup

This repository provides a **Docker-based local development environment** for the backend application and its supporting services.

---

## Services Overview

| Service           | Container         | Image / Build         | Description                | Ports           | Dependencies              |
| ----------------- | ----------------- | --------------------- | -------------------------- | --------------- | ------------------------- |
| **App**           | `spring-boot-app` | Dockerfile            | Spring Boot backend API    | `8080`          | MySQL, Logstash, RabbitMQ |
| **MySQL**         | `mysql-db`        | mysql:8.0             | Application database       | `3306`          | —                         |
| **RabbitMQ**      | `rabbitmq`        | rabbitmq:3-management | Async messaging & queues   | `5672`, `15672` | —                         |
| **Logstash**      | `logstash`        | logstash:8.6.0        | Log ingestion & processing | `5044`          | Elasticsearch             |
| **Elasticsearch** | `elasticsearch`   | elasticsearch:8.6.0   | Log storage & search       | `9200`          | —                         |
| **Kibana**        | `kibana`          | kibana:8.6.0          | Log visualization UI       | `5601`          | Elasticsearch             |

---

## Prerequisites

Make sure the following are installed:

* **Docker**
* **Docker Compose**

Required files:

* `docker-compose.yml`
* `application.properties`

---

## Running the Application

Start all services:

```bash
docker-compose -f docker-compose.yml up -d
```

Check running containers:

```bash
docker ps
```

View container logs:

```bash
docker logs <container-id>
```

List Docker volumes:

```bash
docker volume ls
```

Stop and remove containers:

```bash
docker-compose -f docker-compose.yml down
```

---

## Database Configuration (MySQL)

**Credentials**

* Username: `root`
* Password: `root`
* Database: `fyp`

---

## Application Configuration

All configuration is defined in `application.properties`.

### API Configuration

```properties
football.api.key=api-key
```

### Email Configuration

```properties
spring.mail.username=example@gmail.com
spring.mail.password=password
```

### Database Configuration

```properties
spring.datasource.url=jdbc:mysql://mysql:3306/fyp
spring.datasource.username=root
spring.datasource.password=root
```

---

## Spring Profiles

Choose the profile depending on logging requirements:

### Enable ELK Logging

```properties
spring.profiles.active=elk
```

### Lightweight Mode (No ELK)

```properties
spring.profiles.active=light
```

---

