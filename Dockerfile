# ---- Build stage ----
FROM eclipse-temurin:21 AS builder
WORKDIR /app

# Copy Gradle files and source
COPY build.gradle gradlew* ./
COPY gradle gradle
COPY src src

# Build application
RUN ./gradlew clean bootJar --no-daemon

# ---- Runtime stage ----
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copy only the jar from builder
COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 80
# Run Spring Boot on port 80 inside the container
ENTRYPOINT ["java","-Dserver.port=80","-jar","app.jar"]