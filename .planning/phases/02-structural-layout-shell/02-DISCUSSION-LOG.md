# Phase 2: Structural Layout Shell - Discussion Log

**Date:** 2026-03-24
**Mode:** discuss
**Outcome:** Context ready for planning

## Selected Gray Areas

- Header action priority
- Sidebar behavior
- Desktop column behavior
- Mobile navigation model

## Discussion Record

### Header action priority

**Q1:** On desktop, what should stay permanently visible in the fixed header?
- Option: Room badge with copy/share actions, compact status, settings, and desktop-only destroy action
- Option: Only brand + room badge; move status and destructive actions into the sidebar/menu
- Option: Minimal brand/header only; keep almost all controls in the sidebar or sections
- **Selected:** Room badge with copy/share actions, compact status, settings, and desktop-only destroy action

**Q2:** Where should connection state and chair-ownership status live?
- Option: Compact pills in the header near the room badge
- Option: Inside the sidebar or menu only
- Option: Only as transient toasts when state changes
- **Selected:** Compact pills in the header near the room badge

**Q3:** On mobile, how much of the desktop header action set should remain visible?
- Option: Keep a compact room badge/share affordance in the header; move destroy/settings into Menu
- Option: Mirror most desktop actions in the mobile header
- Option: Keep the mobile header almost brand-only and rely on bottom nav/menu
- **Selected:** Keep a compact room badge/share affordance in the header; move destroy/settings into Menu

**Q4:** What should we do with the mockup's extra utility affordance beyond required settings, like notifications?
- Option: Omit non-functional utilities for now and use that visual space for real status information
- Option: Show a disabled/decorative notifications icon to preserve the mockup silhouette
- Option: Include a live notifications-style area as a generic activity bucket
- **Selected:** Omit non-functional utilities for now and use that visual space for real status information

**Continuation check:** Move to next area

### Sidebar behavior

**Q1:** How should desktop sidebar navigation behave in Phase 2?
- Option: Anchor-scroll navigation within one page, with active-state highlighting
- Option: View-switching shell that shows one panel group at a time
- Option: Hybrid: anchor-scroll for existing sections now, architected so it can evolve into stronger shell state later
- **Selected:** Anchor-scroll navigation within one page, with active-state highlighting

**Q2:** What should the desktop sidebar default to on first load?
- Option: Expanded sidebar on first load, with user-triggered collapse to icon rail
- Option: Collapsed icon rail by default
- Option: Remember the last sidebar state per browser
- **Selected:** Collapsed icon rail by default

**Q3:** How should the sidebar behave on mobile when opened from hamburger/Menu?
- Option: Slide-over panel from the left using the same nav structure
- Option: Full-screen menu sheet
- Option: No sidebar on mobile; bottom nav is the only navigation
- **Selected:** Slide-over panel from the left using the same nav structure

**Q4:** What should the required Agenda nav item do in this phase, given agenda management is out of scope?
- Option: Show it as a placeholder nav item that targets a static 'Agenda coming later' section/menu slot
- Option: Hide Agenda entirely until a later phase
- Option: Reuse Agenda as a generic notes/settings destination
- **Selected:** Hide Agenda entirely until a later phase

**Continuation check:** Move to next area

### Desktop column behavior

**Q1:** What should occupy the center focus column in Phase 2, before the conference table phase lands?
- Option: A shell-focused placeholder/foundation area that establishes spacing and hierarchy, not the full table yet
- Option: A live hybrid center panel with current timer/room info carried forward as interim content
- Option: Leave the center mostly empty beyond spacing scaffolding
- **Selected:** A shell-focused placeholder/foundation area that establishes spacing and hierarchy, not the full table yet

**Q2:** How should desktop scrolling and stickiness work?
- Option: Only the top header is fixed; the three columns scroll naturally with the page
- Option: Header fixed plus sticky side columns
- Option: Header, left utilities, and right queue all stay pinned while only center content scrolls
- **Selected:** Only the top header is fixed; the three columns scroll naturally with the page

**Q3:** How much of the current live functionality should remain visible inside the new desktop shell during Phase 2?
- Option: Keep the live sections visible, but reposition them into the new columns without trying to finish their final visual treatments yet
- Option: Use mostly placeholders for left/right panels until later phases
- Option: Fully restyle and finish the left/right functional panels now
- **Selected:** Keep the live sections visible, but reposition them into the new columns without trying to finish their final visual treatments yet

**Q4:** Where should existing room metadata/config information live in the shell during this phase?
- Option: In the left utility column as a provisional settings-style card or section
- Option: Inside the sidebar/menu structure only
- Option: De-emphasize it and let it return in a later migration phase
- **Selected:** In the left utility column as a provisional settings-style card or section

**Continuation check:** Move to next area

### Mobile navigation model

**Q1:** How should the bottom tabs behave on mobile?
- Option: Scroll to anchored sections on the same page and highlight the active section
- Option: Act like true tabs that show one panel at a time
- Option: Hybrid: tabs jump to sections, but some sections collapse/expand to reduce scroll length
- **Selected:** Scroll to anchored sections on the same page and highlight the active section

**Q2:** When the conference table is hidden on mobile, what should the top of the mobile experience emphasize first?
- Option: Meeting Controls first
- Option: A compact room summary/status block before any section content
- Option: Speaker Queue first
- **Selected:** Meeting Controls first

**Q3:** What should the Menu tab open or target?
- Option: A slide-over menu containing room actions, settings, metadata, and any secondary items
- Option: A normal in-page Menu section farther down the document
- Option: The same sidebar overlay used by the hamburger button, with the Menu tab acting as another opener
- **Selected:** A slide-over menu containing room actions, settings, metadata, and any secondary items

**Q4:** How should mobile tab highlighting update?
- Option: Automatically track the active section as the user scrolls
- Option: Only highlight the last tab the user tapped
- Option: Keep tab styling mostly static and avoid active-state logic
- **Selected:** Automatically track the active section as the user scrolls

**Continuation check:** Move to next area

## Final Confirmation

- Summary presented across all four areas
- Final choice: I'm ready for context

---

*Phase: 02-structural-layout-shell*
*Discussion logged: 2026-03-24*