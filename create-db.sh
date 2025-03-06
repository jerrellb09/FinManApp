#!/bin/bash

# Check if PostgreSQL is installed
if ! command -v psql &> /dev/null; then
    echo "PostgreSQL client is not installed. Please install it first."
    exit 1
fi

# Create database
echo "Creating database 'tradingbotv2'..."
PGPASSWORD=postgres psql -h localhost -U postgres -c "CREATE DATABASE tradingbotv2;" || {
    echo "Failed to create database. Check if PostgreSQL server is running."
    echo "You may need to start it with: brew services start postgresql"
    exit 1
}

echo "Database 'tradingbotv2' created successfully!"
echo "You can now start your application."