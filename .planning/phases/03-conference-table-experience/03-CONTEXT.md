# Phase 3: Conference Table Experience - Context

**Gathered:** 2026-03-25
**Status:** Ready for planning

<domain>
## Phase Boundary

Deliver the desktop conference-table centerpiece for the chair view: a round-table visualization that shows participant presence, speaking state, queue order, and active timing. The table stays desktop-only; mobile continues to use the phase-2 shell navigation and hides the table.

</domain>

<decisions>
## Implementation Decisions

### Table placement
- **D-01:** Phase 3 replaces the center column with the round-table centerpiece on desktop rather than keeping the old center-shell stack visible alongside it.
- **D-02:** The conference table remains hidden on mobile, with the existing shell/navigation continuing to carry the experience there.

### Participant identity
- **D-03:** Participants are rendered as monogram avatars with stable assigned colors rather than photos, generic icons, or neutral initials.

### Status encoding
- **D-04:** The current speaker gets the tertiary highlight treatment, and queued participants show explicit numbered position badges so the table reads as an operational queue.

### Center timing hierarchy
- **D-05:** The table center prioritizes the current speaker countdown and room topic as the primary read.
- **D-06:** The center no longer uses room code as the focal label.
- **D-07:** Current speaker initials, total meeting time, and remaining speaker time stay visible in smaller secondary text inside the centerpiece.

### the agent's Discretion
- Exact avatar color algorithm and styling details, as long as colors remain stable per participant and fit The Orchestrator palette.
- Exact orbit geometry, spacing, and count handling for the participant ring.
- Exact typography and microcopy for the secondary timer text.

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Phase scope and acceptance criteria
- `.planning/ROADMAP.md` — Phase 3 goal, dependencies, and success criteria for the conference-table experience.
- `.planning/REQUIREMENTS.md` — `TABLE-01` through `TABLE-06`, which define the round table, monogram avatars, speaking highlight, queue badges, center timing, and mobile hiding behavior.
- `.planning/PROJECT.md` — milestone-level constraints, especially the chair-view redesign scope and the desktop-only table direction.
- `.planning/STATE.md` — current milestone status and the note that Phase 3 is the active focus.
- `.planning/phases/02-structural-layout-shell/02-CONTEXT.md` — Phase 2 decisions that keep the shell single-page, anchor-based, and ready to host the table in the center column.

### Design references
- `src/chair-view-redesign/DESIGN.md` — The Orchestrator visual rules, including no-line structure, tonal layering, glass panels, and status treatment.
- `src/chair-view-redesign/code.html` — the current mockup reference for the round table, orbiting participants, and center timer composition.

### Existing chair baseline and codebase maps
- `src/main/resources/static/chair.html` — live chair DOM, timer bindings, queue rendering, and current state update flow that Phase 3 must extend.
- `.planning/codebase/STRUCTURE.md` — repository and static frontend layout context.
- `.planning/codebase/CONVENTIONS.md` — frontend and DOM update conventions, including sanitization and state-driven rendering patterns.
- `.planning/codebase/INTEGRATIONS.md` — STOMP and state payload contracts that the table rendering must consume without changing backend behavior.

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- `src/main/resources/static/chair.html`: already exposes `roomCodeDisplay`, `roomCodeDisplay2`, `curName`, `speakTimer`, `remaining`, `meetingTimer`, and the queue container, so the table can be built on top of existing state instead of inventing new payloads.
- `src/chair-view-redesign/code.html`: provides the intended visual composition for the round table, center timer stack, and participant orbit.
- `src/chair-view-redesign/DESIGN.md`: provides the exact palette, tonal layering, and no-line status language for the centerpiece.

### Established Patterns
- The chair view is still a single static HTML document with inline JavaScript and STOMP-driven `updateUI(state)` rendering.
- Existing participant and queue data are already available in the full room state snapshot, so the table should be a presentation layer over current state rather than a new data model.
- DOMPurify remains the standard sanitization path for any participant names or user-supplied strings inserted into the DOM.

### Integration Points
- The table center should consume the existing room state fields for current speaker, queue, timing, and room config without changing backend APIs or STOMP topics.
- The phase must integrate with the phase-2 shell layout while preserving mobile navigation, section anchors, and the desktop-only table rule.
- Queue rendering remains tied to the current state broadcast, so the orbit and queue badges need to derive from the same state source of truth.

</code_context>

<specifics>
## Specific Ideas

- The user wants the center focal point to be the current speaker countdown plus room topic, not the room code.
- The user wants current speaker initials, total meeting time, and remaining speaker time to remain visible in smaller text inside the centerpiece.
- The participant ring should feel like a real table of seats, not a decorative halo.

</specifics>

<deferred>
## Deferred Ideas

None — discussion stayed within the Phase 3 conference-table boundary.

</deferred>

---

*Phase: 03-conference-table-experience*
*Context gathered: 2026-03-25*