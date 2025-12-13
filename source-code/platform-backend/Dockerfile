# Multi-stage build for efficiency
FROM maven:3.9-eclipse-temurin-17 AS builder

# 1. First, create and build the parent POM structure
WORKDIR /app

# CRITICAL FIX: Copy the PARENT pom.xml from project root
# Your project structure shows the parent is at a higher level
COPY ../../pom.xml ./pom.xml

# Download dependencies for parent project first
RUN mvn dependency:go-offline -N

# Copy eureka-server module pom.xml
COPY eureka-server/pom.xml eureka-server/pom.xml

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