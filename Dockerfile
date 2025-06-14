# --- Stage 1: Build stage using Maven + Java 21 ---
FROM maven:3.9.4-eclipse-temurin-21 AS build

WORKDIR /app

# Copy source code
COPY . .

# Run mvn clean install to build the application
RUN mvn clean package -DskipTests

# --- Stage 2: Runtime image ---
FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

# Copy the built jar from the previous stage
COPY --from=build /app/target/oceandive-0.0.1-SNAPSHOT.jar app.jar

# Copy the database
COPY --from=build /app/data/ /app/data/

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
