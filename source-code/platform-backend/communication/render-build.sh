#!/bin/bash
echo "Building and deploying to Render..."

# Build the application
mvn clean package -DskipTests

# Check if build was successful
if [ $? -eq 0 ]; then
    echo "Build successful!"
    echo "Deployment will be triggered automatically by Render..."
else
    echo "Build failed!"
    exit 1
fi