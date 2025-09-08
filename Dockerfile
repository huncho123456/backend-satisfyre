# -----------------------------
# 1️⃣ Build Stage
# -----------------------------
FROM eclipse-temurin:17-jdk as builder

WORKDIR /app

# Copy Gradle wrapper and config files first (for caching)
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# Copy the source code
COPY src src

# Make Gradle wrapper executable and build the app
RUN chmod +x gradlew
RUN ./gradlew clean build -x test

# -----------------------------
# 2️⃣ Runtime Stage
# -----------------------------
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# Create a non-root user for security
RUN groupadd --system javauser && useradd --system --gid javauser javauser

# Copy the built JAR from the builder stage
COPY --from=builder /app/build/libs/*.jar app.jar

# Set ownership to non-root user
RUN chown -R javauser:javauser /app
USER javauser

# Expose a default port (for documentation; Render overrides it)
EXPOSE 8080

# Use Render's dynamic port
ENTRYPOINT ["sh", "-c", "java -Dserver.port=$PORT -jar /app/app.jar"]
