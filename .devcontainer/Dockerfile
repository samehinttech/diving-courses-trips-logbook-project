# Dockerfile
FROM mcr.microsoft.com/devcontainers/base:ubuntu

# Install dependencies
RUN apt-get update && apt-get install -y \
    wget \
    unzip \
    && rm -rf /var/lib/apt/lists/*

# Install Eclipse Temurin 23.0.2
RUN wget https://github.com/adoptium/temurin23-binaries/releases/download/jdk-23.0.2+10/OpenJDK23U-jdk_x64_linux_hotspot_23.0.2_10.tar.gz -O temurin.tar.gz \
    && mkdir -p /usr/local/temurin \
    && tar -xzf temurin.tar.gz -C /usr/local/temurin --strip-components=1 \
    && rm temurin.tar.gz

# Set JAVA_HOME environment variable
ENV JAVA_HOME=/usr/local/temurin
ENV PATH=$JAVA_HOME/bin:$PATH