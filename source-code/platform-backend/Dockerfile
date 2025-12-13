# Multi-stage build for efficiency
FROM maven:3.9-eclipse-temurin-17 AS builder

# 1. First, create and build the parent POM structure
WORKDIR /app

# Copy the parent pom.xml (critical - must exist at root)
COPY eureka-server/pom.xml .

# Download dependencies first (cache layer)
RUN mvn dependency:go-offline -N

# Copy eureka-server module
COPY eureka-server/pom.xml eureka-server/pom.xml
RUN mvn dependency:go-offline -pl eureka-server

# Copy all source code
COPY eureka-server/src eureka-server/src

# Build ONLY eureka-server with its dependencies
RUN mvn clean package -pl eureka-server -am -DskipTests

# 2. Runtime stage
FROM eclipse-temurin:17-jre-alpine AS runtime

# Security: non-root user
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy the built jar from builder
COPY --from=builder /app/eureka-server/target/*.jar app.jar

# Expose Eureka port
EXPOSE 8761

# Health check (for Render)
HEALTHCHECK --interval=30s --timeout=3s --start-period=10s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8761/actuator/health || exit 1

# Run with optimized JVM settings for containers
ENTRYPOINT ["java", "-Xmx256m", "-jar", "/app.jar"]