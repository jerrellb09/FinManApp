#!/bin/bash

echo "Building FinManApp with Maven..."
./mvnw clean package -DskipTests

echo "Starting the application..."
java -jar target/finmanapp-0.0.1-SNAPSHOT.jar