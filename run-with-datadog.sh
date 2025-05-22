#!/bin/bash

# Script to run the application with Datadog APM Java agent

echo "Starting application with Datadog APM enabled..."

# Download the Datadog Java agent if not present
AGENT_PATH="dd-java-agent.jar"
if [ ! -f "$AGENT_PATH" ]; then
  echo "Downloading Datadog Java agent..."
  curl -L -o $AGENT_PATH "https://dtdg.co/latest-java-tracer"
fi

# Run the application with the Datadog Java agent
java -javaagent:$AGENT_PATH \
  -Ddd.service.name=finmanapp \
  -Ddd.env=dev \
  -Ddd.trace.enabled=true \
  -Ddd.agent.host=localhost \
  -Ddd.agent.port=8126 \
  -Ddd.site=us5.datadoghq.com \
  -Ddd.profiling.enabled=true \
  -Ddd.logs.injection=true \
  -Ddd.api.key=8f478971895258597575fa486c29ffdf \
  -jar target/FinManApp-0.0.1-SNAPSHOT.jar