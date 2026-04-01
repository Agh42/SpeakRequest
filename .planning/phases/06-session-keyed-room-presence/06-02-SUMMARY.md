---
phase: 06-session-keyed-room-presence
plan: 02
subsystem: ui
tags: [stomp, websocket, snapshot, room-presence, chair-ui]
requires:
  - phase: 05-foundation
    provides: Room aggregate, snapshot broadcast, and session tracking patterns
provides:
  - Chair proxy requests append visible room members without overwriting chair presence
  - Chair proxy withdraw removes the matching visible member entries before broadcast
  - Chair avatar labels render with fallback and truncation rules from the snapshot state
affects: [phase-07-chair-surface-readability, phase-08-timer-urgency-and-topic-editing]
tech-stack:
  added: []
  patterns: [snapshot-first room state, session-keyed presence, chair-proxy member appends]
key-files:
  created: []
  modified: [src/main/java/de/koderman/domain/Room.java, src/main/java/de/koderman/infrastructure/MeetingController.java, src/main/resources/static/chair.html, src/test/java/de/koderman/MeetingControllerPresenceTest.java, src/test/java/de/koderman/RoomMemberPresenceTest.java]
key-decisions:
  - "Chair proxy requests append distinct member and queue entries without replacing the chair's self presence"
  - "Withdraw removes visible member entries by name before broadcasting the snapshot"
  - "Chair avatar labels use fallback markers for empty names and truncated labels for longer names"
patterns-established:
  - "Proxy-driven chair actions can add multiple visible members from one WebSocket session"
  - "Snapshot member rendering remains the single source of truth for the chair circle"
requirements-completed: [PRES-01, PRES-02, PRES-03, PRES-04]
completed: 2026-04-01
---

# Phase 6: Session-Keyed Room Presence Gap Closure Summary

**The remaining chair-proxy presence gap is closed: chair on-behalf requests now create distinct snapshot members and queue items, and withdraw removes the matching visible entries.**

## Performance

- **Duration:** N/A
- **Started:** N/A
- **Completed:** 2026-04-01
- **Tasks:** 2 implementation tasks
- **Files modified:** 5

## Accomplishments
- Kept normal session-keyed replacement behavior intact for participant self-requests.
- Added chair-proxy member appends so the chair can queue multiple on-behalf participants from one session without overwriting the chair avatar.
- Routed chair-proxy requests to unique queue entries so repeated proxy requests no longer collapse into one queue item.
- Removed matching visible members on withdraw and preserved disconnect cleanup for session-owned presence.
- Updated the chair avatar helper to render `??` for empty labels and truncate longer labels with an ellipsis.
- Extended regression tests to cover chair-proxy appends, withdraw cleanup, and the preserved normal replacement path.

## Files Created/Modified
- `src/main/java/de/koderman/domain/Room.java` - added proxy-member append and name-based removal helpers.
- `src/main/java/de/koderman/infrastructure/MeetingController.java` - branched chair-proxy requests and unique queue IDs, plus withdraw member cleanup.
- `src/main/resources/static/chair.html` - avatar label helper now uses fallback and bounded labels.
- `src/test/java/de/koderman/MeetingControllerPresenceTest.java` - controller regression coverage for chair proxy append and withdraw cleanup.
- `src/test/java/de/koderman/RoomMemberPresenceTest.java` - room regression coverage for proxy appends and removal behavior.

## Decisions Made
- Preserved the existing `/topic/room/{roomCode}/state` broadcast contract.
- Kept the normal replacement path session-keyed, while chair proxy actions append additional visible entries for the chair session.
- Left queue and snapshot rendering on the existing static chair page.

## Issues Encountered
- None after the targeted regression tests passed.

## User Setup Required
None.

## Next Phase Readiness
Phase 6 is fully complete and Phase 7 can now be planned or executed without changing the STOMP contract.

---
*Phase: 06-session-keyed-room-presence*
*Completed: 2026-04-01*