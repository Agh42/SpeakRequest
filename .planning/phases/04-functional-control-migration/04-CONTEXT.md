# Phase 4: Functional Control Migration - Context

**Gathered:** 2026-03-25 (batch discussion)
**Status:** Ready for planning

<domain>
## Phase Boundary

Rebuild the chair-facing control areas in the redesigned layout so the chair can operate the meeting from the new shell. This phase covers Meeting Controls, Speaker Queue, Live Poll, and Share Access in the Orchestrator layout without adding backend behavior, new routes, or changing the already-migrated center table.

</domain>

<decisions>
## Implementation Decisions

### Meeting Controls
- **D-01:** The existing speaker-control actions stay on their current STOMP destinations and are rebuilt into the new left-column Meeting Controls card rather than being replaced with new APIs.
- **D-02:** The Meeting Controls card keeps the chair proxy request/withdraw input, the per-speaker limit field, and the Start / Pause / Next / Reset timer actions as the chair's primary operational controls.
- **D-03:** The existing popout utility stays attached to the chair's control surface as a secondary utility, not as a separate route or primary workflow surface.

### Speaker Queue
- **D-04:** The queue becomes a dedicated right-column operational card with the current speaker pinned at the top, queued speakers listed below, and a visible Force Add Speaker action.
- **D-05:** The queue continues to render from the existing room state snapshot; no new queue data model or backend payload is introduced in this phase.

### Live Poll
- **D-06:** The Live Poll area stays in the left-column command surface and preserves the existing poll state machine inside the new glass-panel treatment, including poll creation, active polling, results, and empty states.
- **D-07:** The active poll and live results are the primary visible poll states in the redesigned panel, and the existing End Poll action remains available.

### Share Access
- **D-08:** Share Access becomes a dedicated left-column card that keeps working copy-code and copy-link actions in the redesigned chair view.
- **D-09:** The header keeps only a compact share shortcut; the canonical share actions live in the left-column Share Access card.

### Room Metadata and Settings
- **D-10:** Existing room metadata and configuration remain in the settings/menu area for this phase; Phase 4 does not move or redesign that content beyond fitting it into the new shell.

### Existing DOM and STOMP Contract
- **D-11:** Phase 4 keeps the current chair.html action IDs and STOMP destinations intact; this phase is a layout and panel migration, not a protocol rewrite.

### the agent's Discretion
- Exact internal ordering, spacing, and density of the control cards within the left and right columns.
- Exact poll-result visualization styling inside the new glass-panel treatment, as long as the existing poll states and End Poll action remain intact.
- Exact internal treatment of the popout utility within the control surface, provided it remains secondary and does not change behavior.

</decisions>

<specifics>
## Specific Ideas

- The left column should feel like the chair's command surface, not a generic settings panel.
- The Speaker Queue should read as an operational queue, with the current speaker visibly separated from waiting speakers.
- The live poll card should feel like the mockup's frosted control panel, not a plain form.

</specifics>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Phase scope and acceptance criteria
- `.planning/ROADMAP.md` — Phase 4 goal, dependency on Phase 3, and control-migration success criteria.
- `.planning/REQUIREMENTS.md` — `CTRL-01` through `CTRL-06` and `MIG-01` through `MIG-04`, which define the control panel, queue, poll, share, and migration constraints.
- `.planning/PROJECT.md` — milestone-level constraints, especially no backend changes, no new routes, desktop-only table, and the single-page chair workflow.
- `.planning/STATE.md` — current milestone status, current focus, and the known `MeetingController` constructor-signature blocker in tests.
- `.planning/phases/02-structural-layout-shell/02-CONTEXT.md` — Phase 2 shell decisions for header, sidebar, anchor navigation, and menu placement.
- `.planning/phases/03-conference-table-experience/03-CONTEXT.md` — Phase 3 decisions that keep the center table desktop-only and leave the left and right columns available for controls.

### Design references
- `src/chair-view-redesign/DESIGN.md` — The Orchestrator surface hierarchy, glass-panel treatment, no-line rule, and control hierarchy.
- `src/chair-view-redesign/code.html` — structural mockup for the left-column controls, right-column queue, live poll panel, and share access layout.

### Existing chair baseline
- `src/main/resources/static/chair.html` — current live DOM, IDs, update functions, and STOMP event wiring that Phase 4 must preserve.

### Codebase maps
- `.planning/codebase/STRUCTURE.md` — repository and static frontend layout context.
- `.planning/codebase/CONVENTIONS.md` — frontend and DOM update conventions, including sanitization and state-driven rendering patterns.
- `.planning/codebase/INTEGRATIONS.md` — STOMP destinations and state payload contracts that the migrated controls must continue to use.

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- `src/main/resources/static/chair.html`: existing control IDs, `updateUI(state)`, `updatePollUI(state)`, `updateRoomConfigUI(state)`, share handlers, and STOMP send destinations can be reused rather than rewritten.
- `src/chair-view-redesign/code.html`: the mockup already shows the intended left-column / right-column control layout and the relationship between Meeting Controls, Live Poll, Share Access, and Speaker Queue.
- `share.js`, `metadata-loader.js`, and `purify.min.js`: existing client-side helpers already support sharing, metadata display, and sanitization.
- `src/chair-view-redesign/DESIGN.md`: clarifies the Orchestrator surface hierarchy, no-line rule, and frosted control-panel treatment.

### Established Patterns
- The chair page is still a single static HTML document with inline JavaScript and state-driven rendering from full room snapshots.
- Existing chair actions are wired to fixed STOMP destinations, so the migration is primarily a DOM/layout change, not a protocol change.
- DOMPurify remains the standard path for any user-supplied strings rendered into the DOM.

### Integration Points
- `updateUI(state)` remains the central render path for queue, current speaker, timers, poll UI, and room config.
- The left column continues to host chair operations and share actions, while the right column owns the queue.
- Header shortcuts and the mobile menu from Phase 2 must keep working while the control panels are reorganized.
- The existing STOMP handlers for request, withdraw, next, timer, setLimit, poll start/end/close, and room destroy remain the wiring target for the migrated panels.

</code_context>

<deferred>
## Deferred Ideas

None — discussion stayed within the Phase 4 control-migration boundary.

</deferred>

---

*Phase: 04-functional-control-migration*
*Context gathered: 2026-03-25*
