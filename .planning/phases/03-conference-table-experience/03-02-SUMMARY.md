---
phase: 03-conference-table-experience
plan: 02
subsystem: ui
tags: [avatars, status-highlight, queue-badges, live-state]
requires:
  - phase: 03-01
    provides: Desktop conference-table scaffold in the center column
provides:
  - Monogram participant avatars with stable name-derived colors
  - Current-speaker highlight treatment and explicit queue position badges
  - Center hierarchy that keeps the speaker countdown primary and supporting metadata secondary
affects: [phase-04, phase-05]
tech-stack:
  added: []
  patterns: [state-driven-seat-rendering, stable-participant-colors, tertiary-highlight]
key-files:
  created: []
  modified: [src/main/resources/static/chair.html]
key-decisions:
  - "Derive seat identity from the existing room snapshot instead of adding new backend fields."
  - "Use a stable hash palette and initials-only avatars so the table reads clearly at a glance."
  - "Render the current speaker as a highlighted seat and label queued participants with numbered badges."
patterns-established:
  - "The live table can be rebuilt directly from the current state snapshot on every update."
  - "Current speaker identity, queue order, and timing can share one centerpiece without altering STOMP behavior."
requirements-completed: [TABLE-02, TABLE-03, TABLE-04]
duration: 7 min
completed: 2026-03-25
---

# Phase 03 Plan 02: Participant Status Rendering Summary

**The conference table now renders live participant seats with monogram avatars, stable colors, queue badges, and a highlighted current speaker.**

## Performance

- **Duration:** 7 min
- **Completed:** 2026-03-25
- **Tasks:** 2
- **Files modified:** 1

## Accomplishments
- Added state-to-seat rendering helpers that derive initials and stable accent colors from the existing room snapshot.
- Rendered the current speaker with the tertiary-highlight treatment and added numbered badges for waiting participants.
- Kept the speaker countdown dominant while showing room code, total meeting time, and current speaker identity as secondary center metadata.

## Files Created/Modified
- `src/main/resources/static/chair.html` - Added the live conference-table rendering path, seat styling, and center metadata updates.

## Decisions Made
- Used DOMPurify on all name-derived output so the table stays aligned with the existing sanitization contract.
- Chose a deterministic palette mapping so each participant keeps a stable visual identity across updates.

## Deviations from Plan

None - the live rendering stayed within the existing state payload and STOMP contract.

## Next Phase Readiness
- Phase 4 can now migrate the remaining control panels into the updated chair shell.

---
*Phase: 03-conference-table-experience*
*Completed: 2026-03-25*