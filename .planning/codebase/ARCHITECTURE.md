# ARCHITECTURE.md — System Architecture

## Pattern

**Layered Spring Boot monolith** with a clear domain model, thin infrastructure adapter, and static-file frontend. No microservices, no service layer abstraction — the controller directly delegates to the `Room` domain entity.

```
┌─────────────────────────────────────────────────────────────┐
│  Browser (Vanilla JS + STOMP)                               │
│  chair.html / participant.html / popout.html / landing.html │
└───────────────────┬─────────────────────────────────────────┘
                    │ WebSocket /ws  +  HTTP /api/*
┌───────────────────▼─────────────────────────────────────────┐
│  Infrastructure Layer                                        │
│  MeetingController  ─  REST @GetMapping/@PostMapping        │
│                     ─  STOMP @MessageMapping                │
│  Health             ─  GET /healthz                         │
└───────────────────┬─────────────────────────────────────────┘
                    │
┌───────────────────▼─────────────────────────────────────────┐
│  Domain Layer                                                │
│  Room            — mutable aggregate, explicit ReentrantLock│
│  RoomRepository  — in-memory ConcurrentHashMap + session map│
│  Domain records  — immutable payloads / projections         │
│  Domain enums    — MeetingGoal, ParticipationFormat, etc.   │
│  Domain exceptions — RoomNotFoundException, ChairAccess...  │
└─────────────────────────────────────────────────────────────┘
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
No external auth system. Chair identity = the WebSocket session ID that first calls `assumeChairRole()`. Session ID is cross-referenced on every chair-only STOMP message (`@MessageMapping`). Chair role auto-releases on WebSocket disconnect (`SessionDisconnectEvent`).

### State Broadcast Model
After every mutation, the controller calls `broadcast(roomCode)` which snapshots the full `Room` state into an immutable `State` record and pushes it to `/topic/room/{code}/state`. Clients maintain no local delta tracking; they always receive the full state.

### Concurrency
`Room` uses `ReentrantLock` (not `synchronized`) to allow future lock interruptibility. `RoomRepository` uses `ConcurrentHashMap` for reads and a dedicated `roomCreationLock` object for the create+evict atomic operation.

### Room Eviction
When `maxRooms` is reached, the oldest room (by `meetingStartSec` timestamp, stored in `TreeMap`) is silently evicted — including any active sessions. Eviction is logged as a warning.

### Poll Lifecycle
`ACTIVE → ENDED → CLOSED`. During `ENDED` the overlay shows results. During `CLOSED` the last results are retained in `lastPollResults` for later reference but the overlay is hidden.

### Room Code Normalization
Room codes are 4 chars, chars `A-Z` + `1-9` (no zero). Input is normalized server-side: `.toUpperCase().replace("0", "O")`. All methods in `MeetingController` normalize before passing to `RoomRepository`.

## Entry Points

| Entry Point | Type | Notes |
|-------------|------|-------|
| `MeetingApp.main()` | Spring Boot bootstrap | `@SpringBootApplication` |
| `POST /api/rooms` | REST | Creates room, returns code |
| `/ws` | WebSocket | STOMP upgrade endpoint |
