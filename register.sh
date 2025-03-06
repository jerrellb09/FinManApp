#!/bin/bash

# Register a new user
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "drdoompaid13@gmail.com",
    "password": "password",
    "firstName": "Dr",
    "lastName": "Doom"
  }'

echo -e "\n\nNow trying to login:"
# Try to login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "drdoompaid13@gmail.com",
    "password": "password"
  }'