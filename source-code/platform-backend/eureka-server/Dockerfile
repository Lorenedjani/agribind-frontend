# Stage 1: Build with parent context
FROM maven:3.8.4-openjdk-11 AS builder

WORKDIR /workspace

# Copy parent POM first
COPY ../pom.xml ./pom.xml

# Copy this module's POM
COPY pom.xml ./eureka-server/pom.xml

# Download dependencies for the entire project
RUN mvn dependency:go-offline -B -N

# Copy source code
COPY src ./eureka-server/src

# Build only the eureka-server module
RUN mvn clean package -pl eureka-server -am -DskipTests

# Stage 2: Runtime
FROM openjdk:11-jre-slim
WORKDIR /app

# Copy the built jar
COPY --from=builder /workspace/eureka-server/target/*.jar app.jar

EXPOSE ${PORT:-8761}

ENTRYPOINT ["java", "-jar", "app.jar"]