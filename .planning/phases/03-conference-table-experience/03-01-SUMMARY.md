---
phase: 03-conference-table-experience
plan: 01
subsystem: ui
tags: [conference-table, desktop-only, responsive, timing]
requires: []
provides:
  - Desktop conference-table scaffold in the center column
  - Mobile hide behavior that preserves the phase-2 shell path
  - Center timing stack with topic-first hierarchy and secondary room metadata
affects: [phase-04, phase-05, mobile-chair-view]
tech-stack:
  added: []
  patterns: [single-page-anchor-navigation, desktop-only-centerpiece]
key-files:
  created: []
  modified: [src/main/resources/static/chair.html]
key-decisions:
  - "Keep the conference table desktop-only so mobile continues to rely on the phase-2 shell sections."
  - "Promote the room topic and current speaker countdown inside the centerpiece while keeping room code secondary."
patterns-established:
  - "The center column can become a dedicated visual surface without changing any backend payloads or routes."
  - "Responsive hiding can preserve shell usability on small screens while the desktop table takes over the focal area."
requirements-completed: [TABLE-01, TABLE-05, TABLE-06]
duration: 6 min
completed: 2026-03-25
---

# Phase 03 Plan 01: Conference Table Scaffold Summary

**The chair view now has a desktop-only round-table scaffold with a dedicated center timing stack, while the mobile shell remains unchanged.**

## Performance

- **Duration:** 6 min
- **Completed:** 2026-03-25
- **Tasks:** 2
- **Files modified:** 1

## Accomplishments
- Replaced the center-shell placeholder with a desktop round-table composition.
- Added the table ring, seat orbit container, and central timing stack.
- Preserved the existing timer and room metadata bindings so later rendering work could reuse the same DOM IDs.

## Files Created/Modified
- `src/main/resources/static/chair.html` - Added the conference-table scaffold, responsive desktop-only visibility, and the center timing hierarchy.

## Decisions Made
- Kept the centerpiece hidden below the desktop breakpoint so mobile remains on the phase-2 shell navigation.
- Treated the room topic as the primary text inside the table center and demoted the room code to supporting metadata.

## Deviations from Plan

None - the scaffold landed as a direct structural replacement.

## Next Phase Readiness
- Phase 3 plan 02 can now render live participant identity and status into the existing table frame.

---
*Phase: 03-conference-table-experience*
*Completed: 2026-03-25*