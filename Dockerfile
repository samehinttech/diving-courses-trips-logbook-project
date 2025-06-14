# --- Stage 1: Build stage ---
FROM maven:3.9.4-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# --- Stage 2: Runtime ---
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY --from=build /app/target/oceandive-0.0.1-SNAPSHOT.jar app.jar
COPY --from=build /app/data/ /app/data/
COPY images/ /app/images/
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
