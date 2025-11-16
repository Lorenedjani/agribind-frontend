#!/bin/bash
# scripts/start.sh

echo "Building Notification Service..."
mvn clean package -DskipTests

echo "Starting Docker containers..."
docker-compose -f docker/docker-compose.yml up --build

echo "Notification service is starting..."