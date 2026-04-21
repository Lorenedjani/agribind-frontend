#!/bin/bash
echo "Building Eureka Server from multi-module project..."

# Move to parent directory to access parent POM
cd ..

# Build only the eureka-server module
mvn clean package -pl eureka-server -am -DskipTests

# Check if build was successful
if [ $? -eq 0 ]; then
    echo "Build successful!"
    ls -la eureka-server/target/*.jar
else
    echo "Build failed!"
    exit 1
fi