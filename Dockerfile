# Build stage
ARG MAVEN_VERSION=4.0.0-eclipse-temurin-21
FROM maven:${MAVEN_VERSION} AS build-stage
WORKDIR /app
COPY pom.xml ./
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime stage
ARG JDK_VERSION=21-jdk
FROM openjdk:${JDK_VERSION} AS runtime-stage
WORKDIR /app
ARG JAR_FILE=oceandive-0.0.1-SNAPSHOT.jar
COPY --from=build-stage /app/target/${JAR_FILE} app.jar
ARG APP_PORT=8080
EXPOSE ${APP_PORT}
ENTRYPOINT ["java", "-jar", "app.jar"]