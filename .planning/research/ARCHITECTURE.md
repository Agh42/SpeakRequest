# Architecture Research

**Domain:** real-time meeting moderation UI within a Spring Boot monolith
**Researched:** 2026-03-28
**Confidence:** HIGH

## Standard Architecture

### System Overview

```
┌──────────────────────────────────────────────────────────────────────┐
│ Browser surfaces                                                     │
│ chair.html / participant.html / popout.html / landing.html          │
│ static JS renders full room snapshots from STOMP                     │
└──────────────────────────────────────┬───────────────────────────────┘
                                       │ WebSocket /ws + HTTP /api/*
┌──────────────────────────────────────▼───────────────────────────────┐
│ Infrastructure adapter                                               │
│ MeetingController                                                     │
│ - REST routes                                                         │
│ - STOMP message handlers                                              │
│ - broadcast(roomCode) after every mutation                            │
│ - SessionDisconnectEvent cleanup                                      │
└──────────────────────────────────────┬───────────────────────────────┘
                                       │ direct domain mutation
┌──────────────────────────────────────▼───────────────────────────────┐
│ Domain aggregate                                                      │
│ Room                                                                  │
│ - queue, current speaker, timer, poll, config, chair session         │
│ - persistent room-member map keyed by websocket session              │
│ - snapshot() builds immutable State                                   │
└──────────────────────────────────────┬───────────────────────────────┘
                                       │ in-memory lookup / tracking
┌──────────────────────────────────────▼───────────────────────────────┐
│ In-memory repository                                                  │
│ RoomRepository                                                        │
│ - roomsByCode                                                         │
│ - sessionToRoomCode                                                   │
│ - eviction / destroy cleanup                                          │
└──────────────────────────────────────────────────────────────────────┘
```

### Component Responsibilities

| Component | Responsibility | Typical Implementation |
|-----------|----------------|------------------------|
| MeetingController | Orchestrates all REST and STOMP entry points, mutates the Room aggregate, and rebroadcasts a full snapshot after each change | Thin controller with message handlers and a shared broadcast helper |
| Room | Owns all meeting state and concurrency rules, including chair session identity, queue, timer, poll, config, and room-member presence | Mutable aggregate with a lock and immutable snapshot records |
| RoomRepository | Resolves room code to Room instance and tracks session-to-room membership for disconnect cleanup | In-memory concurrent maps plus eviction bookkeeping |
| State and child records | Carry the serialized room snapshot to every client | Immutable records consumed directly by static JS |
| Static client pages | Render the snapshot and emit STOMP commands; no client-side source of truth | Vanilla JS in chair/participant/popout HTML |

## Recommended Project Structure

The current package split is already the right boundary for v1.1.

```
src/main/java/de/koderman/
├── infrastructure/   # controller and transport concerns
├── domain/           # Room aggregate, value records, exceptions, enums
└── config/           # WebSocket and startup configuration
src/main/resources/static/
├── chair.html        # chair-specific rendering and commands
├── participant.html  # participant surface
└── popout.html       # projector / secondary display surface
```

### Structure Rationale

- **`domain/`** should keep the new room-member value object and any snapshot record changes, because the persistence change belongs to meeting state, not transport.
- **`infrastructure/`** should remain thin. It already owns join/disconnect orchestration and is the correct place to attach room-member lifecycle calls.
- **`static/`** stays snapshot-driven. The new UI work is a rendering and navigation change, not a new frontend application.

## Architectural Patterns

### Pattern 1: Snapshot-First Aggregate

**What:** Every mutation updates `Room`, then the controller broadcasts the full `State` snapshot to `/topic/room/{code}/state`.
**When to use:** Any time clients must stay in sync with meeting state without local reconciliation logic.
**Trade-offs:** Simple and robust, but it pushes a larger payload on every change.

**Example:**
```java
room.updateRoomConfig(sessionId, topic, meetingGoal, participationFormat, decisionRule, deliverable);
broadcast(roomCode);
```

### Pattern 2: Session-Keyed Presence Inside `Room`

**What:** Keep room members in a map keyed by websocket session ID inside the Room aggregate, then emit a list in the snapshot.
**When to use:** When presence must survive queue churn and chair/current speaker changes.
**Trade-offs:** Slightly more domain state, but it avoids a second presence service and keeps membership aligned with the existing session lifecycle.

**Example:**
```java
private final Map<String, RoomMember> membersBySessionId = new HashMap<>();
```

### Pattern 3: Anchor Navigation for Chair Affordances

**What:** UI actions such as "click topic to edit" should scroll to the existing room-menu section instead of introducing a new route.
**When to use:** When the target already exists in the single-page chair workflow.
**Trade-offs:** Zero backend cost, but it depends on the page keeping stable section IDs.

## Data Flow

### Request Flow

```
Chair / participant action
    ↓
STOMP message to MeetingController
    ↓
Room mutation under lock
    ↓
broadcast(roomCode)
    ↓
State snapshot to /topic/room/{code}/state
    ↓
Static JS re-renders the view
```

### State Management

```
Room
    ├── queue
    ├── current
    ├── poll state
    ├── room config
    └── membersBySessionId   <-- new presence store

State snapshot
    ├── queue
    ├── current
    ├── pollState
    ├── roomConfig
    ├── chairOccupied
    └── members              <-- new client-facing presence list
```

### Key Data Flows

