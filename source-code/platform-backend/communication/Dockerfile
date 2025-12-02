# Build stage
FROM eclipse-temurin:17-jdk AS build

WORKDIR /app

# Copy project files
COPY pom.xml .
COPY src src

# Build the application
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre

WORKDIR /app

# Copy the built jar
COPY --from=build /app/target/communication-*.jar app.jar

# Create directories for uploads and logs
RUN mkdir -p /tmp/uploads/audio /tmp/logs

# Set non-root user for security
RUN groupadd -r spring && useradd -r -g spring spring
USER spring:spring

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl --fail http://localhost:8084/api/communications/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "-Djava.security.egd=file:/dev/./urandom", "-Dspring.profiles.active=prod", "/app/app.jar"]