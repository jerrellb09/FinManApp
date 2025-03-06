#!/bin/bash

EMAIL=${1:-"test@example.com"}
PASSWORD=${2:-"password"}

echo "Logging in with $EMAIL..."

TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"$EMAIL\",\"password\":\"$PASSWORD\"}" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)

if [ -z "$TOKEN" ]; then
  echo "Login failed! No token received."
  exit 1
fi

echo "Token: $TOKEN"
echo 
echo "Testing authentication with this token..."
echo 

curl -s -X GET http://localhost:8080/api/auth/whoami \
  -H "Authorization: Bearer $TOKEN" | jq .

echo
echo "Testing to get user accounts..."
echo

curl -s -X GET http://localhost:8080/api/accounts \
  -H "Authorization: Bearer $TOKEN" | jq .

echo 
echo "To use this token in other requests:"
echo "curl -H \"Authorization: Bearer $TOKEN\" http://localhost:8080/api/..."