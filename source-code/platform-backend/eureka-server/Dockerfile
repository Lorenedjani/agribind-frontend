FROM maven:3.9-eclipse-temurin-11 AS builder
WORKDIR /app

# Copy the standalone POM
COPY pom.xml pom.xml
COPY src ./src

# Build with the standalone POM
RUN mvn clean package -DskipTests

FROM eclipse-temurin:11-jre AS runtime
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE ${PORT:-8761}
ENTRYPOINT ["java", "-jar", "app.jar"]