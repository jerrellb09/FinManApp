#!/bin/bash

# Simple script to check if the PostgreSQL database is ready and contains the finmanapp database

if ! command -v docker &> /dev/null; then
    echo "Docker is not installed or not in PATH."
    exit 1
fi

# Check if PostgreSQL container is running
if ! docker ps | grep -q finmanapp-postgres; then
    echo "PostgreSQL container is not running."
    exit 1
fi

# Check if PostgreSQL is accepting connections
if ! docker exec finmanapp-postgres pg_isready -U postgres; then
    echo "PostgreSQL is not ready to accept connections."
    exit 1
fi

# Check if the database exists
if ! docker exec finmanapp-postgres psql -U postgres -lqt | cut -d \| -f 1 | grep -qw finmanapp; then
    echo "Database 'finmanapp' does not exist. Creating it now..."
    docker exec finmanapp-postgres psql -U postgres -c "CREATE DATABASE finmanapp WITH OWNER postgres;"
    echo "Database created."
else
    echo "Database 'finmanapp' exists."
fi

echo "PostgreSQL is ready with database 'finmanapp'."
exit 0