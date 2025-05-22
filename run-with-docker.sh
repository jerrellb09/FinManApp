#!/bin/bash

# Build the application
echo "Building the application..."
./mvnw clean package -DskipTests

# Download the Datadog Java agent if not present
AGENT_PATH="dd-java-agent.jar"
if [ ! -f "$AGENT_PATH" ]; then
  echo "Downloading Datadog Java agent..."
  curl -L -o $AGENT_PATH "https://dtdg.co/latest-java-tracer"
fi

# Run with Docker Compose
echo "Starting all services with Docker Compose..."
docker-compose up --build