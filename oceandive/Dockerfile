# Use a Maven image to build the JAR
FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder

WORKDIR /oceandive

# Copy only the pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy the source code and build the application
COPY src ./src
RUN mvn clean package -DskipTests

# Use a minimal JRE image to run the app
FROM eclipse-temurin:21-jre-alpine

WORKDIR /oceandive

# Copy the built JAR from the builder stage
COPY --from=builder /oceandive/target/oceandive-0.0.1-SNAPSHOT.jar oceandive.jar

# Expose the application port
EXPOSE 8080

# Run the application
CMD ["java", "-jar", "oceandive.jar"]

LABEL Author="Sameh Ahmed"