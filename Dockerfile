# Build stage
FROM maven:3.9.9-eclipse-temurin-21 AS build-stage

# Set the working directory
WORKDIR /app

# Copy the pom.xml and source code
COPY pom.xml ./
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package

# Runtime stage
FROM openjdk:21-jdk AS runtime-stage

# Set up environment variables
ARG USERNAME=devuser
ARG USER_UID=1000
ARG USER_GID=1000

# Install curl and development tools
RUN apt-get update && apt-get install -y \
        curl \
        git \
        vim \
        sudo \
    && rm -rf /var/lib/apt/lists/*

# Create a non-root user
RUN groupadd --gid $USER_GID $USERNAME || echo "Group already exists" \
     && useradd --uid $USER_UID --gid $USER_GID -m $USERNAME || echo "User  already exists" \
     && echo "$USERNAME ALL=(root) NOPASSWD:ALL" > /etc/sudoers.d/$USERNAME \
     && chmod 0440 /etc/sudoers.d/$USERNAME

# Set the working directory
WORKDIR /app

# Give ownership to the non-root user
RUN mkdir -p /app && chown -R $USERNAME:$USERNAME /app

# Switch to non-root user
USER $USERNAME

# Copy the jar file from the build stage
COPY --from=build-stage /app/target/oceandive-0.0.1-SNAPSHOT.jar app.jar

# Expose the application port
EXPOSE 8080

# Health check
HEALTHCHECK CMD curl --fail http://localhost:8080/ || exit 1

# Set the entry point for the container
ENTRYPOINT ["java", "-jar", "app.jar"]