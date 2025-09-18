# ---- Build stage ----
FROM eclipse-temurin:17 AS builder
WORKDIR /app

# Copy Gradle files and source
COPY build.gradle gradlew* ./
COPY gradle gradle
COPY src src

# Build application
RUN ./gradlew clean bootJar --no-daemon

# ---- Runtime stage ----
FROM eclipse-temurin:17-jre
WORKDIR /app

# Copy only the jar from builder
COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]