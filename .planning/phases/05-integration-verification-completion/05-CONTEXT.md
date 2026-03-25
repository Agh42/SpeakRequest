# Phase 5: Integration Verification & Completion - Context

**Gathered:** 2026-03-25 (planning synthesis)
**Status:** Ready for planning

<domain>
## Phase Boundary

Finish the chair-view redesign by verifying that the migrated Orchestrator shell still preserves the existing STOMP-driven behavior, sanitization, and room-metadata visibility end to end. This phase does not add new routes, backend features, or alternate event destinations.

</domain>

<decisions>
## Implementation Decisions

### Existing STOMP and DOM Contract
- **D-01:** The redesigned chair view must keep the existing `/app/room/{roomCode}/...` publish destinations and `/topic/room/{roomCode}/...` subscriptions intact.
- **D-02:** The phase validates the already-migrated chair controls against the live DOM IDs in `chair.html` rather than introducing new UI contracts.

### Sanitization
- **D-03:** All participant names and other user-supplied strings continue to be sanitized before they reach `innerHTML` or visible text nodes.

### Room Metadata
- **D-04:** The room configuration metadata remains visible in the settings/sidebar area; it is not moved to a separate page or new route.

### the agent's Discretion
- Exact ordering and wording of browser smoke-check steps.
- Whether minor chair.html cleanup is needed to keep the bindings aligned, as long as existing STOMP destinations and DOM IDs stay unchanged.

</decisions>

<canonical_refs>
## Canonical References

### Phase scope and acceptance criteria
- `.planning/ROADMAP.md` — Phase 5 goal, dependency on Phase 4, and migration completion criteria.
- `.planning/REQUIREMENTS.md` — `MIG-01` through `MIG-04`.
- `.planning/PROJECT.md` — milestone-level constraints, especially no backend changes and no new routes.
- `.planning/STATE.md` — current milestone status and known backend test constructor mismatch.
- `.planning/phases/04-functional-control-migration/04-CONTEXT.md` — Phase 4 DOM and STOMP contract decisions.
- `.planning/phases/04-functional-control-migration/04-01-SUMMARY.md` — Meeting Controls and Speaker Queue migration summary.
- `.planning/phases/04-functional-control-migration/04-02-SUMMARY.md` — Live Poll and Share Access migration summary.

### Design and code references
- `src/chair-view-redesign/DESIGN.md` — Orchestrator surface hierarchy and no-line rule.
- `src/chair-view-redesign/code.html` — structural mockup for the control panels and sidebar/menu placement.
- `src/main/resources/static/chair.html` — live DOM, state update functions, STOMP event wiring, and sanitization paths.
- `.planning/codebase/CONVENTIONS.md` — frontend sanitization and state-driven rendering conventions.
- `.planning/codebase/INTEGRATIONS.md` — STOMP destinations and metadata endpoints.

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- `src/main/resources/static/chair.html`: `updateUI(state)`, `updatePollUI(state)`, `updateRoomConfigUI(state)`, `renderConferenceTable(state)`, `renderQueuePanel(state)`, and the existing chair send handlers already carry the live contracts.
- `metadata-loader.js` and `share.js`: existing browser helpers for metadata caching and sharing behavior.
- `purify.min.js`: the sanitization dependency already loaded by the chair page.

### Integration Points
- `/topic/room/{roomCode}/state` remains the single source of truth for the UI.
- `/topic/room/{roomCode}/destroyed`, `/topic/room/{roomCode}/error`, `/topic/room/{roomCode}/chairAssumed`, and `/user/queue/error` must continue to flow into the redesigned UI.
- Existing publish actions for join, assumeChair, request, withdraw, next, timer, setLimit, poll/start, poll/end, poll/close, poll/cancel, updateConfig, and destroy remain the wiring target.
- Room metadata stays visible through the existing config form and `updateRoomConfigUI(state)`.

</code_context>

---
*Phase: 05-integration-verification-completion*
*Context gathered: 2026-03-25*