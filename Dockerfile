
# DockerFile
# Java 21 with Spring Boot 3.5.0 and H2 database
FROM eclipse-temurin:21-jdk-alpine

# Set working directory inside the container
WORKDIR /app

# Copy the Spring Boot JAR into the image
COPY target/oceandive-0.0.1-SNAPSHOT.jar app.jar

# Copy the H2 database file (must be in ./data/ locally)
COPY data/ /app/data/

# Expose the Spring Boot default port
EXPOSE 8080

# Run the app
ENTRYPOINT ["java", "-jar", "app.jar"]