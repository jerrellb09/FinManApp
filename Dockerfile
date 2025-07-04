FROM openjdk:21-slim

# Set working directory
WORKDIR /app

# Copy the JAR file
COPY target/FinManApp-0.0.1-SNAPSHOT.jar app.jar

# Expose the port the app runs on
EXPOSE 8080

# Command to run the application
ENTRYPOINT ["java", "-jar", "/app/app.jar"]