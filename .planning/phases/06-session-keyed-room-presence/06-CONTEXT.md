# Phase 6: Session-Keyed Room Presence - Context

**Gathered:** 2026-03-28
**Status:** Ready for planning

<domain>
## Phase Boundary

Persist speaker membership by session and keep avatars stable until disconnect or replacement. Room members must live in Room state keyed by WebSocket session ID, and the existing STOMP/static-page architecture stays in place.

</domain>

<decisions>
## Implementation Decisions

### Room presence model
- **D-01:** Room owns the authoritative member presence map keyed by WebSocket session ID.
- **D-02:** The member map is part of the Room snapshot state, so chair/participant/popout renderers can consume a single broadcast source of truth.
- **D-03:** Presence is session-scoped, not name-scoped; display names remain attached to the session record rather than acting as identity.

### Request and replacement behavior
- **D-04:** The first time a session requests to speak, the server stores that participant name for that session ID.
- **D-05:** If the same session requests again with a different name, the server replaces the stored name for that session instead of creating a duplicate member.
- **D-06:** A repeated request from the same session should keep the member entry stable enough that the chair avatar does not disappear and reappear as a new person.

### Disconnect cleanup
- **D-07:** When a client session disconnects, the server removes that session's member entry from Room state before the next broadcast.
- **D-08:** RoomRepository keeps handling session-to-room tracking and disconnect cleanup for room membership bookkeeping, but the actual visible member record is removed from Room.

### Chair rendering contract
- **D-09:** The chair continues to render from `/topic/room/{roomCode}/state` snapshots rather than a new event stream or route.
- **D-10:** Existing queue/current-speaker rendering remains in place; Phase 6 adds stable member presence, not a second roster model.

### the agent's Discretion
- Exact internal Room data structure naming, as long as session-keyed member persistence is authoritative and snapshot-driven.
- Whether member addition/update is centralized in one Room method or split across request/join helpers, as long as the behavior above is preserved.

</decisions>

<specifics>
## Specific Ideas

- "members obvously live in the room state when they are added server side to the Room entity"
- "attach names to the client-ids"
- "remove names when the client session disconnects"
- "when the client 'requests to speak' for the first time, store his name for the sesisonid in a hashmap"
- "when he requests with another name, remove the first one and replace with the new one"

</specifics>

<canonical_refs>
## Canonical References

### Phase scope and requirements
- [ROADMAP.md](../../ROADMAP.md) — Phase 6 goal, dependency chain, and success criteria.
- [REQUIREMENTS.md](../../REQUIREMENTS.md) — PRES-01 through PRES-04 define the required presence behavior.
- [PROJECT.md](../../PROJECT.md) — milestone-level constraints: keep the existing STOMP/static architecture and extend Room state.
- [STATE.md](../../STATE.md) — current milestone status and recorded project concerns.

### Existing room-state and lifecycle code
- [src/main/java/de/koderman/domain/Room.java](../../../src/main/java/de/koderman/domain/Room.java) — core aggregate, queue/timer/poll state, snapshot construction.
- [src/main/java/de/koderman/domain/RoomRepository.java](../../../src/main/java/de/koderman/domain/RoomRepository.java) — session-to-room tracking and cleanup.
- [src/main/java/de/koderman/domain/State.java](../../../src/main/java/de/koderman/domain/State.java) — current snapshot payload contract.
- [src/main/java/de/koderman/infrastructure/MeetingController.java](../../../src/main/java/de/koderman/infrastructure/MeetingController.java) — join/request/withdraw/assumeChair/disconnect lifecycle wiring and state broadcast helper.

### Existing chair rendering patterns
- [src/main/resources/static/chair.html](../../../src/main/resources/static/chair.html) — current state subscription, table rendering, topic/menu navigation patterns, and timer helpers.
- [src/main/resources/static/participant.html](../../../src/main/resources/static/participant.html) — parallel snapshot-driven rendering and timer thresholds.
- [src/main/resources/static/popout.html](../../../src/main/resources/static/popout.html) — secondary display snapshot rendering and timer behavior.

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- `Room` queue/current/timer/poll/config machinery: reuse the existing aggregate and add presence alongside it.
- `MeetingController.broadcast(...)`: keep the snapshot-after-mutation pattern intact.
- `RoomRepository` session tracking: reuse the existing session-to-room lifecycle as the cleanup trigger.
- Static chair/participant/popout snapshot rendering: extend the current rendering path rather than introducing new routes.

### Established Patterns
- Snapshot-first state flow: mutate `Room`, then broadcast the full `State` snapshot to `/topic/room/{roomCode}/state`.
- Session-based chair authority: the existing implementation already treats WebSocket session IDs as the authoritative identity for chair-only actions.
- Frontend derives everything from state broadcasts: the browser pages should continue to consume full snapshots instead of maintaining a separate presence source.

### Integration Points
- `Room` state snapshot shape: add member presence to the existing broadcast payload.
- `MeetingController` request/join/disconnect handlers: register, replace, and remove member entries at lifecycle boundaries.
- `chair.html` table rendering: render stable avatar circles from room member presence while preserving the existing queue/current speaker display.

</code_context>

<deferred>
## Deferred Ideas

None — discussion stayed within Phase 6 scope.

</deferred>

---

*Phase: 06-session-keyed-room-presence*
*Context gathered: 2026-03-28*
