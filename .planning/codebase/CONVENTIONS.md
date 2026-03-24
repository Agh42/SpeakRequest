# CONVENTIONS.md — Coding Conventions & Patterns

## Java Code Style

- **Java 21**, targeting `release = 21` via `options.release.set(21)` in `build.gradle`.
- **Package naming**: `de.koderman` base, subpackages: `config`, `domain`, `infrastructure`.
- **Class visibility**: Package-private for config/infrastructure classes (`WsConfig`, `Health`) unless Spring auto-detection requires `public`. Domain classes are `public`.
- **Lombok used conservatively**: `@Slf4j` (logging) and `@RequiredArgsConstructor` (constructor injection). No `@Data`, `@Builder`, or field injection.
- **Constructor injection** for Spring beans (`@RequiredArgsConstructor` on `MeetingController`).

## Domain Model Patterns

### Immutable Records for Messages and Projections
All STOMP payloads and state projections are Java `record` types:
```java
// Inbound command
public record RequestSpeak(
    @NotBlank @Size(max = 30) @Pattern(...) String name
) {}

// Outbound state
public record State(
    List<Participant> queue, Current current, long meetingStartSec, int defaultLimitSec,
    String roomCode, boolean chairOccupied, PollState pollState, RoomConfig roomConfig
) {}
```

### Mutable Aggregate with Explicit Locking
`Room` is the only mutable domain object. All public methods lock/unlock explicitly:
```java
public void nextParticipant(String sessionId) {
    lock.lock();
    try {
        requireChairAccess(sessionId);
        // ... mutation
    } finally {
        lock.unlock();
    }
}
```

### Chair Authorization Pattern
Every chair-only method calls `requireChairAccess(sessionId)` at the top of the lock block:
```java
private void requireChairAccess(String sessionId) {
    if (!isChairSession(sessionId)) {
        throw new ChairAccessException("Chair access required", this.roomCode, sessionId);
    }
}
```

### Snapshot/Broadcast Pattern
After every state mutation, the controller calls a shared `broadcast()` helper:
```java
private void broadcast(String roomCode) {
    Room room = roomRepository.getByCodeOrThrow(roomCode);
    State s = room.snapshot();
    broker.convertAndSend("/topic/room/" + roomCode + "/state", s);
}
```

## Input Validation

- Jakarta Validation annotations on STOMP payload records: `@NotBlank`, `@Size`, `@Pattern`.
- `@Valid` on `@Payload` parameters in STOMP handlers.
- Null/blank guards in handlers before delegating to domain (belt-and-suspenders):
  ```java
  public void request(..., RequestSpeak msg) {
      if (msg == null || msg.name() == null || msg.name().isBlank()) return;
  ```
- Input sanitization on the frontend via `DOMPurify.sanitize()` before inserting user content into DOM.
- Room code normalization: `.toUpperCase().replace("0", "O")` applied in controller before any repo call.

## Error Handling

- STOMP exceptions handled via `@MessageExceptionHandler` methods in `MeetingController`:
  - `RoomNotFoundException` → broadcast to `/topic/room/{code}/error` (all subscribers)
  - `ChairAccessException` → send to `/user/queue/error` (offending session only)
- `RoomError` record carries `message`, `roomCode`, `errorCode`, and `redirectUrl` for client-side routing.
- REST endpoints use standard HTTP 200 with body; no explicit `@ExceptionHandler` at REST level.

## Enum Pattern
All domain enums carry human-readable metadata:
```java
public enum MeetingGoal {
    SHARE_INFORMATION("Share Information", "Ensure everyone has the same facts..."),
    ...;
    private final String displayName;
    private final String description;
    // getters only
}
```

Metadata is exposed via `/api/metadata/*` REST endpoints, cached in browser `localStorage` by `metadata-loader.js`.

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
