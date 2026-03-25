---
phase: 04-functional-control-migration
plan: 01
subsystem: ui
tags: [meeting-controls, speaker-queue, queue-summary, force-add]
requires: []
provides:
  - Migrated left-column Meeting Controls card with preserved chair action IDs
  - Right-column Speaker Queue card with pinned current-speaker summary and force-add action
affects: [phase-05]
tech-stack:
  added: []
  patterns: [state-driven-rendering, preserved-stomp-contract, queue-summary-card]
key-files:
  created: []
  modified: [src/main/resources/static/chair.html]
key-decisions:
  - "Kept the existing request, withdraw, timer, limit, and popout IDs so the chair action contract stayed intact."
  - "Rendered the queue as an operational surface with the current speaker pinned above the waiting list and a visible force-add button."
patterns-established:
  - "Phase 4 can reshape the control surface without changing the current STOMP destinations or room snapshot model."
  - "Queue state can be rendered into a dedicated summary card while the conference table remains the center-column primary view."
requirements-completed: [CTRL-01, CTRL-02, CTRL-03, CTRL-04]
duration: pending-checkpoint
completed: 2026-03-25
---

# Phase 04 Plan 01: Meeting Controls and Speaker Queue Summary

**The chair view now keeps the existing control contract while presenting the Meeting Controls and Speaker Queue as dedicated operational cards in the redesigned shell.**

## Performance

- **Duration:** pending-checkpoint
- **Completed:** 2026-03-25
- **Tasks:** 2
- **Files modified:** 1

## Accomplishments
- Rebuilt the Meeting Controls card into the new left-column shell while preserving the existing request, withdraw, limit, timer, and popout element IDs.
- Moved the queue into a dedicated right-column card with a pinned current-speaker summary, a waiting-speaker list, and a visible force-add button.
- Kept the queue render driven by the live room snapshot and the existing `updateUI(state)` flow.

## Files Created/Modified
- `src/main/resources/static/chair.html` - Migrated the Meeting Controls and Speaker Queue layouts and added the queue summary render path.

## Decisions Made
- Treated the force-add action as a chair-visible shortcut that reuses the existing speak-request contract rather than introducing a new backend route.
- Preserved the current chair DOM IDs so existing STOMP handlers and browser behaviors continue to bind to the same elements.

## Deviations from Plan

None - the control migration stayed within the approved chair action contract.

## Next Phase Readiness
- Phase 4 plan 02 can now validate the migrated poll/share surfaces and finish the browser check once the checkpoint is confirmed.

---
*Phase: 04-functional-control-migration*
*Completed: 2026-03-25*