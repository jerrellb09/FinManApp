# FinManApp Observability Stack

This document describes the observability setup for FinManApp using OpenTelemetry, Prometheus, and Grafana.

## Overview

The application uses:
- **OpenTelemetry**: For distributed tracing and metrics collection
- **Prometheus**: For metrics storage and querying  
- **Grafana**: For visualization and alerting
- **OpenTelemetry Collector**: For receiving, processing, and exporting telemetry data

## Quick Start

### Running with Docker Compose

```bash
# Start the full stack
docker-compose up -d

# View logs
docker-compose logs -f finmanapp
```

### Access Points

- **Application**: http://localhost:8080
- **Grafana**: http://localhost:3000 (admin/admin)
- **Prometheus**: http://localhost:9090
- **OpenTelemetry Collector Metrics**: http://localhost:9464/metrics

## Configuration

### Application Properties

Key OpenTelemetry configuration in `application-prod.properties`:

```properties
# OpenTelemetry Configuration
otel.service.name=finmanapp
otel.service.version=1.0.0
otel.resource.attributes=environment=prod
otel.exporter.otlp.endpoint=http://localhost:4317

# Prometheus metrics
management.endpoints.web.exposure.include=prometheus,health,info
management.endpoint.prometheus.enabled=true
management.metrics.export.prometheus.enabled=true
```

### Docker Environment Variables

The FinManApp service is configured with:

```yaml
environment:
  - OTEL_EXPORTER_OTLP_ENDPOINT=http://otel-collector:4317
  - OTEL_SERVICE_NAME=finmanapp
  - OTEL_SERVICE_VERSION=0.0.1
  - OTEL_RESOURCE_ATTRIBUTES=environment=dev
```

## Architecture

```
┌─────────────┐    ┌──────────────────┐    ┌─────────────┐
│  FinManApp  │───▶│ OpenTelemetry    │───▶│ Prometheus  │
│             │    │ Collector        │    │             │
└─────────────┘    └──────────────────┘    └─────────────┘
       │                     │                     │
       │                     │                     │
       ▼                     ▼                     ▼
┌─────────────┐    ┌──────────────────┐    ┌─────────────┐
│   Logs      │    │   Jaeger         │    │   Grafana   │
│   (stdout)  │    │   (Optional)     │    │   Dashboard │
└─────────────┘    └──────────────────┘    └─────────────┘
```

## Features

### Automatic Instrumentation

The application automatically captures:
- HTTP requests and responses
- Database queries (via Spring Boot auto-configuration)
- Method-level tracing in controllers and services
- JVM metrics
- Custom business metrics

### Manual Instrumentation

Custom tracing is available via the `TracingUtil` service:

```java
@Autowired
private TracingUtil tracingUtil;

public void someMethod() {
    Span span = tracingUtil.startSpan("custom.operation");
    try (Scope scope = span.makeCurrent()) {
        span.setAttribute("user.id", "123");
        // Your business logic here
        span.setStatus(StatusCode.OK);
    } catch (Exception e) {
        tracingUtil.recordException(span, e);
        throw e;
    } finally {
        span.end();
    }
}
```

### Structured Logging

Logs include trace correlation IDs via the `LoggingService`:

```java
@Autowired
private LoggingService loggingService;

public void someMethod() {
    loggingService.info("Processing transaction for user: {}", userId);
    // Automatically includes trace.id and span.id in log output
}
```

## Metrics Available

### Application Metrics
- HTTP request duration and count
- Database query performance
- Transaction processing metrics
- Account synchronization metrics
- Business-specific counters

### JVM Metrics
- Memory usage
- Garbage collection
- Thread counts
- CPU usage

### Custom Metrics
- Transaction sync success/failure rates
- User authentication rates
- API endpoint performance

## Dashboards

Grafana comes pre-configured with:
- Prometheus data source
- Basic application monitoring dashboard
- JVM metrics dashboard

### Creating Custom Dashboards

1. Access Grafana at http://localhost:3000
2. Use admin/admin to log in
3. Create new dashboard
4. Add panels with Prometheus queries

Example queries:
```promql
# Request rate
rate(http_server_requests_total[5m])

# Error rate
rate(http_server_requests_total{status=~"5.."}[5m])

# Response time 95th percentile
histogram_quantile(0.95, rate(http_server_requests_duration_seconds_bucket[5m]))
```

## Troubleshooting

### Common Issues

1. **Metrics not appearing**: Check that actuator endpoints are exposed
2. **Traces not visible**: Verify OTLP collector is running and endpoint is correct
3. **High memory usage**: Adjust batch processing settings in otel-collector-config.yaml

### Debugging

View OpenTelemetry Collector logs:
```bash
docker-compose logs otel-collector
```

Check Prometheus targets:
```bash
curl http://localhost:9090/api/v1/targets
```

Test metrics endpoint:
```bash
curl http://localhost:8080/actuator/prometheus
```

## Migration Notes

This setup replaces the previous Datadog integration. Key changes:

1. **Dependencies**: Replaced Datadog libraries with OpenTelemetry
2. **Configuration**: New environment variables and properties
3. **APIs**: Updated from OpenTracing to OpenTelemetry APIs
4. **Deployment**: Docker Compose now includes observability stack

### Removed Components
- Datadog Agent container
- `dd-java-agent.jar`
- Datadog-specific configuration
- `DatadogConfig.java` class
- `run-with-datadog.sh` script

### Added Components
- OpenTelemetry Collector
- Prometheus server  
- Grafana dashboard
- New `ObservabilityConfig.java` class
- Updated `TracingUtil.java` and `LoggingService.java`

## Performance Impact

The observability stack is designed to be lightweight:
- Metrics collection: ~1-2% CPU overhead
- Tracing: ~2-3% latency overhead
- Memory: ~50MB additional for full stack

For production, consider:
- Sampling traces (not all requests need to be traced)
- Adjusting metric collection intervals
- Using dedicated observability infrastructure