# Multi-stage build for efficiency
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /app

# Step 1: Install parent POM to local Maven repository
COPY pom.xml .
RUN mvn clean install -N -DskipTests

# Step 2: Build eureka-server as standalone project
WORKDIR /app/eureka-server
COPY eureka-server/pom.xml .
COPY eureka-server/src src

# Build the module
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre-alpine AS runtime

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=builder /app/eureka-server/target/*.jar app.jar

EXPOSE 8761

HEALTHCHECK --interval=30s --timeout=3s --start-period=10s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8761/actuator/health || exit 1

ENTRYPOINT ["java", "-Xmx256m", "-jar", "/app.jar"]