1. **Join / reconnect:** the controller tracks the session in `RoomRepository`, the Room registers or refreshes the member entry for that session, and the next broadcast makes the avatar stable on the chair surface.
2. **Queue / current speaker updates:** queue and current speaker continue to use the existing `Participant` and `Current` records. Room-member presence is independent, so avatars do not disappear when a participant leaves the queue or becomes the current speaker.
3. **Timer warnings:** timer color changes remain a client-side concern. The backend keeps emitting the same `Current` data; the chair, participant, and popout renderers compute warn/danger thresholds from `limitSec` and elapsed time.
4. **Topic editing affordance:** clicking the topic label in the chair view should jump to the existing room-menu section. That is a pure client-side navigation change; no new API is needed.

## Integration Points

### External Services

There are no external runtime services. This milestone stays inside the existing monolith and STOMP channel.

### Internal Boundaries

| Boundary | Communication | Notes |
|----------|---------------|-------|
| MeetingController → Room | Direct method calls | Best place to register and release room members alongside join/disconnect events |
| Room → State | Immutable snapshot | Add member presence to the snapshot, not a second runtime model |
| State → chair.html | STOMP broadcast payload | Chair table rendering should read room members from the snapshot |
| State → participant.html / popout.html | STOMP broadcast payload | These pages keep their current queue/current rendering and only need timer threshold updates |
| topic label click → room menu section | DOM scroll / anchor navigation | Reuse the existing `section-menu` anchor instead of a new route |

## Anti-Patterns

### Anti-Pattern 1: Separate Presence Service

**What people do:** Introduce a second store or service for room members.
**Why it's wrong:** It splits the meeting state across two sources of truth and makes disconnect cleanup harder.
**Do this instead:** Keep membership inside `Room` and project it through the existing snapshot.

### Anti-Pattern 2: Deriving Avatars from Queue Only

**What people do:** Use queue entries as the only source of avatar data.
**Why it's wrong:** Avatars disappear whenever a participant leaves the queue, which is exactly the bug this milestone is addressing.
**Do this instead:** Render avatars from persistent room-member presence.

### Anti-Pattern 3: New Route for Topic Editing

**What people do:** Add a dedicated edit route for the topic label interaction.
**Why it's wrong:** It adds navigation and state complexity for a problem already solved by the single-page chair layout.
**Do this instead:** Scroll to the existing menu section.

## Scaling Considerations

| Scale | Architecture Adjustments |
|-------|--------------------------|
| 0-1k users | The current in-memory Room aggregate is sufficient; keep snapshots full and simple |
| 1k-100k users | Optimize client rendering and consider trimming snapshot payloads if room-member lists grow large |
| 100k+ users | Revisit in-memory room storage and eviction policy before changing the UI architecture |

### Scaling Priorities

1. **First bottleneck:** client rendering of the chair table, because avatar placement and reflow happen on every broadcast.
2. **Second bottleneck:** snapshot size, because a persistent room-member list increases the payload for each broadcast.

## Build Order

The build order should follow dependency, not visual convenience.

1. **Add room-member persistence in `Room` and `State`.** This is the foundation. Without a snapshot field for members, the chair UI cannot keep avatars visible across queue changes or disconnect/rejoin cycles.
2. **Wire lifecycle transitions in `MeetingController`.** Register or refresh member presence on join and assume-chair flows, and release it on disconnect and room destroy cleanup. This keeps the server-side lifecycle consistent with the existing session-to-room tracking.
3. **Update the chair renderer to consume members from the snapshot.** Render avatar circles from the new member list, while still using queue/current for speaking order and status.
4. **Apply the chair title and room-title presentation fixes.** Clamp the room title to two lines with ellipsis in the chair surface after the new snapshot is available, so the layout can be tuned against the final room-member rendering.
5. **Adjust timer warning thresholds in all current surfaces.** Update the chair, participant, and popout timer class logic to use 25% and 10% remaining as the visible thresholds; this is UI-only and does not need backend changes.
6. **Add the clickable topic label in the chair view.** Make the topic element scroll to the existing room-menu section. This is lowest risk and should land last because it depends only on stable section IDs.

### Why This Order

- The persistent member list is a data-model change, so it must land before any avatar rendering that depends on it.
- Controller lifecycle wiring must follow the model change or the snapshot will not stay accurate across join and disconnect events.
- Timer color changes are independent of membership, so they can be done in parallel with the rendering work, but they do not unblock it.
- The topic click is pure interaction polish and has no server dependency, so it is the safest final step.

## Sources

- [.planning/PROJECT.md](../PROJECT.md)
- [.planning/codebase/ARCHITECTURE.md](../codebase/ARCHITECTURE.md)
- [.planning/codebase/INTEGRATIONS.md](../codebase/INTEGRATIONS.md)
- [src/main/java/de/koderman/infrastructure/MeetingController.java](../../src/main/java/de/koderman/infrastructure/MeetingController.java)
- [src/main/java/de/koderman/domain/Room.java](../../src/main/java/de/koderman/domain/Room.java)
- [src/main/java/de/koderman/domain/State.java](../../src/main/java/de/koderman/domain/State.java)
- [src/main/java/de/koderman/domain/RoomRepository.java](../../src/main/java/de/koderman/domain/RoomRepository.java)
- [src/main/resources/static/chair.html](../../src/main/resources/static/chair.html)
- [src/main/resources/static/participant.html](../../src/main/resources/static/participant.html)
- [src/main/resources/static/popout.html](../../src/main/resources/static/popout.html)

---
*Architecture research for: v1.1 UI Improvements milestone*
*Researched: 2026-03-28*
