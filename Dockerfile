FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Copy the built jar — run `mvn package -DskipTests` before building this image
COPY target/loan-service-1.0.0.jar app.jar

# Expose the default Spring Boot port
EXPOSE 8080

# All sensitive config comes in via environment variables at runtime
ENTRYPOINT ["java", "-jar", "app.jar"]
