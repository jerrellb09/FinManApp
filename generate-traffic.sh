#!/bin/bash

# Script to generate API traffic to FinManApp for testing Datadog APM

BASE_URL="http://localhost:8080"
ITERATIONS=10

# Function to make a request and log it
make_request() {
  local method=$1
  local endpoint=$2
  local payload=$3
  local description=$4
  
  echo "Making $method request to $endpoint: $description"
  
  if [ -z "$payload" ]; then
    # GET request with no payload
    curl -s -X "$method" "$BASE_URL$endpoint" -H "Content-Type: application/json" > /dev/null
  else
    # Request with payload
    curl -s -X "$method" "$BASE_URL$endpoint" -H "Content-Type: application/json" -d "$payload" > /dev/null
  fi
  
  echo "✓ $method request to $endpoint completed"
}

# Check if the application is running
if ! curl -s "$BASE_URL/actuator/health" > /dev/null 2>&1; then
  echo "Error: Application doesn't seem to be running at $BASE_URL"
  echo "Make sure your application is running with ./run-with-datadog.sh before running this script."
  exit 1
fi

echo "Starting traffic generation to FinManApp..."
echo "Will make various API requests $ITERATIONS times."
echo "Press Ctrl+C to stop."
echo ""

# Create a temporary user for auth
echo "Creating temporary user for authentication..."
register_payload='{"email":"test@example.com","password":"Password123!","firstName":"Test","lastName":"User"}'
curl -s -X POST "$BASE_URL/api/auth/register" -H "Content-Type: application/json" -d "$register_payload" > /dev/null

# Login to get a token
echo "Logging in to get authentication token..."
login_payload='{"email":"test@example.com","password":"Password123!"}'
TOKEN=$(curl -s -X POST "$BASE_URL/api/auth/login" -H "Content-Type: application/json" -d "$login_payload" | grep -o '"token":"[^"]*' | cut -d'"' -f4)

if [ -z "$TOKEN" ]; then
  echo "Failed to get authentication token. Using unauthenticated endpoints only."
  AUTH_HEADER=""
else
  echo "Successfully authenticated."
  AUTH_HEADER="Authorization: Bearer $TOKEN"
fi

# Main loop to generate traffic
for i in $(seq 1 $ITERATIONS); do
  echo "Iteration $i of $ITERATIONS"
  
  # Public/unauthenticated endpoints
  make_request "GET" "/api/auth/health" "" "Health check"
  
  if [ ! -z "$AUTH_HEADER" ]; then
    # Authenticated endpoints
    make_request "GET" "/api/transactions" "" "Get transactions" "-H \"$AUTH_HEADER\""
    make_request "GET" "/api/users/profile" "" "Get user profile" "-H \"$AUTH_HEADER\""
    
    # Create a transaction
    TRANSACTION_PAYLOAD='{"accountId":1,"amount":25.99,"description":"Test Transaction","date":"2025-05-21T12:00:00","categoryId":1}'
    make_request "POST" "/api/transactions" "$TRANSACTION_PAYLOAD" "Create transaction" "-H \"$AUTH_HEADER\""
    
    # Get categories
    make_request "GET" "/api/categories" "" "Get categories" "-H \"$AUTH_HEADER\""
    
    # Sync transactions
    make_request "GET" "/api/transactions/sync" "" "Sync transactions" "-H \"$AUTH_HEADER\""
  fi
  
  # Sleep between iterations to spread out the traffic
  sleep 2
done

echo "Traffic generation completed."