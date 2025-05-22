#!/bin/bash

# This script runs the FinManApp application
echo "Starting FinManApp..."

# Run application
./mvnw spring-boot:run > app.log 2>&1 &
PID=$!

echo "Application started with PID: $PID"
echo "Logs are being written to app.log"
echo "To stop the application: kill $PID"
echo "To follow the logs: tail -f app.log"