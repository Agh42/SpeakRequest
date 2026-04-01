---
phase: 06-session-keyed-room-presence
plan: 01
subsystem: ui
tags: [stomp, websocket, snapshot, room-presence, chair-ui]
requires:
  - phase: 05-foundation
    provides: Room aggregate, snapshot broadcast, and session tracking patterns
provides:
  - Session-keyed room member presence in Room state
  - Disconnect cleanup that removes member presence before broadcast
  - Chair avatar rendering driven by the room snapshot member list
affects: [phase-07-chair-surface-readability, phase-08-timer-urgency-and-topic-editing]
tech-stack:
  added: []
  patterns: [snapshot-first room state, session-keyed presence, stable chair avatar ordering]
key-files:
  created: [src/main/java/de/koderman/domain/RoomMember.java, src/test/java/de/koderman/RoomMemberPresenceTest.java, src/test/java/de/koderman/MeetingControllerPresenceTest.java]
  modified: [src/main/java/de/koderman/domain/Room.java, src/main/java/de/koderman/domain/State.java, src/main/java/de/koderman/infrastructure/MeetingController.java, src/main/resources/static/chair.html, src/test/java/de/koderman/WebSocketErrorHandlingTest.java]
key-decisions:
  - "Room owns session-keyed member presence and exposes it through snapshots"
  - "Same-session requests replace the stored member name instead of duplicating presence"
  - "Disconnect removes the member entry before the next broadcast"
patterns-established:
  - "Room snapshot now includes durable member presence alongside queue and current speaker state"
  - "Chair rendering derives avatar circles from snapshot members rather than transient queue entries"
requirements-completed: [PRES-01, PRES-02, PRES-03, PRES-04]
completed: 2026-03-29
---

# Phase 6: Session-Keyed Room Presence Summary

**Session-keyed room presence now flows through Room state, with chair avatars rendered from durable member snapshots.**

## Performance

- **Duration:** N/A
- **Started:** N/A
- **Completed:** 2026-03-29
- **Tasks:** 2 implementation tasks + 1 human-verify checkpoint in the plan
- **Files modified:** 7

## Accomplishments
- Added a `RoomMember` snapshot model and extended `State` so room broadcasts now carry session-keyed member presence.
- Wired join, request, and disconnect handling so member names are stored, replaced, and removed by WebSocket session ID.
- Updated the chair table renderer to drive avatar circles from `state.members`, keeping the visual ring stable across repeated same-session requests.
- Added focused tests covering presence upsert, replacement, removal, controller lifecycle behavior, and the updated request error path.

## Files Created/Modified
- `src/main/java/de/koderman/domain/RoomMember.java` - Session-keyed room member record.
- `src/main/java/de/koderman/domain/Room.java` - Member presence map, snapshot expansion, and queue item replacement by session ID.
- `src/main/java/de/koderman/domain/State.java` - Snapshot payload now includes room members.
- `src/main/java/de/koderman/infrastructure/MeetingController.java` - Join/request/disconnect lifecycle wiring for member presence.
- `src/main/resources/static/chair.html` - Avatar ring now renders from snapshot members.
- `src/test/java/de/koderman/RoomMemberPresenceTest.java` - Presence lifecycle coverage.
- `src/test/java/de/koderman/MeetingControllerPresenceTest.java` - Controller lifecycle coverage.
- `src/test/java/de/koderman/WebSocketErrorHandlingTest.java` - Updated request signature coverage.

## Decisions Made
- Kept room presence authoritative inside `Room` rather than adding a parallel roster store.
- Used WebSocket session ID as the stable identity for member replacement and disconnect cleanup.
- Preserved the existing `/topic/room/{roomCode}/state` broadcast contract for the chair view.

## Deviations from Plan

### Auto-fixed Issues

**1. Unused helper removed from MeetingController**
- **Found during:** Test validation after wiring request presence
- **Issue:** The old queue ID helper became dead code after switching request identity to session ID.
- **Fix:** Removed the unused method to keep the controller clean and satisfy compile checks.
- **Files modified:** `src/main/java/de/koderman/infrastructure/MeetingController.java`
- **Verification:** `get_errors` returned no issues.
- **Committed in:** Not committed in this turn

**Total deviations:** 1 auto-fixed
**Impact on plan:** No scope creep; the extra change was required for correctness and compile cleanliness.

## Issues Encountered
- The initial broad patch had to be split into smaller edits because the editor bridge rejected the first large diff.
- The request handler signature changed, so the existing error-handling test needed an explicit session header mock.

## User Setup Required
None - no external service configuration required.

## Next Phase Readiness
Phase 6 is ready for the human verification checkpoint. The next phase can build on session-keyed member presence without changing the STOMP contract.

---
*Phase: 06-session-keyed-room-presence*
*Completed: 2026-03-29*
