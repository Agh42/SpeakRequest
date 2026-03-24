---
status: diagnosed
phase: 02-structural-layout-shell
source: [02-01-SUMMARY.md, 02-02-SUMMARY.md]
started: 2026-03-24T21:01:17.0987122+01:00
updated: 2026-03-24T21:18:38.7849325+01:00
---

## Current Test
<!-- OVERWRITE each test - shows where we are -->

[testing complete]

## Tests

### 1. Desktop Header Actions Are Visible and Usable
expected: On desktop, the fixed top header remains visible and shows room info plus share/status/destroy controls.
result: pass

### 2. Desktop Three-Column Shell Renders Correctly
expected: On desktop, the page presents a three-column shell with controls on the left, a center focus area, and queue/poll on the right.
result: pass

### 3. Desktop Sidebar Collapse and Anchor Navigation
expected: The desktop sidebar starts in compact icon-rail mode, can expand/collapse, and clicking nav items scrolls to real in-page sections.
result: pass

### 4. Mobile Bottom Tabs Navigate Sections
expected: On mobile width, bottom tabs for Controls, Queue, Poll, and Menu are visible, and section tabs move to the corresponding in-page section.
result: pass

### 5. Mobile Menu Slide-Over Works
expected: Tapping Menu opens a slide-over panel with room actions/metadata, and closing it returns to the main view without losing state.
result: issue
reported: "fail - tapping menu just dims the screen but no menu appears"
severity: major

### 6. Active Section State Syncs While Scrolling
expected: As you scroll through sections, desktop and mobile navigation active states update to reflect the currently visible section.
result: issue
reported: "fail - highlights do not cleanly change, others stay highlighted. weird effect with both background and a left-side border being highlighted"
severity: major

## Summary

total: 6
passed: 4
issues: 2
pending: 0
skipped: 0
blocked: 0

## Gaps

- truth: "Tapping Menu opens a slide-over panel with room actions/metadata, and closing it returns to the main view without losing state."
  status: failed
  reason: "User reported: fail - tapping menu just dims the screen but no menu appears"
  severity: major
  test: 5
  root_cause: "The mobile slide-over panel is permanently forced off-screen by an inline transform style, and opening the menu only toggles data-open/backdrop without overriding that inline transform."
  artifacts:
    - path: "src/main/resources/static/chair.html"
      issue: "Inline transform on #mobileMenuPanel overrides CSS open-state transform."
  missing:
    - "Remove inline panel transform and let data-open CSS control position."
    - "Or explicitly update panel transform in open/close handlers."
  debug_session: ".planning/debug/phase02-menu-dims-no-panel.md"
- truth: "As you scroll through sections, desktop and mobile navigation active states update to reflect the currently visible section."
  status: failed
  reason: "User reported: fail - highlights do not cleanly change, others stay highlighted. weird effect with both background and a left-side border being highlighted"
  severity: major
  test: 6
  root_cause: "Active section selection is derived from per-callback IntersectionObserver delta entries instead of a deterministic all-sections ranking, causing unstable/sticky highlights in the multi-column layout where multiple sections intersect at once."
  artifacts:
    - path: "src/main/resources/static/chair.html"
      issue: "Observer callback chooses active section from callback entries, not full tracked section state."
  missing:
    - "Compute active section deterministically from all tracked sections each update."
    - "Ensure exactly one active nav target at a time when classes are applied."
  debug_session: ".planning/debug/phase02-nav-active-state.md"
