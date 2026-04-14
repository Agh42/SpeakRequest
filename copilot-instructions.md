<!-- GSD:project-start source:PROJECT.md -->
## Project

**SpeakRequest — Chair View Redesign**

SpeakRequest is a hybrid meeting manager for discussion moderators and facilitators. It manages speaking turns with a transparent queue, tracks speech timers, and conducts realtime polls. The Chair View redesign replaces the current functional-but-plain chair.html with a high-end, editorial-inspired command center that introduces a central round conference table showing all participant avatars.

**Core Value:** The chair can see every participant's status at a glance — who is speaking, who is next, and who is waiting — without leaving the main screen.

### Constraints

- **No CDN for production-critical assets**: Tailwind CDN is acceptable (already pattern in redesign mockup); fonts via Google Fonts acceptable (already used in mockup)
- **No backend changes**: All changes confined to `src/main/resources/static/chair.html` and `styles.css` (new tokens may be added)
- **DOMPurify required**: All participant names and user-supplied content must continue to be sanitized before DOM insertion
- **Browser compat**: Same as existing app — modern evergreen browsers (Chrome, Firefox, Edge, Safari)
<!-- GSD:project-end -->

<!-- GSD:stack-start source:codebase/STACK.md -->
## Technology Stack

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
- Config value injected via `@Value("${app.room.max-rooms:2500}")` in both `RoomRepository` and `StartupPropertiesLogger`
## Build & Packaging
- Build: `./gradlew build` or `./gradlew bootJar`
- Run locally: `./gradlew bootRun`
- Docker: two-stage build in `Dockerfile`
- Shell convenience scripts: `build.sh`, `run.sh` (Linux/CI usage)
<!-- GSD:stack-end -->

<!-- GSD:conventions-start source:CONVENTIONS.md -->
## Conventions

## Java Code Style
- **Java 21**, targeting `release = 21` via `options.release.set(21)` in `build.gradle`.
- **Package naming**: `de.koderman` base, subpackages: `config`, `domain`, `infrastructure`.
- **Class visibility**: Package-private for config/infrastructure classes (`WsConfig`, `Health`) unless Spring auto-detection requires `public`. Domain classes are `public`.
- **Lombok used conservatively**: `@Slf4j` (logging) and `@RequiredArgsConstructor` (constructor injection). No `@Data`, `@Builder`, or field injection.
- **Constructor injection** for Spring beans (`@RequiredArgsConstructor` on `MeetingController`).
## Domain Model Patterns
### Immutable Records for Messages and Projections
### Mutable Aggregate with Explicit Locking
### Chair Authorization Pattern
### Snapshot/Broadcast Pattern
## Input Validation
- Jakarta Validation annotations on STOMP payload records: `@NotBlank`, `@Size`, `@Pattern`.
- `@Valid` on `@Payload` parameters in STOMP handlers.
- Null/blank guards in handlers before delegating to domain (belt-and-suspenders):
- Input sanitization on the frontend via `DOMPurify.sanitize()` before inserting user content into DOM.
- Room code normalization: `.toUpperCase().replace("0", "O")` applied in controller before any repo call.
## Error Handling
- STOMP exceptions handled via `@MessageExceptionHandler` methods in `MeetingController`:
- `RoomError` record carries `message`, `roomCode`, `errorCode`, and `redirectUrl` for client-side routing.
- REST endpoints use standard HTTP 200 with body; no explicit `@ExceptionHandler` at REST level.
## Enum Pattern
## Frontend Conventions
- **Vanilla JS** (ES2020+), no build step, no bundler.
- STOMP client connected to `ws[s]://{host}/ws` using scheme detection (`location.protocol`).
- User-generated content always sanitized: `DOMPurify.sanitize(value)` before `innerHTML` or `textContent`.
- Metadata cached in `localStorage` with key `speek_metadata_{type}`, version-keyed `"version": "1.0"` — stale cache invalidated on version mismatch.
- QR codes generated client-side via `qrcode.min.js`.
- Share functionality in `share.js` uses Web Share API with a modal fallback (copy/email/WhatsApp/SMS).
## Logging
- Structured via SLF4J + Logback (Spring Boot default).
- `@Slf4j` on `Room` and `RoomRepository` — detailed operation logging at INFO/DEBUG/WARN levels.
- Pattern: `Room[{roomCode}] methodName: context info sessionId={} ...`
- Debug logs for normal operations, INFO for state changes, WARN for evictions/rejections, ERROR for unexpected states.
<!-- GSD:conventions-end -->

