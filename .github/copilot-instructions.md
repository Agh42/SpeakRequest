# Copilot Instructions for SpeakRequest

## Build and test commands

Use the Gradle wrapper from repo root.

- Full build: `.\gradlew.bat build`
- Run app locally: `.\gradlew.bat bootRun`
- Run all tests: `.\gradlew.bat test`
- Run one test class: `.\gradlew.bat test --tests "de.koderman.RoomCreationFlowTest"`
- Run one test method: `.\gradlew.bat test --tests "de.koderman.RoomCreationFlowTest.testCreateRoomEndpoint_createsRoomWithRandomCode"`

There is no dedicated lint task configured in `build.gradle` (no Checkstyle/SpotBugs/PMD plugin).

## High-level architecture

- Backend is a Spring Boot app (`MeetingApp`) serving static frontend files and handling realtime messaging.
- `MeetingController` is the central adapter for both:
  - REST routes (`/api/rooms`, `/api/rooms/{roomCode}`, `/api/metadata/*`)
  - STOMP message routes (`/app/room/{roomCode}/...`)
- WebSocket/STOMP config (`WsConfig`):
  - endpoint: `/ws`
  - app destination prefix: `/app`
  - broker topics: `/topic`
- Core domain model is `Room`:
  - manages queue, current speaker timer, chair ownership, poll state, and room config
  - enforces chair-only actions via session-based checks
  - uses explicit locking (`ReentrantLock`) for consistency under concurrent websocket events
- `RoomRepository` is in-memory (`ConcurrentHashMap`) with session-to-room tracking and a max-room cap (`app.room.max-rooms`). When limit is reached, oldest room is evicted.
- Frontend is static HTML + inline JS (`chair.html`, `participant.html`, `popout.html`, `landing.html`) using STOMP over WebSocket to subscribe/publish room state/events.

## Key repository conventions

- Room codes are 4 chars, generated from `A-Z` and `1-9` (no zero). Input room codes are normalized to uppercase and `0 -> O` server-side.
- Room state updates are broadcast to `/topic/room/{roomCode}/state`; room lifecycle/errors use:
  - `/topic/room/{roomCode}/destroyed`
  - `/topic/room/{roomCode}/error`
  - user-specific chair errors on `/user/queue/error`
- Chair authorization is based on websocket session id, not a separate auth system.
- Domain payloads are Java `record`s with Jakarta Validation annotations (`@NotBlank`, `@Size`, `@Pattern`), and message handlers typically use `@Valid`.
- Poll lifecycle in `Room`: `ACTIVE -> ENDED -> CLOSED`, with `lastPollResults` retained for later display.
- Frontend sanitizes dynamic user-facing strings with `DOMPurify` before rendering.
- Metadata for enums (meeting goal, participation format, decision rule, deliverable) is loaded from backend `/api/metadata/*` endpoints and cached in `localStorage` by `metadata-loader.js`.
