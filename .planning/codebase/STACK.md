# STACK.md — Technology Stack

## Runtime & Language

| Layer | Technology |
|-------|-----------|
| Language | Java 21 (sealed/record preview used) |
| Runtime | JVM — Eclipse Temurin 21 JRE (Docker) |
| Build tool | Gradle 8.9 (wrapper), Groovy DSL |
| Application | Spring Boot 3.3.3 |

## Frameworks & Libraries

### Backend
- **Spring Boot Starter Web** — Embedded Tomcat, REST controllers (`@RestController`)
- **Spring Boot Starter WebSocket** — STOMP over native WebSocket (`@EnableWebSocketMessageBroker`)
- **Spring Boot Starter Validation** — Jakarta Bean Validation for STOMP payload records
- **Project Lombok** — `@Slf4j`, `@RequiredArgsConstructor` on controller (compile-time, `compileOnly`)
- **Spring Boot Starter Test** — JUnit 5 + Mockito (transitive)

### Frontend (vendored in `src/main/resources/static/`)
- **stomp.min.js** — STOMP client over raw WebSocket
- **purify.min.js** — DOMPurify for sanitizing user-provided strings before DOM insertion
- **qrcode.min.js** — QR code generation for room join links
- **Font Awesome** (all.min.css + webfonts/) — Icon set
- Vanilla JavaScript, no framework bundler

## Configuration

- `application.yaml` (project root, also copied into Docker image):
  ```yaml
  app:
    room:
      max-rooms: 2500         # Hard cap — oldest room evicted when exceeded
  server:
    forward-headers-strategy: framework   # Handles X-Forwarded-* from reverse proxy
    tomcat:
      remote-ip-header: x-forwarded-for
      protocol-header: x-forwarded-proto
      use-relative-redirects: true
  logging:
    level:
      "[de.koderman.domain.Room]": INFO   # Most other loggers commented out
  ```
- Config value injected via `@Value("${app.room.max-rooms:2500}")` in both `RoomRepository` and `StartupPropertiesLogger`

## Build & Packaging

- Build: `./gradlew build` or `./gradlew bootJar`
- Run locally: `./gradlew bootRun`
- Docker: two-stage build in `Dockerfile`
  - **Build stage**: `gradle:8.9-jdk21` — uses BuildKit `--mount=type=cache` for Gradle cache
  - **Runtime stage**: `eclipse-temurin:21-jre` — copies jar + `application.yaml`
  - Exposes port `8080`
  - Entry point: `java -jar app.jar`
- Shell convenience scripts: `build.sh`, `run.sh` (Linux/CI usage)
