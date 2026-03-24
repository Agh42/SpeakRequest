---
status: diagnosed
trigger: "You are diagnosing one UAT gap for phase 02-structural-layout-shell: nav active states do not cleanly change while scrolling (Test 6). Goal: find_root_cause_only."
created: 2026-03-24T21:16:42.7838681+01:00
updated: 2026-03-24T21:22:05.0000000+01:00
---

## Current Focus

hypothesis: IntersectionObserver picks active section from partial callback entries in a multi-column layout, so nav state transitions appear sticky/incoherent while scrolling.
test: Validate that active state is derived only from current callback entries (not full visible section set) and that sections are arranged in parallel columns rather than linear flow.
expecting: Observer logic can select non-intuitive section and produce lingering/highlight artifacts from user perspective.
next_action: return structured root-cause diagnosis (no code changes)

## Symptoms

expected: As you scroll through sections, desktop and mobile navigation active states update to reflect the currently visible section.
actual: highlights do not cleanly change; others stay highlighted; both background and left-side border effects can remain highlighted.
errors: none reported, behavioral UI bug from UAT Test 6.
reproduction: Run UAT Test 6 for phase 02 and scroll through sections on desktop/mobile layouts.
started: discovered during UAT.

## Eliminated

## Evidence

- timestamp: 2026-03-24T21:17:40.0000000+01:00
	checked: .planning/phases/02-structural-layout-shell/02-UAT.md
	found: Test 6 is recorded as failed with stale/multiple highlight behavior during scroll-based section changes.
	implication: Bug scope is specifically active-state synchronization, not missing navigation elements.

- timestamp: 2026-03-24T21:17:55.0000000+01:00
	checked: src/main/resources/static/chair.html (top and layout/nav section)
	found: Desktop nav links start with one nav-active and others nav-inactive; nav-active style applies both gradient background and inset left border.
	implication: If multiple links keep nav-active, user-observed dual highlighting is expected.

- timestamp: 2026-03-24T21:20:10.0000000+01:00
	checked: src/main/resources/static/chair.html (navigation script)
	found: updateActiveNavigation toggles nav-active/nav-inactive for all [data-nav-link] targets from a single activeSectionId, while IntersectionObserver chooses activeSectionId from entries in the current callback only (sorted by intersectionRatio).
	implication: During smooth scroll and multi-section visibility, active selection can lag or jump based on callback timing rather than true current section dominance.

- timestamp: 2026-03-24T21:20:25.0000000+01:00
	checked: src/main/resources/static/chair.html (layout structure)
	found: tracked sections are distributed across multiple columns (left controls/menu, right queue/poll, center shell separate), not a single vertical reading order.
	implication: Scroll position does not map cleanly to one canonical section; observer-driven active state becomes ambiguous and appears inconsistent to users.

- timestamp: 2026-03-24T21:21:35.0000000+01:00
	checked: build/resources/main/static/chair.html (served artifact copy)
	found: Built output contains identical updateActiveNavigation + IntersectionObserver logic and thresholds as source.
	implication: UAT behavior is reproducible from active build artifact, not caused by source/build mismatch.

## Resolution

root_cause: Active section detection uses IntersectionObserver callback entries only (delta-based updates) combined with a multi-column shell where multiple tracked sections are visible at the same time, so the chosen active section is timing/ratio-dependent rather than a stable single scroll position mapping; this creates sticky/jumpy highlight transitions that appear as lingering active states.
fix: Replace delta-entry winner logic with deterministic section scoring across all tracked sections on scroll/resize (or use a linear sentinel model), then set one canonical active section and clear all others each update.
verification: Diagnosis only mode (find_root_cause_only). No fix applied.
files_changed: []
