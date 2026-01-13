# Spring Boot Custom Metrics Demo

This project demonstrates how to capture custom metrics in a Spring Boot application using Micrometer and visualize them with Prometheus and Grafana.

## Prerequisites

- Java 21
- Docker & Docker Compose
- Maven (optional, mvnw wrapper is usually preferred but this project assumes installed maven or you can generate wrapper)

## Project Structure

- `src/main/java`: Spring Boot Application code
- `src/main/resources`: Configuration
- `docker-compose.yml`: Prometheus and Grafana setup
- `prometheus/prometheus.yml`: Prometheus configuration

## Getting Started

### 1. Build and Run with Docker

This method runs both the application and the monitoring stack in Docker.

```bash
docker-compose up -d --build
```
This starts:
- **App**: http://localhost:8080
- **Prometheus**: http://localhost:9090
- **Grafana**: http://localhost:3000

Verify app is running:
`curl localhost:8080/actuator/prometheus`

### 2. Manual / Local Run (Alternative)

If you prefer to run the Java app locally:
```bash
mvn spring-boot:run
```
(Ensure you update `prometheus/prometheus.yml` to point to `localhost:8080` if running this way).

### 3. Generate Metrics

Generate some specific custom metrics by hitting the API endpoints:

**Process Request (Counter & Timer)**
```bash
# Simulate a 500ms process
curl -X POST "http://localhost:8080/api/metrics/process?durationMs=500"

# Run multiple times to generate data
for i in {1..10}; do curl -X POST "http://localhost:8080/api/metrics/process?durationMs=$((RANDOM % 500))"; done
```

**Queue Metrics (Gauge)**
```bash
# Add to queue (increase gauge)
curl -X POST http://localhost:8080/api/metrics/queue/add

# Remove from queue (decrease gauge)
curl -X POST http://localhost:8080/api/metrics/queue/remove
```

### 4. Configure Grafana

1. **Login** to Grafana (http://localhost:3000).
2. **Add Data Source**:
   - Go to **Connections** > **Data Sources** > **Add data source**.
   - Select **Prometheus**.
   - Set URL to `http://prometheus:9090`.
   - Click "Save & Test".
3. **Create Dashboard**:
   - **Request Count**: Use query `custom_request_count_total` (Rate/Increase).
   - **Process Duration**: Use query `rate(custom_process_duration_seconds_sum[1m]) / rate(custom_process_duration_seconds_count[1m])` for avg duration.
   - **Queue Size**: Use query `custom_queue_size`.

## Metrics Explained

- `custom_request_count`: Counter that increments on every request.
- `custom_process_duration`: Timer that records how long the process takes.
- `custom_queue_size`: Gauge that tracks the current number of items (simulated).