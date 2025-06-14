# --- Stage 1: Build stage using Maven ---
FROM maven:3.8.3-openjdk-21 AS build

WORKDIR /app

# Copy all source code
COPY . .

# Build the Spring Boot application
RUN mvn clean package -DskipTests

# --- Stage 2: Final image ---
FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

# Copy only the built JAR from the previous stage
COPY --from=build /app/target/oceandive-0.0.1-SNAPSHOT.jar app.jar

# Copy database file if it's in the repo
COPY --from=build /app/data/ /app/data/

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
