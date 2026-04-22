# Phase 11: Participant View Style Sync - Context

**Gathered:** 2026-04-22
**Status:** Ready for planning

<domain>
## Phase Boundary

The participant view adopts the chair-view visual language for shared presentation surfaces: the circular avatar table display, card and panel surfaces, typography, and button treatments. Participant-only behavior stays intact, including request/withdraw actions and the existing poll box. Chair-only controls such as start poll remain chair-only.

</domain>

<decisions>
## Implementation Decisions

### Table treatment
- **D-01:** Use the full circular avatar-table layout on the participant page, not just avatar chips inside the existing queue list.
- **D-02:** Keep the participant data and interactions participant-specific, even though the table presentation matches the chair view.

### Shared surfaces
- **D-03:** Match the chair view across all shared participant surfaces, including the request card, room info, queue area, poll box, and shared utility panels.
- **D-04:** Keep the participant poll box as the participant poll box; it should get the same style language as the updated chair view, but not adopt chair-only controls.

### Button language
- **D-05:** Use the same premium rounded chair-view button treatment for participant actions such as request, withdraw, share, and copy actions.
- **D-06:** Do not retain older participant-specific button shapes when a shared chair-style treatment is available.

### Behavior boundary
- **D-07:** Copy presentation patterns only.
- **D-08:** Do not add chair-only functionality to the participant view, including start poll, destroy room, or chair menu actions.

### the agent's Discretion
- Exact spacing scale inside the participant cards and table layout.
- Small responsive adjustments for the circular table on narrower screens, so long as the participant behavior and chair-only boundary stay intact.

</decisions>

<specifics>
## Specific Ideas

- The user explicitly wanted the participant view to pick up the chair view's recent layout changes: circular avatar table display, general box styles, fonts, and button styles.
- The user explicitly called out that only style should be copied, not chair functions.
- The user explicitly said the participant view should keep the poll-box, but give it the same style as the new chair view.

</specifics>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Roadmap and project context
- `.planning/ROADMAP.md` — Phase 11 boundary, goal, depends-on chain, and the participant-view style sync scope.
- `.planning/PROJECT.md` — Milestone goal, current requirements, and the explicit out-of-scope note for participant-view redesign in the milestone context.
- `.planning/REQUIREMENTS.md` — Current product requirements and compatibility constraints that still apply to the shared surfaces.
- `.planning/STATE.md` — Current milestone state and roadmap evolution note for Phase 11.

### Chair and participant implementations
- `src/main/resources/static/chair.html` — Source of the updated chair-view visual language, avatar-table composition, card surfaces, and button styling.
- `src/main/resources/static/participant.html` — Existing participant behavior and current poll-box / queue / action wiring that must stay participant-specific.

### Chair redesign references
- `src/chair-view-redesign/DESIGN.md` — Design rules for the Orchestrator visual system, including tonal layering, typography, and button treatment.
- `src/chair-view-redesign/code.html` — Reference implementation for the chair-view styling patterns and component treatment.

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- `conference-table-shell`, `conference-table-seat`, and `conference-table-avatar` from `src/main/resources/static/chair.html` provide the exact circular table presentation pattern to mirror.
- `glass-panel`, tonal surface tokens, and the Manrope/Inter font setup are already established in the chair page and can be reused as the participant styling language.
- The participant page already has stable request/withdraw, queue rendering, and poll-box behavior that should remain functionally intact.

### Established Patterns
- The chair page already uses tone-based glass cards, rounded utility buttons, and circular avatar seats with hashed accent colors.
- The participant page currently keeps behavior in vanilla JS with DOMPurify sanitization and direct event wiring; the phase should preserve that interaction model.
- The current codebase separates presentation from behavior through static HTML and inline JS, so participant styling changes can be made without introducing new backend routes.

### Integration Points
- `participant.html` queue rendering is the main participant-side entry point for the new avatar-table presentation.
- The participant poll section is the surface that should be restyled to match the chair view while preserving the participant polling flow.
- Request/withdraw/share buttons are the participant controls that should be visually updated without inheriting chair-only actions.

</code_context>

<deferred>
## Deferred Ideas

None — the discussion stayed within the participant-view style sync boundary.

</deferred>

---

*Phase: 11-participant-view-style-sync*
*Context gathered: 2026-04-22*
