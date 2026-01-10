# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Event-driven architecture PoC demonstrating modern microservices patterns:
- **Event-driven messaging** with RabbitMQ (including Dead Letter Queue for failure handling)
- **Clean Architecture** following ports and adapters pattern (Bancolombia plugin)
- **Reactive programming** with Project Reactor (non-blocking I/O)
- **Observability** via Prometheus/Grafana metrics
- **DevSecOps** with optimized Docker images and Trivy security scanning
- **Container-first** approach using Podman

**Use case**: Order management system that publishes events to RabbitMQ, processes them asynchronously, and handles failures gracefully. Orders with even IDs intentionally fail to demonstrate DLQ behavior.

Base package: `co.com.thechaoscompany`

## Local Development Setup

### Prerequisites
- Java 17
- Gradle 8.13
- Podman (or Docker)
- PowerShell (for Windows scripts) or bash

**Note**: On Unix systems, make gradlew executable: `chmod +x gradlew`

### 1. Start RabbitMQ (first time)
```bash
podman run -d --restart always --name rabbitmq \
  -p 5672:5672 -p 15672:15672 \
  rabbitmq:3.9-management
```

Access management UI: http://localhost:15672 (guest/guest)

### 2. Start RabbitMQ (subsequent times)
```bash
podman start rabbitmq
```

### 3. Start Prometheus (optional, for observability)
```bash
podman run -d --name prometheus \
  -p 9090:9090 \
  -v "$(pwd)/prometheus.yml:/etc/prometheus/prometheus.yml:ro" \
  prom/prometheus
```

### 4. Start Grafana (optional, for observability)
```bash
podman run -d --name grafana \
  -p 3000:3000 \
  grafana/grafana
```

Configure Grafana data source: http://localhost:3000 (admin/admin), use `http://host.containers.internal:9090` for Prometheus URL.

## Build & Development Commands

### Build
```bash
./gradlew clean build
```

### Run tests
```bash
./gradlew test
```

### Run single test
```bash
./gradlew :module-name:test --tests ClassName.methodName
# Example: ./gradlew :usecase:test --tests OrderUseCaseTest.shouldPublishOrder
```

### Code coverage (Jacoco)
```bash
./gradlew jacocoMergedReport
# Report: build/reports/jacocoMergedReport/html/index.html
```

### Mutation testing (PIT)
```bash
./gradlew pitest
# Aggregated report: build/reports/pitest/index.html
```

### Run application locally

**Option 1: Run from Gradle**
```bash
./gradlew :app-service:bootRun
```

**Option 2: Run built JAR**
```bash
./gradlew clean build
java -jar applications/app-service/build/libs/poc-rabbitmq.jar
```

Application will be available at: http://localhost:8080

### Test the application
```bash
# Publish an order (will succeed - odd ID)
curl -X POST http://localhost:8080/order/publish \
  -H "Content-Type: application/json" \
  -d '{"customerId":"CUST001","productId":"PROD123","quantity":5}'

# Check actuator health
curl http://localhost:8080/actuator/health

# Check metrics
curl http://localhost:8080/actuator/prometheus
```

### Build and run with Podman

**Using multi-stage Dockerfile (root)**
```bash
podman build -t poc-rabbitmq-img .
podman run --name poc-rabbitmq-cnt -p 8080:8080 poc-rabbitmq-img
```

**Using deployment Dockerfile (pre-built JAR)**

PowerShell:
```powershell
./gradlew clean build
Move-Item -Path "applications/app-service/build/libs/poc-rabbitmq.jar" -Destination "deployment/" -Force
cd deployment
podman build -t poc-rabbitmq-img .
podman run --name poc-rabbitmq-cnt -p 8080:8080 poc-rabbitmq-img
```

Bash:
```bash
./gradlew clean build
mv -f applications/app-service/build/libs/poc-rabbitmq.jar deployment/
cd deployment
podman build -t poc-rabbitmq-img .
podman run --name poc-rabbitmq-cnt -p 8080:8080 poc-rabbitmq-img
```

**Cleanup containers**
```bash
podman rm poc-rabbitmq-cnt
podman rmi poc-rabbitmq-img
```

### Security scanning with Trivy
```bash
trivy image localhost/poc-rabbitmq-img
```

## Architecture

### Clean Architecture Structure
- **domain/model**: Domain entities and gateway interfaces (ports)
  - `Order` entity
  - `OrderRepository` gateway interface
- **domain/usecase**: Business logic
  - `OrderUseCase`: coordinates publishing orders via repository gateway
- **infrastructure/driven-adapters**: Implementations of outbound ports
  - `async-event-bus`: RabbitMQ publisher (`OrderEventPublisher` implements `OrderRepository`)
- **infrastructure/entry-points**: Inbound adapters
  - `reactive-web`: REST API controllers (`ApiRest`)
  - `async-event-handler`: RabbitMQ listeners (`OrderEventListener`)
- **infrastructure/helpers**: Cross-cutting concerns
  - `micrometer`: Observability metrics
- **applications/app-service**: Main application and configuration
  - `MainApplication.java`: Spring Boot entrypoint with `@EnableRabbit`
  - `UseCasesConfig.java`: Use case bean configuration

### Event Flow
1. **HTTP Request**: REST endpoint `POST /order/publish` receives Order JSON
   - Assigns random ID, current timestamp, and "PENDING" status
2. **Use Case**: `OrderUseCase` delegates to `OrderRepository` gateway interface
3. **Event Publishing**: `OrderEventPublisher` (implements gateway) serializes Order to JSON and sends to RabbitMQ
   - Exchange: `orders.exchange`
   - Routing key: `order.created`
