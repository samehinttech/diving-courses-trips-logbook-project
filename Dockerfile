

# Use a base image with Java (e.g., OpenJDK)
FROM openjdk:21-jdk-slim 

WORKDIR /app

 # Copy the packaged application jar into the container, assuming the jar is built in the 'target' folde
COPY target/oceandive-0.0.1-SNAPSHOT.jar app.jar

 # Expose the port your application will use
EXPOSE 8080

 # Run the JAR file when the container starts
ENTRYPOINT ["java", "-jar", "app.jar"]


