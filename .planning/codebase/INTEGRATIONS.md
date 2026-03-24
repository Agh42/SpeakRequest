# INTEGRATIONS.md — External Services & APIs

## External Dependencies

This is a **self-contained, server-side-only application** with no external service dependencies at runtime.

| Category | Details |
|----------|---------|
| Database | None — all state is in-memory (`ConcurrentHashMap`) |
| Auth provider | None — session identity is derived from WebSocket session ID only |
| Email / messaging | None |
| Cache (Redis, etc.) | None |
| Scheduled jobs | None (Spring scheduling not enabled) |
| Cloud platform | Agnostic — ships as a Docker image |
| CDN | None — all static assets served from the Spring Boot static resource handler |

## Internal HTTP / WebSocket Endpoints

### REST

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/rooms` | Create a room — returns `RoomInfo` with the generated code |
| `GET` | `/api/rooms/{roomCode}` | Check if room exists — returns `RoomInfo` |
| `GET` | `/api/metadata/meeting-goals` | Enum metadata for `MeetingGoal` values |
| `GET` | `/api/metadata/participation-formats` | Enum metadata for `ParticipationFormat` values |
| `GET` | `/api/metadata/decision-rules` | Enum metadata for `DecisionRule` values |
| `GET` | `/api/metadata/deliverables` | Enum metadata for `Deliverable` values |
| `GET` | `/healthz` | Health check — returns `{"status":"ok"}` |
| `GET` | `/` | Redirects → `/landing.html` |
| `GET` | `/chair/{roomCode}` | Redirects → `/chair.html?room={normalizedCode}` |
| `GET` | `/room/{roomCode}` | Redirects → `/participant.html?room={normalizedCode}` |

### STOMP (WebSocket)

WebSocket endpoint: `/ws`  
App prefix: `/app`  
Broker topic prefix: `/topic`

| STOMP Destination | Payload | Description |
|-------------------|---------|-------------|
| `/app/room/{code}/join` | `Join` | Join room as participant or chair |
| `/app/room/{code}/request` | `RequestSpeak` | Add self to speaker queue |
| `/app/room/{code}/withdraw` | `Withdraw` | Remove self from queue |
| `/app/room/{code}/next` | — | (chair) advance to next speaker |
| `/app/room/{code}/timer` | `TimerCtrl` | (chair) start/pause/reset timer |
| `/app/room/{code}/setLimit` | `SetLimit` | (chair) set per-speaker time limit |
| `/app/room/{code}/assumeChair` | `AssumeChair` | Claim chair role |
| `/app/room/{code}/poll/start` | `StartPoll` | (chair) start a poll |
| `/app/room/{code}/poll/vote` | `CastVote` | Cast a vote |
| `/app/room/{code}/poll/end` | — | (chair) end poll, show results |
| `/app/room/{code}/poll/close` | — | (chair) close poll overlay |
| `/app/room/{code}/poll/cancel` | — | (chair) cancel active poll |
| `/app/room/{code}/updateConfig` | `UpdateRoomConfig` | (chair) update room metadata |
| `/app/room/{code}/destroy` | — | (chair) destroy room |

### Outbound STOMP Topics (server → clients)

| Topic | Payload | Description |
|-------|---------|-------------|
| `/topic/room/{code}/state` | `State` | Full room state broadcast after any mutation |
| `/topic/room/{code}/destroyed` | `RoomDestroyed` | Room was closed |
| `/topic/room/{code}/error` | `RoomError` | Error targeting all topic subscribers |
| `/topic/room/{code}/chairAssumed` | `{success, requestId}` | Chair takeover confirmed |
| `/user/queue/error` | `RoomError` | Chair access denial — sent to specific session only |

## Frontend API Usage

Frontend pages fetch REST endpoints at page load and use STOMP for all live updates:
- `landing.html` — calls `POST /api/rooms` and `GET /api/rooms/{code}` via `fetch()`
- `chair.html`, `participant.html` — subscribe to `/topic/room/{code}/state` and related topics
- `metadata-loader.js` — fetches `/api/metadata/*` once, caches in `localStorage` with version key `speek_metadata_{type}`
