# Phase 1 Research: Design System Foundation

## Scope
Phase 1 is a frontend-only migration for chair view visual foundation:
- DS-01: The Orchestrator palette + tonal surfaces + no hard divider lines
- DS-02: Tailwind CDN becomes primary styling path for chair view
- DS-03: Manrope + Inter via Google Fonts
- DS-04: Material Symbols Outlined icon system

## Existing Constraints Confirmed
- No backend changes. Keep all STOMP/WebSocket behavior and event routes unchanged.
- Keep DOMPurify usage intact for all user-supplied strings.
- Work should be confined to chair UI foundation in this phase.

## Codebase Findings
- Current chair page is a monolithic static file at `src/main/resources/static/chair.html`.
- Existing chair styling depends on `styles.css` plus many inline styles and Font Awesome icon classes.
- Existing behavior is ID-driven (`document.getElementById(...)` and event listeners in-page), so structural edits must preserve IDs for controls and dynamic fields.
- Design references are available in:
  - `src/chair-view-redesign/DESIGN.md` (token and visual philosophy)
  - `src/chair-view-redesign/code.html` (Tailwind + Material Symbols reference implementation)

## Implementation Direction
1. Bootstrap head assets first (Tailwind CDN config, Google Fonts, Material Symbols).
2. Establish The Orchestrator token palette in Tailwind config and map body/page shell to tokenized classes.
3. Remove chair page dependency on `styles.css` and migrate to utility classes for phase-1 surfaces/typography/icon baseline.
4. Preserve existing JS hooks and IDs exactly to avoid behavioral regressions in later phases.

## Risks And Mitigations
- Risk: Breaking JS selectors during markup cleanup.
  - Mitigation: Explicitly preserve all existing element IDs and keep script section unchanged in phase 1.
- Risk: Partial migration causes mixed visual system.
  - Mitigation: Ensure body, cards, and major section wrappers all consume Tailwind token classes before phase end.
- Risk: Icon replacement drift from required system.
  - Mitigation: Replace Font Awesome usage in visible controls with Material Symbols Outlined in this phase.

## Verification Approach
- Automated file assertions (PowerShell) to confirm:
  - Tailwind CDN script present
  - Google font links (Manrope + Inter) present
  - Material Symbols link present
  - `styles.css` link removed from chair page
  - Tokenized Tailwind color keys present in config

## Recommendation
Proceed with two sequential plans:
- Plan 01: Asset + token bootstrap
- Plan 02: Visual foundation application across current chair layout
