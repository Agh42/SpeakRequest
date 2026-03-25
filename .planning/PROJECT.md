# SpeakRequest — Chair View Redesign

## What This Is

SpeakRequest is a hybrid meeting manager for discussion moderators and facilitators. It manages speaking turns with a transparent queue, tracks speech timers, and conducts realtime polls. The Chair View ("The Orchestrator") redesign replaces the current functional-but-plain chair.html with a high-end, editorial-inspired command center that introduces a central round conference table showing all participant avatars.

## Core Value

The chair can see every participant's status at a glance — who is speaking, who is next, and who is waiting — without leaving the main screen.

## Current State

Milestone v1.0 is shipped and archived. The chair view now uses the Orchestrator layout, the central conference table, the migrated control panels, and the preserved STOMP / sanitization / metadata contract.

## Next Milestone Goals

- Participant view redesign that matches the chair-view visual language.
- Lightweight motion and state transitions for table and queue updates.
- Popout view refresh if the next milestone chooses to unify secondary surfaces.

## Requirements

### Validated

- ✓ Room creation with 4-char code (A-Z, 1-9) — existing
- ✓ Chair role via WebSocket session ID — existing
- ✓ Speak request queue (request, withdraw, next-speaker) — existing
- ✓ Speech timer with start / pause / next / reset controls — existing
- ✓ Per-speaker time limit configurable by chair — existing
- ✓ Real-time state broadcast via STOMP to `/topic/room/{code}/state` — existing
- ✓ Live polling (yes/no, gradient-of-agreement) — existing
- ✓ Popout view for projector / screen share — existing
- ✓ QR code join flow — existing
- ✓ DOMPurify sanitization of all user-supplied strings — existing
- ✓ Mobile-responsive layout (current chair.html) — existing
- ✓ Chair view now boots with Tailwind + tokenized Orchestrator base palette — validated in Phase 1
- ✓ Manrope/Inter typography and Material Symbols icon system baseline in chair view — validated in Phase 1
- ✓ Tonal no-line styling baseline and consistent text input control language established — validated in Phase 1
- ✓ Full collapsible sidebar navigation — completed in Phase 2
- ✓ Full mockup header — completed in Phase 2
- ✓ Central round conference table — completed in Phase 3
- ✓ Participant avatars and queue badges — completed in Phase 3
- ✓ Conference table hidden on mobile — completed in Phase 3
- ✓ Meeting Controls section reorganized — completed in Phase 4
- ✓ Speaker Queue right-column panel — completed in Phase 4
- ✓ Live Poll glass-panel integrated in left column — completed in Phase 4
- ✓ Share Access section in left column — completed in Phase 4
- ✓ All existing STOMP/WebSocket event wiring preserved and tested — completed in Phase 5

### Active

- [x] Chair view fully reworked to "The Orchestrator" design system (deep-navy dark theme, tonal layering, no explicit borders)
- [x] Tailwind CSS (CDN) + Manrope/Inter Google Fonts adopted in chair.html
- [x] Full collapsible sidebar navigation (Meeting Controls, Speaker Queue, Live Polling, Agenda sections)
- [x] Full mockup header: hamburger toggle, room-code badge, copy-code/copy-link, destroy-room, notifications+settings icons
- [x] Central round conference table — top-down SVG/CSS abstract view with participant avatars
- [x] Participant avatars: colored monogram circles; "speaking now" state (tertiary glow/border); "in-queue" state (numbered badge)
- [x] Conference table hidden on mobile; mobile uses bottom nav anchor (Controls / Queue / Poll / Menu)
- [x] Meeting Controls section reorganized: speaker-name input, proxy-request, withdraw, per-speaker limit, Start/Pause/Next/Reset action buttons
- [x] Speaker Queue right-column panel (scrollable list, current speaker pinned at top, "Force Add Speaker" button)
- [x] Live Poll glass-panel integrated in left column on desktop
- [x] Share Access section (copy code, copy link) in left column on desktop
- [x] All existing STOMP/WebSocket event wiring preserved and tested

### Out of Scope

- Participant view redesign — deferred to next milestone
- Popout view (popout.html) changes — keep as-is
- New backend / API features — UI-only refactor
- CSS animations and transitions — keep static for now
- Profile pictures / external avatar images — initials only

## Context

- **Stack**: Spring Boot monolith, plain vanilla JS + STOMP over WebSocket, static HTML served by Spring.
- **Auth**: No login system. Chair identity = first WebSocket session to call `assumeChairRole()`. Session ID governs chair-only actions.
- **State**: Full room state broadcast after every mutation to `/topic/room/{code}/state`. Clients get the entire state snapshot each time.
- **Design reference**: `src/chair-view-redesign/code.html` (Tailwind + Material Symbols static mockup) and `src/chair-view-redesign/DESIGN.md` (The Orchestrator design system spec).
- **Codebase map**: `.planning/codebase/` contains ARCHITECTURE.md, STACK.md, CONVENTIONS.md, TESTING.md, STRUCTURE.md, INTEGRATIONS.md, CONCERNS.md.

## Constraints

- **No CDN for production-critical assets**: Tailwind CDN is acceptable (already pattern in redesign mockup); fonts via Google Fonts acceptable (already used in mockup)
- **No backend changes**: All changes confined to `src/main/resources/static/chair.html` and `styles.css` (new tokens may be added)
- **DOMPurify required**: All participant names and user-supplied content must continue to be sanitized before DOM insertion
- **Browser compat**: Same as existing app — modern evergreen browsers (Chrome, Firefox, Edge, Safari)

## Key Decisions

| Decision | Rationale | Outcome |
|----------|-----------|---------|
| Adopt Tailwind CDN | Design mockup already uses it; avoids build pipeline complexity | Accepted and implemented in Phase 1 |
| Round table shape | User-selected; all seats equidistant from center | — Pending |
| Initials-only avatars | No profile picture support in backend; initials from participant name | — Pending |
| Table hidden on mobile | Mobile screen too small for meaningful table view; bottom nav serves navigation | — Pending |
| Full sidebar nav | Matches The Orchestrator mockup fidelity | — Pending |

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

---
*Last updated: 2026-03-24 after Phase 1 completion*

## Archived State

- Milestone v1.0 shipped on 2026-03-25.
- Remaining v2 ideas stay deferred until the next milestone is started.
- Phase summaries and milestone archives live under `.planning/milestones/`.

---
*Last updated: 2026-03-25 after v1.0 milestone completion*
