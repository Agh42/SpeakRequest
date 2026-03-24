# CONCERNS.md — Technical Debt, Issues & Fragile Areas

## High Priority

### 1. Silent Room Eviction Affects Active Users
**File**: `src/main/java/de/koderman/domain/RoomRepository.java`

When `maxRooms` (default 2500) is reached, the oldest room is removed **without notifying its participants**. Connected clients will continue sending STOMP messages that will silently fail with `RoomNotFoundException`, and the error is broadcast to the `/topic/room/{code}/error` topic — but clients in that room may not receive it if they've already missed the `destroyed` event. The log shows a warning but the UX is broken silently.

### 2. No Persistence — All State Lost on Restart
All room state lives in `ConcurrentHashMap` in-memory. Any JVM restart (deploy, crash, OOM kill) destroys all active meeting rooms and queues with no recovery path. There is no graceful shutdown handler, so in-flight sessions are broken abruptly.

### 3. Test References Non-Existent Field Name
**File**: `src/test/java/de/koderman/RoomLimitIntegrationTest.java` (line ~33)

The test attempts to reflect on `MAX_ROOMS` (a static constant naming convention), but the actual field in `RoomRepository` is `maxRooms` (instance field). This test will fail at the reflection step unless the field name matches exactly. Needs verification.

### 4. Chair Authority Tied to Ephemeral Session ID
Chair role is bound to a WebSocket session ID. If a chair's network drops and reconnects, their new session ID differs — they cannot reclaim chair role unless a fresh `assumeChair` flow is triggered and no other client has assumed the chair in the interim. There is no persistent chair token or rejoin mechanism.

## Medium Priority

### 5. MeetingController: Reflection-Based Test Access is Fragile
`RoomCreationFlowTest` and `WebSocketErrorHandlingTest` access `MeetingController`'s private `roomRepository` field via reflection. Any field rename breaks the tests silently at the reflection call (not a compile error).

### 6. No Rate Limiting or Abuse Prevention
`POST /api/rooms` and all STOMP message endpoints have no rate limiting. A single client can:
- Flood the server with `POST /api/rooms` until `maxRooms` is reached, evicting all active meetings.
- Spam the speaker queue with `request` messages under different names.
- There's no IP-based throttling, no CAPTCHA, and no auth.

### 7. `RoomRepository.roomsByTimestamp` Can Have Duplicate Keys
`TreeMap<Long, Room>` is keyed by `meetingStartSec` (seconds precision). If two rooms are created in the same second, the second overwrite the first in the TreeMap. This means one room becomes invisible to the eviction mechanism:
```java
roomsByTimestamp.put(newRoom.getMeetingStartSec(), newRoom);
// If same second, silently replaces the earlier room's eviction entry
```

### 8. CORS: `setAllowedOriginPatterns("*")`
**File**: `src/main/java/de/koderman/config/WsConfig.java`

WebSocket endpoint allows all origins. Acceptable for a public demo but worth restricting in production deployments.

### 9. `TimerCtrl` Action Parsed as Lowercase String Switch
**File**: `src/main/java/de/koderman/infrastructure/MeetingController.java`

`ctrl.action().toLowerCase()` is used in a switch expression with `"start"`, `"pause"`, `"reset"` cases. Unknown actions are silently ignored (no default). Typos in client code produce no error.

### 10. Frontend Test Pages in Production Build
`test-metadata.html` and `test-share.html` are included in `src/main/resources/static/` and are served to any user. These are developer test pages and should be excluded from production builds.

## Low Priority / Observations

### 11. `src/chair-view-redesign/` in Source Tree
An in-progress redesign prototype (`code.html`, `DESIGN.md`) lives under `src/` but is not part of the main Gradle build. It's dead code in production but could cause confusion.

### 12. Lombok on Infrastructure Only
Lombok is used in `MeetingController` (`@RequiredArgsConstructor`, `@Slf4j` via `RoomRepository`) and `RoomRepository`. The domain models do not depend on Lombok (records handle immutability). This is fine but slightly inconsistent — Room uses Lombok's `@Slf4j` indirectly via import.

### 13. `StartPoll` Record Has Unused Fields
`StartPoll` has both `votesPerParticipant` and `votesPerOption` fields. The `votesPerOption` is not used by `Room.startPoll()`:
```java
public record StartPoll(... Integer votesPerParticipant, Integer votesPerOption) {}
```
`votesPerOption` is dead payload field.

### 14. No `application-*.yaml` Profile Support
A single `application.yaml` serves all environments. There are no `application-dev.yaml`, `application-prod.yaml` profiles. Configuration differences (e.g., logging verbosity, `max-rooms`) must be managed externally (env vars or replacing the file in Docker).

### 15. `metadata-loader.js` Cache Never Expires
Metadata is cached in `localStorage` with a version check (`"1.0"`). If enum display names change in a server release but the version string isn't bumped, stale metadata will be shown to returning users indefinitely until they clear their local storage.
