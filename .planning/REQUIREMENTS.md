# Requirements: SpeakRequest — Chair View Redesign

**Defined:** 2026-03-24
**Core Value:** The chair can see every participant's status at a glance — who is speaking, who is next, and who is waiting — without leaving the main screen.

## v1 Requirements

Requirements for the Chair View redesign milestone. Each maps to roadmap phases.

### Design System

- [ ] **DS-01**: Chair view uses "The Orchestrator" color palette (midnight base `#0b1326`, tonal surface layers, zero explicit 1px borders)
- [x] **DS-02**: Chair view uses Tailwind CSS (CDN) as the style framework, replacing `styles.css` for the chair page
- [x] **DS-03**: Manrope font (headlines/display) and Inter font (body/labels) loaded via Google Fonts in chair.html
- [x] **DS-04**: Material Symbols Outlined icon font used throughout chair view, replacing any previous icon approach

### Layout

- [ ] **LAYOUT-01**: Fixed top header bar: hamburger sidebar toggle, room-code badge with copy-code and copy-link buttons, destroy-room button (desktop only), settings icon
- [ ] **LAYOUT-02**: Collapsible left sidebar navigation with sections: Meeting Controls, Speaker Queue, Live Polling, Agenda — collapses to 72px icon rail on desktop, slides off-screen on mobile
- [ ] **LAYOUT-03**: Desktop layout: 3-column 12-grid — left (4 cols: controls + poll + share), center (5 cols: conference table), right (3 cols: speaker queue)
- [ ] **LAYOUT-04**: Mobile bottom navigation bar: Controls / Queue / Poll / Menu tabs; sections scroll into view on tap

### Conference Table

- [ ] **TABLE-01**: Central round conference table in center column — top-down abstract view, round shape, participants arranged around the perimeter
- [ ] **TABLE-02**: Participant avatar displayed as a colored circle with the participant's initials (monogram), color derived from name for visual distinction
- [ ] **TABLE-03**: "Speaking now" avatar state: highlighted glowing border in tertiary color (`#00e475`) around the speaking participant's avatar
- [ ] **TABLE-04**: "In queue" avatar state: numbered badge (showing queue position 1, 2, 3…) displayed on the avatar
- [ ] **TABLE-05**: Center of the table ring shows current speaker timer countdown and room code
- [ ] **TABLE-06**: Conference table view is hidden on mobile; mobile layout focuses on controls + queue + poll

### Controls

- [ ] **CTRL-01**: Meeting Controls section: speaker-name input field, proxy-request button (add by chair), withdraw button, all wired to existing STOMP events
- [ ] **CTRL-02**: Per-speaker time limit input (minutes, numeric), wired to existing `SetLimit` STOMP event
- [ ] **CTRL-03**: Timer action buttons: Start, Pause, Next Speaker, Reset — arranged in 2×2 grid, wired to existing `TimerCtrl` STOMP events
- [ ] **CTRL-04**: Speaker Queue panel (right column): scrollable list, current speaker card pinned at top with tertiary accent, queued speakers listed below, "Force Add Speaker" button at bottom
- [ ] **CTRL-05**: Live Poll glass-panel (left column): displays active poll question, shows live vote-bar results, "End Poll & Record" button — wired to existing poll STOMP events
- [ ] **CTRL-06**: Share Access section (left column): copy-code button showing room code, copy-link button — wired to existing sharing logic

### Migration

- [ ] **MIG-01**: All existing STOMP subscription handlers (room state, destroyed, error) preserved and wired to updated DOM elements
- [ ] **MIG-02**: All existing STOMP publish actions (join as chair, timer controls, speak requests, poll actions, room config) preserved
- [ ] **MIG-03**: DOMPurify sanitization applied to all participant names and user-supplied strings rendered into the DOM
- [ ] **MIG-04**: Existing room config display (meeting goal, participation format, decision rule, deliverable) retained in settings/sidebar area

## v2 Requirements

Deferred to future release.

### Participant View Redesign
- **PART-01**: Participant view (participant.html) redesigned to match The Orchestrator design language
- **PART-02**: Participant view shows a read-only version of the conference table

### Animations & Transitions
- **ANIM-01**: Smooth avatar state transitions (idle → speaking → queued)
- **ANIM-02**: Queue reorder animations

### Popout View
- **POP-01**: Popout view redesigned to match The Orchestrator design language

## Out of Scope

Explicitly excluded from this milestone.

| Feature | Reason |
|---------|--------|
| Participant view (participant.html) redesign | Deferred — delivered in next milestone using chair view as reference |
| Popout view (popout.html) changes | Kept as-is; separate concern |
| New backend / API features | UI-only refactor; no new STOMP events or REST endpoints |
| CSS animations and transitions | Keep UI static for now; add in a dedicated animation phase |
| Profile pictures / external avatar images | No profile image support in backend; initials only |
| Agenda management feature | The sidebar Agenda nav item is a placeholder; no backend support yet |
| Auth / login system | Chair identity = WebSocket session; no auth layer added |

## Traceability

Updated during roadmap creation.

| Requirement | Phase | Status |
|-------------|-------|--------|
| DS-01 | Phase 1 | Pending |
| DS-02 | Phase 1 | Complete |
| DS-03 | Phase 1 | Complete |
| DS-04 | Phase 1 | Complete |
| LAYOUT-01 | Phase 2 | Pending |
| LAYOUT-02 | Phase 2 | Pending |
| LAYOUT-03 | Phase 2 | Pending |
| LAYOUT-04 | Phase 2 | Pending |
| TABLE-01 | Phase 3 | Pending |
| TABLE-02 | Phase 3 | Pending |
| TABLE-03 | Phase 3 | Pending |
| TABLE-04 | Phase 3 | Pending |
| TABLE-05 | Phase 3 | Pending |
| TABLE-06 | Phase 3 | Pending |
| CTRL-01 | Phase 4 | Pending |
| CTRL-02 | Phase 4 | Pending |
| CTRL-03 | Phase 4 | Pending |
| CTRL-04 | Phase 4 | Pending |
| CTRL-05 | Phase 4 | Pending |
| CTRL-06 | Phase 4 | Pending |
| MIG-01 | Phase 5 | Pending |
| MIG-02 | Phase 5 | Pending |
| MIG-03 | Phase 5 | Pending |
| MIG-04 | Phase 5 | Pending |

**Coverage:**
- v1 requirements: 20 total
- Mapped to phases: 20
- Unmapped: 0 ✓

---
*Requirements defined: 2026-03-24*
*Last updated: 2026-03-24 after initial scoping*
