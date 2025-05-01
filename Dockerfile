# Build stage
FROM maven:4.0.0-eclipse-temurin-21 AS build-stage

# Set the working directory
WORKDIR /app

# Copy the pom.xml and source code
COPY pom.xml ./

# Copy the settings.xml file to the appropriate location
COPY src ./src
COPY settings.xml /root/.m2/settings.xml
RUN mvn clean package -DskipTests

# Runtime stage
FROM openjdk:21-jdk AS runtime-stage
# Set the working directory
WORKDIR /app
# Copy the jar file from the build stage
COPY --from=build-stage /app/target/oceandive-0.0.1-SNAPSHOT.jar app.jar

# Expose the application port
EXPOSE 8080

# Copy the settings.xml file to the appropriate location
COPY settings.xml /root/.m2/settings.xml

# Set the entry point for the container
ENTRYPOINT ["java", "-jar", "app.jar"]