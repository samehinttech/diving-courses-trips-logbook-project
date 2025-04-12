#
# Build stage
FROM maven:3.9.4-eclipse-temurin-17-alpine-secure AS build
# Use a specific version of the base image to ensure reproducibility
# and avoid the need for apk update && apk upgrade

WORKDIR /app
# Copy settings.xml if it exists, otherwise create a default settings.xml
COPY settings.xml /root/.m2/settings.xml

# Cache dependencies
RUN mvn dependency:go-offline -B

# Build the project
COPY . /workspace

# Development stage
FROM mcr.microsoft.com/devcontainers/java:17 AS dev
COPY --from=build /app /workspace
COPY . /workspace
CMD ["bash"]

# Production stage
FROM eclipse-temurin:17-jdk-alpine-secure AS prod
WORKDIR /app


# Ensure only the correct JAR file is copied
COPY --from=build /app/target/diveXplore-0.0.1-SNAPSHOT.jar app.jar
RUN ln -s /app/diveXplore-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]