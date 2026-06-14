# ============================================
# Stage 1: Build Application
# ============================================
FROM gradle:8.8-jdk17 AS builder

WORKDIR /app

# Copy Gradle wrapper and dependencies first (cache optimization)
COPY gradle gradle
COPY build.gradle.kts settings.gradle.kts gradlew ./

# Download dependencies (cached if build.gradle.kts hasn't changed)
RUN ./gradlew dependencies --no-daemon || true

# Copy source code
COPY src src

# Build application (skip tests - they run in CI)
RUN ./gradlew clean bootJar -x test --no-daemon

# ============================================
# Stage 2: Runtime
# ============================================
FROM eclipse-temurin:17-jre

LABEL maintainer="genner.diego@example.com"
LABEL version="1.0.0"
LABEL description="Deck Game Service - Spring Boot REST API"

WORKDIR /app

# Install curl for healthcheck (using apt since not alpine)
RUN apt-get update && \
    apt-get install -y --no-install-recommends curl && \
    rm -rf /var/lib/apt/lists/*

# Create non-root user for security
RUN groupadd -r spring && useradd -r -g spring spring

# Copy built artifact from builder stage
COPY --from=builder /app/build/libs/*.jar app.jar

# Change ownership to non-root user
RUN chown spring:spring app.jar

# Switch to non-root user
USER spring:spring

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8080/api/v1/actuator/health || exit 1

# JVM optimizations
ENV JAVA_OPTS="-Djava.security.egd=file:/dev/./urandom \
  -XX:+UseContainerSupport \
  -XX:MaxRAMPercentage=75.0 \
  -XX:InitialRAMPercentage=50.0"

# Run application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