4. **Event Consumption**: `OrderEventListener` consumes from `orders.queue`
   - Sleeps 5 seconds (simulates processing)
   - **Success**: Odd order IDs are processed successfully and logged
   - **Failure**: Even order IDs throw RuntimeException
5. **DLQ Handling**: Failed messages route to Dead Letter Queue (`orders.dlq`)
   - `OrderEventListener.handleDeadMessage()` processes DLQ messages
   - Sleeps 15 seconds and logs warning

### API Endpoints
- `POST /order/publish` - Publish a new order event
- `GET /actuator/health` - Health check endpoint
- `GET /actuator/metrics` - Available metrics
- `GET /actuator/prometheus` - Prometheus-formatted metrics

### RabbitMQ Configuration
- Exchange: `orders.exchange`
- Queue: `orders.queue`
- Routing key: `order.created`
- DLX: `orders.dlx`
- DLQ: `orders.dlq`
- DLQ routing key: `order.failed`

Configuration in `application.yaml` uses `RABBITMQ_HOST` environment variable (defaults to `localhost`).

## Testing Strategy

- All modules use JUnit 5 with Project Reactor test support
- PIT mutation testing configured for `co.com.thechaoscompany.*`
- Jacoco coverage aggregated across all modules
- Architecture tests in `applications/app-service/src/test/java/co/com/thechaoscompany/ArchitectureTest.java`

## Observability

- Actuator endpoints exposed: `health`, `info`, `metrics`, `prometheus`
- Prometheus scrapes `/actuator/prometheus` at port 8080
- Grafana configured to use Prometheus as data source
- Application tagged as `poc-rabbitmq` in metrics

## Multi-module Gradle Setup

Modules defined in `settings.gradle`:
- `:app-service` → `applications/app-service`
- `:model` → `domain/model`
- `:usecase` → `domain/usecase`
- `:async-event-bus` → `infrastructure/driven-adapters/async-event-bus`
- `:reactive-web` → `infrastructure/entry-points/reactive-web`
- `:async-event-handler` → `infrastructure/entry-points/async-event-handler`
- `:micrometer` → `infrastructure/helpers/micrometer`

Dependencies managed centrally in `main.gradle` with Spring Boot BOM.

## Key Dependencies

- Java 17
- Spring Boot 3.4.4
- Project Reactor (reactive streams)
- Spring AMQP (RabbitMQ)
- Lombok for boilerplate reduction
- Micrometer for metrics
- Log4j2 for logging

## Docker & Security

### Container Tool
This project uses **Podman** (not Docker) as the primary container runtime. All commands use `podman`, but you can substitute with `docker` if needed.

### Two Dockerfile Approaches

**1. Root `Dockerfile`: Multi-stage build**
- Stage 1: Uses `gradle:8.13-jdk17-alpine` to build the application from source
- Stage 2: Creates minimal runtime image with compiled JAR
- Best for CI/CD pipelines or when you don't have Gradle installed locally
- Command: `podman build -t poc-rabbitmq-img .`

**2. `deployment/Dockerfile`: Runtime-only**
- Expects pre-built JAR file (`poc-rabbitmq.jar`) in deployment directory
- Single-stage build, faster for local development
- Use after building with Gradle: `./gradlew clean build`
- Command: `podman build -t poc-rabbitmq-img deployment/`

### Security Features
- **Minimal base image**: Eclipse Temurin Alpine (17.0.14_7-jdk-alpine)
- **Non-root user**: Runs as `appuser` (UID/GID managed by Alpine)
- **Container-optimized JVM**: `-XX:+UseContainerSupport -XX:MaxRAMPercentage=70`
- **Vulnerability scanning**: Trivy integration for security analysis
- **Multi-stage builds**: Reduces final image size and attack surface
- **`.dockerignore`**: Prevents sensitive files from being copied into images

## Environment Configuration

Key environment variables:
- `RABBITMQ_HOST`: RabbitMQ host (default: `localhost`, Docker: `host.containers.internal`)
- `RABBITMQ_PORT`: RabbitMQ port (default: `5672`)
- `JAVA_OPTS`: JVM options for container optimization

## Common Development Tasks

### View RabbitMQ Queues and Messages
Access the RabbitMQ Management UI at http://localhost:15672 (guest/guest) to:
- Monitor queue depths
- View messages in `orders.queue` and `orders.dlq`
- Inspect exchange bindings
- Purge queues for testing

### Check Application Logs
When running with Gradle or JAR, logs output to console. When running in Podman:
```bash
podman logs -f poc-rabbitmq-cnt
```

### Troubleshooting

**Connection refused to RabbitMQ**
- Verify RabbitMQ is running: `podman ps | grep rabbitmq`
- Check connection: `telnet localhost 5672`
- When running app in container, use `RABBITMQ_HOST=host.containers.internal`

**Tests failing**
- Ensure no conflicting process on port 8080
- Check Java version: `java -version` (should be 17)
- Clean build: `./gradlew clean build --refresh-dependencies`

**Gradlew permission denied**
```bash
chmod +x gradlew
```

### Working with the Bancolombia Plugin

This project uses the Bancolombia Clean Architecture Gradle plugin. Useful tasks:
```bash
# Validate architecture structure
./gradlew validateStructure

# Generate new use case
./gradlew generateUseCase --name=NewUseCaseName

# Generate new model
./gradlew generateModel --name=NewModelName

# Generate new driven adapter
./gradlew generateDrivenAdapter --type=asynceventbus
```

See plugin documentation: https://github.com/bancolombia/scaffold-clean-architecture
