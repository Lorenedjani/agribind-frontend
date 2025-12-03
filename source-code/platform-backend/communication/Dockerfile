# Multi-stage build optimized for Render
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build

WORKDIR /app

# First, copy only the pom.xml to download dependencies (caching layer)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Then copy source code and build
COPY src src
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-alpine

WORKDIR /app

# Copy the built jar from the build stage
COPY --from=build /app/target/communication-*.jar app.jar

# Create directories for uploads and logs
RUN mkdir -p /tmp/uploads/audio /tmp/logs

# Create non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Expose port (Render will use PORT environment variable)
EXPOSE ${PORT:-8084}

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:${PORT:-8084}/api/communications/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "-Djava.security.egd=file:/dev/./urandom", "-Dspring.profiles.active=prod", "/app/app.jar"]