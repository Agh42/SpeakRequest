# Phase 4: Functional Control Migration - Discussion Log (Batch Assumptions)

> **Audit trail only.** Do not use as input to planning, research, or execution agents.
> Decisions are captured in CONTEXT.md — this log preserves the analysis.

**Date:** 2026-03-25
**Phase:** 04-functional-control-migration
**Mode:** assumptions
**Areas analyzed:** Meeting Controls and Popout, Speaker Queue, Live Poll Placement and Behavior, Share Access and Header Shortcut, Existing DOM and STOMP Contract

---

## Meeting Controls and Popout

| Option | Description | Selected |
|--------|-------------|----------|
| Secondary utility in the control surface | Popout stays attached to the main chair controls as a secondary utility. | ✓ |
| Separate primary card | Popout gets its own major surface or workflow. | |

**User's choice:** Secondary utility in the control surface.
**Notes:** Preserves popout as a supporting action rather than splitting the chair's operational surface.

## Speaker Queue

| Option | Description | Selected |
|--------|-------------|----------|
| Dedicated right-column operational card | Current speaker pinned at top, queue below, Force Add Speaker visible. | ✓ |
| Passive list only | Keep the queue as a basic list without operational emphasis. | |

**User's choice:** Dedicated right-column operational card.
**Notes:** Matches the chair-command layout and keeps queue order obvious.

## Live Poll Placement and Behavior

| Option | Description | Selected |
|--------|-------------|----------|
| Left-column command surface | Poll panel lives with other chair actions and keeps existing states/results behavior. | ✓ |
| Separate utility panel | Poll remains visually detached from the main control stack. | |

**User's choice:** Left-column command surface.
**Notes:** Keeps the frosted poll panel part of the chair's operational stack.

## Share Access and Header Shortcut

| Option | Description | Selected |
|--------|-------------|----------|
| Canonical left-column card + compact header shortcut | Share actions live in the main panel; header remains a shortcut. | ✓ |
| Header-only share actions | Share code/link actions move into the header exclusively. | |

**User's choice:** Canonical left-column card + compact header shortcut.
**Notes:** Keeps the main share actions discoverable without overloading the header.

## Existing DOM and STOMP Contract

| Option | Description | Selected |
|--------|-------------|----------|
| Preserve IDs and destinations | Reorganize the DOM, keep current event hooks and topics. | ✓ |
| Rename as part of the migration | Treat the migration as an opportunity to redesign the protocol contract. | |

**User's choice:** Preserve IDs and destinations.
**Notes:** Prevents Phase 4 from becoming a backend or protocol rewrite.

## the agent's Discretion

- Exact internal ordering, spacing, and density of the control cards within the left and right columns.
- Exact poll-result visualization styling inside the new glass-panel treatment, as long as the existing poll states and End Poll action remain intact.
- Exact internal treatment of the popout utility within the control surface, provided it remains secondary and does not change behavior.

## Deferred Ideas

None — discussion stayed within phase scope.