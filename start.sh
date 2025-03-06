#!/bin/bash

echo "Starting TradingBotV2 application..."

# Check if Docker is installed
if command -v docker > /dev/null 2>&1 && command -v docker-compose > /dev/null 2>&1; then
    echo "Docker and Docker Compose found. Starting PostgreSQL database..."
    
    # Check if the container is already running
    if [ "$(docker ps -q -f name=tradingbotv2-postgres)" ]; then
        echo "PostgreSQL container is already running."
    else
        # Check if container exists but is stopped
        if [ "$(docker ps -aq -f name=tradingbotv2-postgres)" ]; then
            echo "Starting existing PostgreSQL container..."
            docker start tradingbotv2-postgres
        else
            echo "Creating and starting new PostgreSQL container..."
            docker-compose up -d
        fi
    fi
    
    # Wait for PostgreSQL to be ready
    echo "Waiting for PostgreSQL to be ready..."
    RETRIES=10
    until docker exec tradingbotv2-postgres pg_isready -U postgres -d postgres || [ $RETRIES -eq 0 ]; do
        echo "Waiting for postgres server to be ready, $RETRIES retries left..."
        RETRIES=$((RETRIES-1))
        sleep 2
    done
    
    if [ $RETRIES -eq 0 ]; then
        echo "Failed to connect to PostgreSQL, falling back to H2 database."
        ./mvnw spring-boot:run -Dspring.profiles.active=h2
        exit 0
    fi
    
    # Ensure the database exists
    echo "Ensuring database 'tradingbotv2' exists..."
    docker exec tradingbotv2-postgres psql -U postgres -c "SELECT 1 FROM pg_database WHERE datname = 'tradingbotv2'" | grep -q 1 || \
    docker exec tradingbotv2-postgres psql -U postgres -c "CREATE DATABASE tradingbotv2"
    
    # Start the application with PostgreSQL
    echo "Starting application with PostgreSQL database..."
    ./mvnw spring-boot:run
else
    echo "Docker or Docker Compose not found. Using H2 in-memory database instead."
    echo "NOTE: Data will be lost when the application stops!"
    
    # Start the application with H2 profile
    echo "Starting application with H2 database..."
    ./mvnw spring-boot:run -Dspring.profiles.active=h2
fi