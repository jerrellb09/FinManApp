#!/bin/bash

echo "Testing application with H2 in-memory database"
echo "=============================================="

echo "Running with H2 profile for 30 seconds..."
timeout 30s ./mvnw spring-boot:run -Dspring.profiles.active=h2

if [ $? -eq 124 ]; then
  # Timeout (which is good, means the app was running)
  echo "✅ Application started successfully with H2 database"
  echo "   (Timeout after 30 seconds is expected)"
else
  echo "❌ Application failed to start with H2 database"
fi

echo ""
echo "Test completed."
echo "=================================================="