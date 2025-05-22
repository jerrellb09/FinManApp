#!/bin/bash

echo "Testing PostgreSQL Connection to finmanapp database"
echo "=================================================="

echo "1. Checking if PostgreSQL container is running..."
if docker ps | grep -q postgres; then
  echo "✅ PostgreSQL container is running"
else
  echo "❌ PostgreSQL container is not running"
  exit 1
fi

echo "2. Checking PostgreSQL connection..."
if docker exec c249f77d98be pg_isready -U postgres; then
  echo "✅ PostgreSQL server is accepting connections"
else
  echo "❌ PostgreSQL server is not accepting connections"
  exit 1
fi

echo "3. Checking if finmanapp database exists..."
if docker exec c249f77d98be psql -U postgres -lqt | cut -d \| -f 1 | grep -qw finmanapp; then
  echo "✅ finmanapp database exists"
else
  echo "❌ finmanapp database does not exist"
  echo "Creating finmanapp database..."
  docker exec c249f77d98be psql -U postgres -c "CREATE DATABASE finmanapp;" && echo "✅ finmanapp database created" || echo "❌ Failed to create finmanapp database"
fi

echo "4. Testing connection to finmanapp database..."
if docker exec c249f77d98be psql -U postgres -d finmanapp -c "SELECT 1;" > /dev/null 2>&1; then
  echo "✅ Successfully connected to finmanapp database"
else
  echo "❌ Failed to connect to finmanapp database"
fi

echo "5. Checking database user permissions..."
docker exec c249f77d98be psql -U postgres -d finmanapp -c "\du"

echo ""
echo "Connection test completed."
echo "=================================================="