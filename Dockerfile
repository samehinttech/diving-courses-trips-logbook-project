# Runtime Stage: lightweight image to run the app
FROM eclipse-temurin:21-jdk

# Install useful tools for dev environment
RUN apt-get update && apt-get install -y \
    curl \
    git \
    vim \
    sudo \
    && rm -rf /var/lib/apt/lists/*

# Arguments for user/group creation
ARG USERNAME=devuser
ARG USER_UID=1000
ARG USER_GID=1000

# Check if the group or user already exists and create a non-root user if needed
RUN if ! getent group $USER_GID; then \
      groupadd --gid $USER_GID $USERNAME; \
    fi \
    && if ! id -u $USERNAME > /dev/null 2>&1; then \
      useradd --uid $USER_UID --gid $USER_GID -m $USERNAME; \
    fi \
    && echo "$USERNAME ALL=(ALL) NOPASSWD:ALL" > /etc/sudoers.d/$USERNAME \
    && chmod 0440 /etc/sudoers.d/$USERNAME

# Set the working directory
WORKDIR /app

# Adjust permissions for the non-root user
RUN chown -R $USERNAME:$USERNAME /app

# Switch to the non-root user
USER $USERNAME

# Copy the jar file from the build stage
COPY --from=build /app/target/oceandive-0.0.1-SNAPSHOT.jar app.jar

# Expose the app port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Set the entrypoint
ENTRYPOINT ["java", "-jar", "app.jar"]