#!/bin/bash

# Simple script to check if the PostgreSQL database is ready and contains the tradingbotv2 database

if ! command -v docker &> /dev/null; then
    echo "Docker is not installed or not in PATH."
    exit 1
fi

# Check if PostgreSQL container is running
if ! docker ps | grep -q tradingbotv2-postgres; then
    echo "PostgreSQL container is not running."
    exit 1
fi

# Check if PostgreSQL is accepting connections
if ! docker exec tradingbotv2-postgres pg_isready -U postgres; then
    echo "PostgreSQL is not ready to accept connections."
    exit 1
fi

# Check if the database exists
if ! docker exec tradingbotv2-postgres psql -U postgres -lqt | cut -d \| -f 1 | grep -qw tradingbotv2; then
    echo "Database 'tradingbotv2' does not exist. Creating it now..."
    docker exec tradingbotv2-postgres psql -U postgres -c "CREATE DATABASE tradingbotv2 WITH OWNER postgres;"
    echo "Database created."
else
    echo "Database 'tradingbotv2' exists."
fi

echo "PostgreSQL is ready with database 'tradingbotv2'."
exit 0