<!-- GSD:architecture-start source:ARCHITECTURE.md -->
## Architecture

## Pattern
```
```
## Layers
### Infrastructure (`de.koderman.infrastructure`)
- **`MeetingController`** — the single controller; handles both REST and STOMP routes. Uses `@EventListener` for `SessionDisconnectEvent` to release chair role on WebSocket disconnect.
- **`Health`** — minimal health check endpoint.
- **`SimpMessagingTemplate`** injected for broadcasting state.
### Domain (`de.koderman.domain`)
- **`Room`** — the core aggregate. Holds mutable state (queue, current speaker, timer, poll, config, chair session). All state access gated by `ReentrantLock`. Chair-only actions enforced via `requireChairAccess(sessionId)`.
- **`RoomRepository`** — application-level repository (not JPA). Uses `ConcurrentHashMap<String, Room>` keyed by room code, a `ConcurrentHashMap<String, String>` for session→room tracking, and a `TreeMap<Long, Room>` for ordered timestamp-based eviction. Room creation is synchronized on `roomCreationLock`.
- **Request records** (STOMP payloads): `Join`, `RequestSpeak`, `Withdraw`, `TimerCtrl`, `SetLimit`, `AssumeChair`, `StartPoll`, `CastVote`, `UpdateRoomConfig`, `CreateRoom`, `DestroyRoom`
- **State/projection records**: `State`, `Current`, `Participant`, `PollState`, `PollResults`, `RoomConfig`, `RoomInfo`, `RoomDestroyed`, `RoomError`
- **Domain enums** (with display names + descriptions): `MeetingGoal`, `ParticipationFormat`, `DecisionRule`, `Deliverable`
- **Exceptions**: `RoomNotFoundException`, `ChairAccessException`
### Config (`de.koderman.config`)
- **`WsConfig`** — configures STOMP broker, heartbeat scheduler, allows all origins.
- **`StartupPropertiesLogger`** — logs key config values at startup via `@PostConstruct`.
## Key Design Decisions
### Chair Authorization
### State Broadcast Model
### Concurrency
### Room Eviction
### Poll Lifecycle
### Room Code Normalization
## Entry Points
| Entry Point | Type | Notes |
|-------------|------|-------|
| `MeetingApp.main()` | Spring Boot bootstrap | `@SpringBootApplication` |
| `POST /api/rooms` | REST | Creates room, returns code |
| `/ws` | WebSocket | STOMP upgrade endpoint |
<!-- GSD:architecture-end -->

<!-- GSD:workflow-start source:GSD defaults -->
## GSD Workflow Enforcement

Before using Edit, Write, or other file-changing tools, start work through a GSD command so planning artifacts and execution context stay in sync.

Use these entry points:
- `/gsd-quick` for small fixes, doc updates, and ad-hoc tasks
- `/gsd-debug` for investigation and bug fixing
- `/gsd-execute-phase` for planned phase work

Do not make direct repo edits outside a GSD workflow unless the user explicitly asks to bypass it.
<!-- GSD:workflow-end -->



<!-- GSD:profile-start -->
## Developer Profile

> Profile not yet configured. Run `/gsd-profile-user` to generate your developer profile.
> This section is managed by `generate-claude-profile` -- do not edit manually.
<!-- GSD:profile-end -->
