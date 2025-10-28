FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/location-service-1.0.0.jar app.jar

RUN apt-get update && apt-get install -y curl

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]