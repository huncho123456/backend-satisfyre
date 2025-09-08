# Use the JDK for the build stage
FROM eclipse-temurin:17-jdk as builder

WORKDIR /app

# 1. Copy Gradle wrapper and config files first (for cache)
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .
# Copy the source code
COPY src src

# Make gradlew executable and build the application
RUN chmod +x gradlew
RUN ./gradlew clean build -x test

# 2. Final runtime stage - use JRE for smaller image
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# Create a non-root user for security (important for Render)
RUN groupadd --system javauser && useradd --system --gid javauser javauser

# --- CREATE EMPTY .env FILE FIRST ---
RUN touch .env
COPY --from=builder /app/build/libs/*.jar app.jar
COPY .env .env

# Change ownership to non-root user (includes the .env file)
RUN chown -R javauser:javauser /app
USER javauser

# Expose the port (Render will use this)
EXPOSE 8080

# Use ENTRYPOINT + CMD for better flexibility
ENTRYPOINT ["java", "-jar"]
CMD ["/app/app.jar"]