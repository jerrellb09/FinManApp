#!/bin/bash

# Script to generate transaction-specific traffic to FinManApp for testing Datadog APM

BASE_URL="http://localhost:8080"
ITERATIONS=15

echo "Starting transaction traffic generation to FinManApp..."
echo "Will make requests $ITERATIONS times."
echo "Press Ctrl+C to stop."
echo ""

# Main loop to generate traffic
for i in $(seq 1 $ITERATIONS); do
  echo "Iteration $i of $ITERATIONS"
  
  # Try the endpoint we instrumented in TransactionController
  echo "GET request to /api/transactions"
  curl -s "$BASE_URL/api/transactions" > /dev/null 2>&1
  
  echo "GET request to /api/transactions/sync"
  curl -s "$BASE_URL/api/transactions/sync" > /dev/null 2>&1
  
  # Sleep between iterations to spread out the traffic
  sleep 2
done

echo "Transaction traffic generation completed."