#!/bin/bash

echo "Building TradingbotV2 with Maven..."
./mvnw clean package -DskipTests

echo "Starting the application..."
java -jar target/tradingbot-v2-0.0.1-SNAPSHOT.jar