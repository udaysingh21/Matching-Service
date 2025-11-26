# Use Eclipse Temurin JRE for better Apple Silicon support
FROM eclipse-temurin:17-jre

# Set working directory
WORKDIR /app

# Copy the JAR file
COPY target/matching-service-1.0.0.jar app.jar

# Expose port
EXPOSE 8081

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]