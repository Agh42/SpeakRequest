# Roadmap: SpeakRequest - Chair View Redesign

**Milestone 1** | 5 phases | 20 requirements

## Overview

This milestone refactors the existing chair experience into The Orchestrator design system without changing backend behavior. Work is sequenced so the shared visual foundation lands first, the application shell comes next, the conference table centerpiece is built on that shell, functional control panels are migrated after the structure is stable, and the final phase verifies that all existing STOMP-driven behavior remains intact in the redesigned UI.

## Phases

**Phase Numbering:**
- Integer phases (1, 2, 3): Planned milestone work
- Decimal phases (2.1, 2.2): Urgent insertions (marked with INSERTED)

- [ ] **Phase 1: Design System Foundation** - Establish Tailwind, typography, iconography, and The Orchestrator token system in the chair view.
- [ ] **Phase 2: Structural Layout Shell** - Build the new header, sidebar, desktop grid, and mobile navigation that frame the redesigned experience.
- [ ] **Phase 3: Conference Table Experience** - Deliver the round-table centerpiece with participant avatars, queue markers, and the central timer display.
- [ ] **Phase 4: Functional Control Migration** - Rebuild the chair's control, queue, poll, and sharing panels in the new layout while preserving existing interactions.
- [ ] **Phase 5: Integration Verification & Completion** - Reconnect every remaining DOM binding, preserve sanitization and metadata displays, and verify end-to-end migration completeness.

## Phase Details

### Phase 1: Design System Foundation
**Goal**: The chair view adopts The Orchestrator visual system so every later UI element can be built on the same tokens, fonts, icons, and surface hierarchy.
**Depends on**: Nothing (first phase)
**Requirements**: DS-01, DS-02, DS-03, DS-04
**Success Criteria** (what must be TRUE):
  1. The chair page renders with the midnight palette, tonal surface layers, and no explicit 1px section borders.
  2. Tailwind CDN drives the chair page styling and the legacy chair-specific styling dependency is removed or bypassed for the redesigned screen.
  3. Manrope is visible in display and headline treatments, Inter is used for body and labels, and Material Symbols Outlined icons render consistently across the page.
**Plans**: 2 plans
Plans:
- [x] 01-01-PLAN.md - Bootstrap Tailwind, fonts, and Material Symbols in chair view
- [x] 01-02-PLAN.md - Apply Orchestrator tonal foundation and no-line styling baseline
**UI hint**: yes

### Phase 2: Structural Layout Shell
**Goal**: The redesigned page frame works across desktop and mobile, giving the chair stable navigation and spatial structure before functional panels are migrated.
**Depends on**: Phase 1
**Requirements**: LAYOUT-01, LAYOUT-02, LAYOUT-03, LAYOUT-04
**Success Criteria** (what must be TRUE):
  1. On desktop, the chair sees a fixed top header with room badge actions and a collapsible sidebar that can reduce to an icon rail.
  2. The main desktop content is arranged in a three-column layout with left utility panels, a center focus area, and a right queue column.
  3. On mobile, the conference table is de-emphasized and bottom navigation tabs move the user to Controls, Queue, Poll, and Menu sections.
  4. Sidebar and navigation behaviors are usable without changing any backend APIs or introducing new routes.
**Plans**: TBD
**UI hint**: yes

### Phase 3: Conference Table Experience
**Goal**: The chair can read participant presence, speaking state, queue order, and active timing from the new central round-table visualization.
**Depends on**: Phase 2
**Requirements**: TABLE-01, TABLE-02, TABLE-03, TABLE-04, TABLE-05, TABLE-06
**Success Criteria** (what must be TRUE):
  1. On desktop, participants appear around a central round conference table rather than in a plain list-only view.
  2. Each participant is shown as a monogram avatar with a stable, name-derived color that visually distinguishes attendees.
  3. The currently speaking participant is visually called out with the specified tertiary highlight treatment, and queued participants show numbered position badges.
  4. The table center shows the active speaker timer and room code together in a way the chair can read at a glance.
  5. On mobile, the table is hidden and the page remains usable through the non-table sections.
**Plans**: TBD
**UI hint**: yes

### Phase 4: Functional Control Migration
**Goal**: Every chair-facing control area is rebuilt in the new layout so the redesign is operational, not just visual.
**Depends on**: Phase 3
**Requirements**: CTRL-01, CTRL-02, CTRL-03, CTRL-04, CTRL-05, CTRL-06
**Success Criteria** (what must be TRUE):
  1. The chair can add or withdraw speakers, set speaker limits, and trigger timer actions from the redesigned Meeting Controls section.
  2. The Speaker Queue panel shows the current speaker distinctly at the top, lists queued speakers below, and exposes a visible Force Add Speaker action.
  3. The Live Poll panel displays active poll content and live results in the new glass-panel treatment and still offers the poll-ending action.
  4. The Share Access area exposes working copy-code and copy-link actions from the redesigned left column.
**Plans**: TBD
**UI hint**: yes

### Phase 5: Integration Verification & Completion
**Goal**: The redesigned chair view preserves existing behavior end to end, including subscriptions, publishing actions, sanitization, and room metadata visibility.
**Depends on**: Phase 4
**Requirements**: MIG-01, MIG-02, MIG-03, MIG-04
**Success Criteria** (what must be TRUE):
  1. Room state, destroyed-room, and error updates continue to reach the redesigned UI and update the new DOM correctly.
  2. All existing chair publish actions still work from the redesigned controls without introducing new STOMP events or backend changes.
  3. Participant names and other user-supplied strings remain sanitized before rendering anywhere in the redesigned interface.
  4. Existing room configuration metadata remains visible in the redesigned settings or sidebar area.
**Plans**: TBD
**UI hint**: yes

## Progress

**Execution Order:**
Phases execute in numeric order: 1 -> 2 -> 3 -> 4 -> 5

| Phase | Plans Complete | Status | Completed |
|-------|----------------|--------|-----------|
| 1. Design System Foundation | 0/0 | Not started | - |
| 2. Structural Layout Shell | 0/0 | Not started | - |
| 3. Conference Table Experience | 0/0 | Not started | - |
| 4. Functional Control Migration | 0/0 | Not started | - |
| 5. Integration Verification & Completion | 0/0 | Not started | - |
