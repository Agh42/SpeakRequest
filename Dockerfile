# ---- Build stage (use official Gradle image to leverage caching of dependencies) ----
FROM gradle:8.9-jdk21 AS builder
WORKDIR /home/gradle/project

COPY gradle/ gradle/
COPY gradlew gradlew
COPY build.gradle settings.gradle* gradle.properties* ./

RUN chmod +x gradlew

RUN --mount=type=cache,target=/home/gradle/.gradle ./gradlew --no-daemon dependencies || true

COPY src src
# Use BuildKit cache mount to persist Gradle cache between builds for much faster incremental builds.
RUN --mount=type=cache,target=/home/gradle/.gradle ./gradlew clean bootJar --no-daemon

# ---- Runtime stage ----
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copy only the jar from builder
COPY --from=builder /home/gradle/project/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]