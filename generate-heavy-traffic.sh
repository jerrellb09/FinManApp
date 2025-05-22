#!/bin/bash

# Script to generate intensive traffic to FinManApp for testing Datadog APM
# This will create more traffic with different endpoints and patterns

BASE_URL="http://localhost:8080"
ITERATIONS=50
DELAY=0.5  # Delay between requests in seconds

echo "Starting intensive traffic generation to FinManApp..."
echo "Will make various requests $ITERATIONS times with $DELAY second delay between requests."
echo "Press Ctrl+C to stop."
echo ""

# Function to make a GET request with random query parameters
make_get_request() {
  local endpoint=$1
  local rand=$RANDOM
  
  # Add some randomized query parameters to create different traces
  curl -s "$BASE_URL$endpoint?param=$rand&ts=$(date +%s)" > /dev/null 2>&1
  echo "GET $endpoint (rand=$rand)"
}

# Function to make a POST request with random data
make_post_request() {
  local endpoint=$1
  local rand=$RANDOM
  
  # Create a random payload
  curl -s -X POST "$BASE_URL$endpoint" \
    -H "Content-Type: application/json" \
    -d "{\"id\":$rand,\"timestamp\":\"$(date +%s)\",\"value\":$RANDOM}" > /dev/null 2>&1
  
  echo "POST $endpoint (rand=$rand)"
}

# Main loop to generate traffic
for i in $(seq 1 $ITERATIONS); do
  echo "Iteration $i of $ITERATIONS"
  
  # Make various GET requests
  make_get_request "/api/transactions"
  sleep $DELAY
  
  make_get_request "/api/transactions/sync"
  sleep $DELAY
  
  # Try other endpoints
  make_get_request "/api/health" 
  sleep $DELAY
  
  make_get_request "/api/auth/health"
  sleep $DELAY
  
  # Add some POST requests
  make_post_request "/api/auth/login"
  sleep $DELAY
  
  # Every 5 iterations, make a burst of requests
  if [ $((i % 5)) -eq 0 ]; then
    echo "Making burst of requests..."
    for j in {1..10}; do
      make_get_request "/api/transactions?burst=$j"
      sleep 0.1
    done
  fi
  
  echo "--------------------------"
done

echo "Intensive traffic generation completed."