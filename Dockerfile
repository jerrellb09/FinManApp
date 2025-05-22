FROM openjdk:21-slim

# Set working directory
WORKDIR /app

# Copy the JAR file
COPY target/FinManApp-0.0.1-SNAPSHOT.jar app.jar

# Copy the Datadog Java agent
COPY dd-java-agent.jar dd-java-agent.jar

# Expose the port the app runs on
EXPOSE 8080

# Add Datadog labels for Autodiscovery
LABEL "com.datadoghq.ad.logs"='[{"source": "java", "service": "finmanapp"}]'

# Set Datadog environment variables
ENV DD_SERVICE=finmanapp
ENV DD_ENV=dev
ENV DD_VERSION=0.0.1
ENV DD_LOGS_INJECTION=true
ENV DD_TRACE_ENABLED=true
ENV DD_PROFILING_ENABLED=true
ENV DD_AGENT_HOST=datadog-agent

# Command to run the application with Datadog Java agent
ENTRYPOINT ["java", "-javaagent:/app/dd-java-agent.jar", "-jar", "/app/app.jar"]