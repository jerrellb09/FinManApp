FROM openjdk:21-slim

# Set working directory
WORKDIR /app

# Download OpenTelemetry Java agent
ADD https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/latest/download/opentelemetry-javaagent.jar opentelemetry-javaagent.jar

# Copy the JAR file
COPY target/FinManApp-0.0.1-SNAPSHOT.jar app.jar

# Expose the port the app runs on
EXPOSE 8080

# Command to run the application with OpenTelemetry
ENTRYPOINT ["java", "-javaagent:opentelemetry-javaagent.jar", "-jar", "/app/app.jar"]