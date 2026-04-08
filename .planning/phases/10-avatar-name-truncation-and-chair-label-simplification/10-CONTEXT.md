# Phase 10: Avatar Name Truncation and Chair Label Simplification - Context

**Gathered:** 2026-04-08
**Status:** Ready for planning

<domain>
## Phase Boundary

Keep the chair avatar label short enough to fit inside the avatar circle by truncating long names to a short prefix plus an ellipsis, and replace the chair bootstrap name with a plain `chair` label. Preserve the existing snapshot-driven chair rendering, queue/current-speaker display, and STOMP/static-page architecture.

</domain>

<decisions>
## Implementation Decisions

### Avatar label truncation
- **D-01:** `getParticipantAvatarLabel(name)` in `chair.html` is the single helper that controls the text rendered inside the conference-table avatar circles.
- **D-02:** Long names should truncate to the first three visible characters plus `...` so a name like `annemarie` renders as `ann...`.
- **D-03:** Short names should remain unchanged.

### Chair bootstrap label
- **D-04:** The chair bootstrap join/assume-chair path should send the plain participant name `chair` instead of the current `Chair-Candidate` label.
- **D-05:** The change should apply to both the initial join payload and the follow-up assume-chair payload so the chair identity is consistent.

### Rendering and safety
- **D-06:** Keep DOMPurify sanitization in place for the avatar label and the rest of the chair surface.
- **D-07:** Do not change the room snapshot shape, queue logic, timer behavior, or the existing STOMP destinations.

</decisions>

<specifics>
## Specific Ideas

- `annemarie` should show as `ann...` inside the avatar circle.
- The chair bootstrap name should be plain `chair` everywhere the page currently hardcodes `Chair-Candidate`.
- The avatar circle should still use the same accent/ink palette and current seat rendering logic.

</specifics>

<canonical_refs>
## Canonical References

### Phase scope and roadmap
- [ROADMAP.md](../../ROADMAP.md) — Phase 10 goal, dependency, and phase summary.
- [STATE.md](../../STATE.md) — roadmap evolution note and milestone status.
- [PROJECT.md](../../PROJECT.md) — milestone-level constraints and architecture expectations.

### Existing chair rendering code
- [src/main/resources/static/chair.html](../../../src/main/resources/static/chair.html) — avatar label helper, avatar rendering, and chair bootstrap payloads.

### Existing tests
- [src/test/java/de/koderman/ChairSurfaceRequirementsTest.java](../../../src/test/java/de/koderman/ChairSurfaceRequirementsTest.java) — HTML-based chair surface assertions.
- [src/test/java/de/koderman/MeetingControllerPresenceTest.java](../../../src/test/java/de/koderman/MeetingControllerPresenceTest.java) — keeps the chair/session presence behavior stable while the UI changes land.

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- `getParticipantAvatarLabel(name)` already isolates the avatar label text logic in one place.
- The chair bootstrap code already contains both hardcoded chair-name payloads, so one small edit can keep them consistent.
- Existing chair surface tests already inspect `chair.html` as text, which is the cheapest way to pin down the label behavior.

### Established Patterns
- Frontend surface checks in this repo often validate static HTML source directly rather than spinning up a browser for every assertion.
- The chair page keeps sanitization and snapshot rendering in the same file, so label changes should stay local to the helper and bootstrap payloads.

### Integration Points
- `chair.html` avatar label helper: update truncation rule.
- `chair.html` chair bootstrap: replace `Chair-Candidate` with `chair` in both payloads.
- `ChairSurfaceRequirementsTest`: extend coverage so the new label rules are locked in.

</code_context>

<deferred>
## Deferred Ideas

None — this phase stays within chair-surface label cleanup.

</deferred>

---

*Phase: 10-avatar-name-truncation-and-chair-label-simplification*
*Context gathered: 2026-04-08*
