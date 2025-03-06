#!/bin/bash

# This script provides a clean setup for the PostgreSQL database

# Check if Docker is installed
if ! command -v docker &> /dev/null; then
    echo "Docker is not installed or not in PATH."
    echo "Please install Docker or use H2 database instead."
    echo "To use H2: ./mvnw spring-boot:run -Dspring.profiles.active=h2"
    exit 1
fi

echo "Setting up PostgreSQL for TradingBotV2..."

# Check if the container is already running
if docker ps | grep -q tradingbotv2-postgres; then
    echo "PostgreSQL container is already running. Stopping it..."
    docker stop tradingbotv2-postgres
fi

# Remove existing container if it exists
if docker ps -a | grep -q tradingbotv2-postgres; then
    echo "Removing existing PostgreSQL container..."
    docker rm tradingbotv2-postgres
fi

echo "Creating new PostgreSQL container with tradingbotv2 database..."
docker run --name tradingbotv2-postgres \
    -e POSTGRES_USER=postgres \
    -e POSTGRES_PASSWORD=postgres \
    -e POSTGRES_DB=tradingbotv2 \
    -p 5432:5432 \
    -d postgres:14-alpine

echo "Waiting for PostgreSQL to initialize..."
# More reliable way to wait for PostgreSQL to be ready
for i in {1..30}; do
    if docker exec tradingbotv2-postgres pg_isready -U postgres &>/dev/null; then
        echo "PostgreSQL is ready!"
        break
    fi
    echo "Waiting for PostgreSQL to start... ($i/30)"
    sleep 1
    
    if [ $i -eq 30 ]; then
        echo "Timed out waiting for PostgreSQL to start."
        echo "Please check Docker logs: docker logs tradingbotv2-postgres"
        exit 1
    fi
done

echo -e "\n✅ PostgreSQL setup complete!"
echo "   Database: tradingbotv2"
echo "   Username: postgres"
echo "   Password: postgres"
echo "   Port: 5432"
echo -e "\nYou can now run the application with:"
echo -e "   ./mvnw spring-boot:run\n"