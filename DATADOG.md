# Datadog APM Integration for FinManApp

This document provides instructions for setting up and using Datadog APM with the FinManApp to monitor application performance and track logs.

## Setup Instructions

### 1. Prerequisites

- A Datadog account with an API key (already configured as: 302ca559505fa3776bbf78ffdf80603b10e50ac9)
- Docker installed on your system for running the Datadog Agent

### 2. Docker Setup

The application is configured to work with Datadog Agent running in a Docker container. The Datadog Agent will be automatically started if not already running when you use the run script.

If you want to manually start the Datadog Agent in Docker:

```bash
docker run -d --name dd-agent \
  -v /var/run/docker.sock:/var/run/docker.sock:ro \
  -e DD_API_KEY=302ca559505fa3776bbf78ffdf80603b10e50ac9 \
  -e DD_APM_ENABLED=true \
  -e DD_APM_NON_LOCAL_TRAFFIC=true \
  -p 8126:8126 \
  datadog/agent:latest
```

### 3. Running the Application with Datadog APM

Use the provided script to run the application with Datadog APM enabled:

```bash
./run-with-datadog.sh
```

This script will:
1. Download the Datadog Java Agent if not present
2. Set the necessary JVM options for Datadog APM
3. Start the application with APM tracing enabled

### 4. Maven Configuration

For development, you can also set up Maven to include the Datadog agent when running the application:

```bash
MAVEN_OPTS="-javaagent:/path/to/dd-java-agent.jar -Ddd.service.name=finmanapp -Ddd.env=dev" ./mvnw spring-boot:run
```

## Features

The FinManApp has been integrated with Datadog APM to provide the following features:

### 1. Distributed Tracing

- Automatic tracing for all HTTP requests and database queries
- Custom tracing for specific operations using the `TracingUtil` class
- Span tags for contextual information

### 2. Log Management

- Structured JSON logging using Logback
- Trace correlation between logs and APM traces
- Log injection for distributed tracing across systems

### 3. Custom Metrics

- Runtime metrics for JVM monitoring
- Custom business metrics for transactions and user activities

## Usage

### Automatic Tracing

Most operations are automatically traced, including:
- HTTP requests to controllers
- Database queries
- External API calls (Plaid, etc.)

### Manual Tracing

For more fine-grained control, you can use the `TracingUtil` class:

```java
try (Scope scope = TracingUtil.startSpan("custom.operation")) {
    // Your code here
    scope.span().setTag("custom.tag", "value");
    
    // Do something
    
    // Add result information
    scope.span().setTag("result.status", "success");
}
```

### Logging with Trace Correlation

Use the `LoggingService` for logging with automatic trace correlation:

```java
LoggingService logger = new LoggingService(YourClass.class);
logger.info("This log is correlated with the current trace");
logger.error("An error occurred", exception);
```

## Best Practices

1. **Naming Conventions**
   - Use consistent naming for services (`dd.service.name=finmanapp`)
   - Use descriptive operation names for spans (e.g., "transaction.sync")

2. **Tags and Metadata**
   - Add relevant business context as tags (user IDs, account IDs, etc.)
   - Use standard tag names when possible (e.g., `http.status_code`)

3. **Error Handling**
   - Mark spans as errors when exceptions occur
   - Include error details in span logs

4. **Log Levels**
   - Use appropriate log levels (DEBUG, INFO, WARN, ERROR)
   - Ensure sensitive information is not logged

## Viewing in Datadog

Once your application is running with Datadog APM enabled, you can view the data in your Datadog dashboard:

1. **APM Traces**: Navigate to APM > Traces in your Datadog dashboard
2. **Logs**: Navigate to Logs > Search in your Datadog dashboard
3. **Metrics**: Navigate to Metrics > Explorer in your Datadog dashboard

## Troubleshooting

If you don't see traces or logs in Datadog:

1. Verify your API key is correct
2. Check that the Datadog Agent is running
3. Ensure network connectivity between your application and Datadog
4. Check Java Agent logs for any errors

For more information, consult the Datadog documentation:
- APM: https://docs.datadoghq.com/tracing/
- Logs: https://docs.datadoghq.com/logs/