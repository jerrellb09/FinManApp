#!/bin/bash

# Test all authentication endpoints

# Ensure the app is running
if ! curl -s http://localhost:8080/api/auth/check >/dev/null; then
  echo "ERROR: Application doesn't seem to be running. Start it with ./mvnw spring-boot:run"
  exit 1
fi

# Step 1: Try unauthenticated access to check endpoint
echo "Checking if auth endpoints are accessible..."
curl -s http://localhost:8080/api/auth/check | jq .

# Step 2: Try to register a new user
echo -e "\nAttempting to register a new test user..."
REGISTER_RESPONSE=$(curl -s -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "newuser@example.com",
    "password": "password",
    "firstName": "New",
    "lastName": "User"
  }')
echo $REGISTER_RESPONSE | jq .

# Extract token from register response if available
REG_TOKEN=$(echo $REGISTER_RESPONSE | grep -o '"token":"[^"]*"' | sed 's/"token":"//;s/"//')

if [ -n "$REG_TOKEN" ]; then
  echo -e "\nRegistration successful, got token"
  
  # Step 3: Try authenticated access with registration token
  echo -e "\nTesting token from registration with whoami endpoint..."
  curl -s -X GET http://localhost:8080/api/auth/whoami \
    -H "Authorization: Bearer $REG_TOKEN" | jq .
else
  echo -e "\nNo token from registration, trying login"
fi

# Step 4: Try to login with test@example.com
echo -e "\nLogging in with test@example.com..."
LOGIN_RESPONSE=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password"
  }')
echo $LOGIN_RESPONSE | jq .

# Extract token
LOGIN_TOKEN=$(echo $LOGIN_RESPONSE | grep -o '"token":"[^"]*"' | sed 's/"token":"//;s/"//')

if [ -n "$LOGIN_TOKEN" ]; then
  echo -e "\nLogin successful, got token"
  
  # Step 5: Test token with whoami
  echo -e "\nTesting token with whoami endpoint..."
  curl -s -X GET http://localhost:8080/api/auth/whoami \
    -H "Authorization: Bearer $LOGIN_TOKEN" | jq .
  
  # Step 6: Test accounts endpoint
  echo -e "\nTesting accounts endpoint with token..."
  curl -s -X GET http://localhost:8080/api/accounts \
    -H "Authorization: Bearer $LOGIN_TOKEN" | jq .
else
  echo -e "\nNo token from login, authentication failing"
fi

echo -e "\nTest complete!"