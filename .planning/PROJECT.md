# SpeakRequest

## What This Is

SpeakRequest is a hybrid meeting manager for discussion moderators and facilitators. It manages speaking turns with a transparent queue, tracks speech timers, and conducts realtime polls.

## Core Value

The chair can see every participant's status at a glance - who is speaking, who is next, and who is waiting - without leaving the main screen.

## Current Milestone: v1.2 Improved Voting

**Goal:** Deliver the existing dot voting feature as a fully verified, labeled, and tested capability across chair, participant, and popout views.

**Target features:**
- End-to-end verification: chair creates dot poll → participants vote with up/down dots → results display correctly
- Label accuracy: "votes received" / "Total votes" → context-correct labels for dot voting
- Backend unit tests for dot voting logic (add/remove dots, per-session limit enforcement)
- Chair config UX: 'Dots per participant' field shows/hides correctly
- Participant UX: up/down arrows correct at boundary conditions (0 and max dots)
- Popout view: dot voting state visible during active poll

**Out of scope:** No new poll types beyond dot voting.

## Current State

**Shipped version: v1.1** — archived 2026-04-22

Both v1.0 and v1.1 are shipped. The chair surface has been fully redesigned with a Tailwind/Orchestrator design system, conference-table presence model, session-keyed room members, timer urgency colors, inline topic editing, and avatar label truncation. The participant view has been restyled to match. All features are wired to the existing STOMP broadcast model.

**Shipped milestones:**
- [v1.0 — Chair View Redesign](.planning/milestones/v1.0-ROADMAP.md) — shipped 2026-03-25
- [v1.1 — UI Improvements](.planning/milestones/v1.1-ROADMAP.md) — shipped 2026-04-22

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

- [ ] Chair can start a dot voting poll with a configurable number of dots per participant.
- [ ] Participant can distribute dots across options using up/down controls; total dots are capped at the configured limit.
- [ ] Chair and participant views display correct dot-specific labels ('Dots per participant', 'Dots placed', 'Total dots') rather than generic vote labels.
- [ ] Popout view reflects dot voting in-progress and result states correctly.
- [ ] Backend enforces per-session dot limits and correctly handles dot removal via the `_DOWN` vote suffix.
- [ ] Dot voting logic is covered by backend unit tests.

### Out of Scope

<!-- Explicit boundaries. Includes reasoning to prevent re-adding. -->

- New poll types beyond dot voting — scope is limited to verifying and fixing the existing dot voting implementation.
- New routes or API surfaces — this milestone only touches existing poll infrastructure.
- Timer overrun counting past zero on the remaining-time display — the remaining timer stays at 00:00 as requested.
- Deferred from v1.1: POP-01 popout UI refresh, TIME-04 overtime audio/flash cues — deferred to a future milestone.

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
| Dot voting uses `_DOWN` suffix for vote removal | Keeps the `CastVote` wire format to a single `vote` string; down-removal is a convention, not a separate message type | — Active |
| Dot voting `totalVotes` = total dots placed (not participants) | Percentage shows share of dots per option, not participant headcount | — Active |

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
*Last updated: 2026-06-02 — milestone v1.2 improved-voting started*
