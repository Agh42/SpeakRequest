# Phase 2: Structural Layout Shell - Context

**Gathered:** 2026-03-24
**Status:** Ready for planning

<domain>
## Phase Boundary

Build the redesigned chair-view shell that frames the experience across desktop and mobile: fixed header, collapsible sidebar, three-column desktop layout, and bottom-tab mobile navigation. This phase reorganizes the existing live chair UI into that shell without adding backend behavior, routes, or Phase 3 conference-table functionality.

</domain>

<decisions>
## Implementation Decisions

### Header action priority
- **D-01:** On desktop, the fixed header keeps the room badge with copy/share actions, compact connection and chair-status indicators, a settings entry point, and a desktop-only destroy-room action.
- **D-02:** On mobile, the header keeps a compact room/share affordance visible, while destroy-room and settings move into the Menu experience.
- **D-03:** Non-functional mockup-only utilities such as notifications are omitted for now; that space should be used for real status information.

### Sidebar behavior
- **D-04:** Desktop sidebar navigation uses anchor-scroll behavior within a single page, with active-state highlighting rather than view switching or new routes.
- **D-05:** The desktop shell starts with the sidebar collapsed to an icon rail by default.
- **D-06:** On mobile, sidebar-style navigation opens as a left slide-over panel from the hamburger/Menu entry points.
- **D-07:** The Agenda nav item stays hidden until it represents a real scoped feature; Phase 2 should not ship a placeholder agenda destination.

### Desktop column behavior
- **D-08:** The center desktop column is a shell/foundation area in Phase 2, establishing spacing and hierarchy without delivering the Phase 3 conference-table experience yet.
- **D-09:** Only the top header is fixed; left, center, and right columns scroll naturally with the page in this phase.
- **D-10:** Existing live chair sections remain usable and are repositioned into the new three-column shell instead of being replaced by placeholders.
- **D-11:** Existing room metadata/configuration stays visible in the left utility column as a provisional settings-style card or section.

### Mobile navigation model
- **D-12:** Bottom navigation tabs scroll to anchored in-page sections and highlight the active section as the user scrolls.
- **D-13:** The mobile experience emphasizes Meeting Controls first once the conference table is hidden.
- **D-14:** The Menu tab opens a slide-over menu that contains room actions, settings, metadata, and other secondary items.
- **D-15:** Mobile tab highlighting updates automatically based on the current visible section, not just the last tapped tab.

### the agent's Discretion
- Exact active-state styling for header pills, sidebar items, and bottom tabs, as long as it stays within The Orchestrator visual language.
- Exact anchor offsets, scroll behavior implementation details, and icon sizing.
- Exact arrangement and microcopy of secondary menu content, provided it respects the decisions above and does not introduce new capabilities.

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Phase scope and acceptance criteria
- `.planning/ROADMAP.md` — Phase 2 goal, dependency on Phase 1, and shell-specific success criteria.
- `.planning/REQUIREMENTS.md` — `LAYOUT-01` through `LAYOUT-04`, which define the fixed header, sidebar behavior, desktop 12-grid shell, and mobile bottom navigation.
- `.planning/PROJECT.md` — redesign-level constraints, especially no backend changes, no new routes, conference table staying desktop-only, and Agenda remaining out of scope as a real feature.
- `.planning/STATE.md` — confirms Phase 2 is the active focus and carries forward the requirement to preserve existing chair behavior and sanitization.

### Design references
- `src/chair-view-redesign/DESIGN.md` — The Orchestrator visual system rules, including the no-line rule, tonal layering, and shell-level component guidance.
- `src/chair-view-redesign/code.html` — structural mockup for the fixed top bar, collapsible sidebar, desktop grid, and mobile bottom navigation shell.

### Existing chair baseline
- `src/main/resources/static/chair.html` — current live chair DOM, IDs, and section structure that the Phase 2 shell must reorganize without breaking behavior.

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- `src/chair-view-redesign/code.html`: reusable shell patterns for the header, collapsible sidebar, 12-column desktop grid, and mobile bottom nav.
- `src/main/resources/static/chair.html`: existing live sections, controls, and IDs can be moved into the new layout instead of rebuilt from scratch.
- Tailwind token setup already in `src/main/resources/static/chair.html`: Phase 1 established the palette and typography baseline that Phase 2 should keep using.

### Established Patterns
- Chair view remains a single static HTML document with inline JavaScript and extensive `document.getElementById(...)` / `querySelector(...)` wiring.
- Existing room controls, poll UI, sharing actions, and metadata are already functional in-page; shell work should preserve those bindings while changing layout.
- The redesign milestone is UI-only: no backend changes, no new STOMP events, and no new routes.

### Integration Points
- Header shell must continue to expose room code, share/copy actions, connection state, and chair occupancy in a form compatible with the existing DOM update flow.
- Desktop shell needs anchorable sections for controls, queue, poll, and menu/settings content so sidebar and mobile bottom-nav can target real sections in one document.
- Left-column settings/menu content needs to absorb existing room metadata and secondary room actions without removing current functionality.

</code_context>

<specifics>
## Specific Ideas

- Desktop should feel denser from the start: sidebar defaults to collapsed icon rail.
- Agenda should not appear until it has real scoped behavior; no fake placeholder just to match the mockup.
- The center column should visibly reserve space and hierarchy for the future conference table without prematurely shipping that feature.

</specifics>

<deferred>
## Deferred Ideas

None — discussion stayed within the Phase 2 shell boundary.

</deferred>

---

*Phase: 02-structural-layout-shell*
*Context gathered: 2026-03-24*