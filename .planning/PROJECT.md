# SpeakRequest

## What This Is

SpeakRequest is a hybrid meeting manager for discussion moderators and facilitators. It manages speaking turns with a transparent queue, tracks speech timers, and conducts realtime polls.

## Core Value

The chair can see every participant's status at a glance - who is speaking, who is next, and who is waiting - without leaving the main screen.

## Current State

**Shipped version: v1.1** — archived 2026-04-22

Both v1.0 and v1.1 are shipped. The chair surface has been fully redesigned with a Tailwind/Orchestrator design system, conference-table presence model, session-keyed room members, timer urgency colors, inline topic editing, and avatar label truncation. The participant view has been restyled to match. All features are wired to the existing STOMP broadcast model.

**Shipped milestones:**
- [v1.0 — Chair View Redesign](.planning/milestones/v1.0-ROADMAP.md) — shipped 2026-03-25
- [v1.1 — UI Improvements](.planning/milestones/v1.1-ROADMAP.md) — shipped 2026-04-22

## Next Milestone Goals

To be defined. Deferred items from v1.1 available for inclusion:
- POP-01: Popout view refresh to match updated UI language
- TIME-04: Overtime presentation (flashing/audio cues)

Run `/gsd-new-milestone` to define requirements and roadmap for the next version.

## Requirements

### Validated

<!-- Shipped and confirmed valuable. -->

- ✓ Room creation with 4-char code (A-Z, 1-9) - existing
- ✓ Chair role via WebSocket session ID - existing
- ✓ Speak request queue (request, withdraw, next-speaker) - existing
- ✓ Speech timer with start / pause / next / reset controls - existing
- ✓ Per-speaker time limit configurable by chair - existing
- ✓ Real-time state broadcast via STOMP to `/topic/room/{code}/state` - existing
- ✓ Live polling (yes/no, gradient-of-agreement) - existing
- ✓ Popout view for projector / screen share - existing
- ✓ QR code join flow - existing
- ✓ DOMPurify sanitization of all user-supplied strings - existing
- ✓ Mobile-responsive layout (current chair.html) - existing
- ✓ Chair view now boots with Tailwind + tokenized Orchestrator base palette - validated in Phase 1
- ✓ Manrope/Inter typography and Material Symbols icon system baseline in chair view - validated in Phase 1
- ✓ Tonal no-line styling baseline and consistent text input control language established - validated in Phase 1
- ✓ Full collapsible sidebar navigation - completed in Phase 2
- ✓ Full mockup header - completed in Phase 2
- ✓ Central round conference table - completed in Phase 3
- ✓ Participant avatars and queue badges - completed in Phase 3
- ✓ Conference table hidden on mobile - completed in Phase 3
- ✓ Meeting Controls section reorganized - completed in Phase 4
- ✓ Speaker Queue right-column panel - completed in Phase 4
- ✓ Live Poll glass-panel integrated in left column - completed in Phase 4
- ✓ Share Access section in left column - completed in Phase 4
- ✓ All existing STOMP/WebSocket event wiring preserved and tested - completed in Phase 5

### Active

- [ ] Persistent room members keyed by WebSocket session stay visible in the room state until disconnect or replacement.
- [ ] Participant avatars continue to render inside the room circle without dropping names once they have joined the speaker set.
- [ ] Room titles clamp to two lines and truncate with ellipsis so they remain readable inside the avatar circle.
- [ ] Large speaking timer changes color at 25% and 10% remaining, then stays red while the elapsed timer continues past zero.
- [ ] The chair can click the topic label to jump to the room menu section for editing.

### Out of Scope

<!-- Explicit boundaries. Includes reasoning to prevent re-adding. -->

- Popout view changes - keep existing surface stable while the room-state and chair improvements land.
- New routes or API surfaces - this milestone is limited to existing surfaces plus the Room entity/state contract.
- Timer overrun counting past zero on the remaining-time display - the remaining timer stays at 00:00 as requested.

## Context

SpeakRequest is a Spring Boot monolith with vanilla JS/STOMP static pages served from the backend. The chair and participant surfaces are already wired to a full room-state broadcast model, so the new milestone should extend the existing Room entity/state instead of introducing a separate model.

## Constraints

- **Tech stack**: Spring Boot monolith with static frontend - avoid introducing new infrastructure or routes unless absolutely necessary.
- **State model**: Room state remains the single source of truth over STOMP - client-side state must continue to derive from broadcasts.
- **Compatibility**: Existing chair controls, sanitization, and room metadata flow must remain intact - this milestone builds on the shipped chair view.

## Key Decisions

<!-- Decisions that constrain future work. Add throughout project lifecycle. -->

| Decision | Rationale | Outcome |
|----------|-----------|---------|
| Preserve STOMP room-state contract | Existing clients rely on the current destinations and message flow | ✓ Good |
| Keep members tied to session IDs in Room state | Allows avatars to remain stable until disconnect or name replacement | — Pending |
| Use color warnings only on the large speaking timer | Remaining-time display should stay at 00:00 after expiry | — Pending |
| Make the topic label jump to the room menu section | Keeps room metadata editing discoverable without adding a new route | — Pending |

## Evolution

This document evolves at phase transitions and milestone boundaries.

**After each phase transition** (via `/gsd-transition`):
1. Requirements invalidated? → Move to Out of Scope with reason
2. Requirements validated? → Move to Validated with phase reference
3. New requirements emerged? → Add to Active
4. Decisions to log? → Add to Key Decisions
5. "What This Is" still accurate? → Update if drifted

**After each milestone** (via `/gsd-complete-milestone`):
1. Full review of all sections
2. Core Value check — still the right priority?
3. Audit Out of Scope — reasons still valid?
4. Update Context with current state

## Archived State

- Milestone v1.0 shipped on 2026-03-25.
- Remaining v2 ideas stay deferred until the next milestone is started.
- Phase summaries and milestone archives live under `.planning/milestones/`.

---
*Last updated: 2026-03-28 after v1.1 milestone start*
