#!/bin/bash

# Simple script to generate API traffic to FinManApp for testing Datadog APM
# This version doesn't require authentication

BASE_URL="http://localhost:8080"
ITERATIONS=30

echo "Starting simple traffic generation to FinManApp..."
echo "Will make requests $ITERATIONS times."
echo "Press Ctrl+C to stop."
echo ""

# Main loop to generate traffic
for i in $(seq 1 $ITERATIONS); do
  echo "Iteration $i of $ITERATIONS"
  
  # Try various endpoints that might be available without auth
  echo "GET request to /actuator/health"
  curl -s "$BASE_URL/actuator/health" > /dev/null 2>&1
  
  echo "GET request to /api/health"
  curl -s "$BASE_URL/api/health" > /dev/null 2>&1
  
  echo "GET request to /"
  curl -s "$BASE_URL/" > /dev/null 2>&1
  
  echo "GET request to /api"
  curl -s "$BASE_URL/api" > /dev/null 2>&1
  
  # Try to hit an auth endpoint
  echo "POST request to /api/auth/login"
  curl -s -X POST "$BASE_URL/api/auth/login" \
    -H "Content-Type: application/json" \
    -d '{"email":"test@example.com","password":"password123"}' \
    > /dev/null 2>&1
  
  # Sleep between iterations to spread out the traffic
  sleep 1
done

echo "Simple traffic generation completed."