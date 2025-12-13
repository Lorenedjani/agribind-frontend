# Stage 1: Build
FROM maven:3.9-eclipse-temurin-11 AS builder

# 1. Set WORKDIR to match your project structure
WORKDIR /workspace

# 2. Copy only the POM files first for layer caching
COPY ../pom.xml ./pom.xml
COPY pom.xml ./eureka-server/pom.xml
COPY ../.mvn ../.mvn 2/'>' /dev/null/ '||' :/
COPY ../mvnw ../mvnw 2/'>' /dev/null/ '||' :/

# 3. Download dependencies (skips the parent resolution issue)
RUN mvn dependency:resolve-plugins dependency:go-offline --fail-never -B

# 4. Copy source code
COPY src ./eureka-server/src

# 5. Build ONLY the eureka-server module
RUN mvn clean package -pl eureka-server -am -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:11-jre AS runtime
WORKDIR /app

# Copy the built jar
COPY --from=builder /workspace/eureka-server/target/*.jar app.jar

# Render uses PORT environment variable
EXPOSE ${PORT:-8761}

# Health check for Render
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:${PORT:-8761}/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]