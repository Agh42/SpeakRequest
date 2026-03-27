# Feature Research

**Domain:** meeting moderation / room-based discussion management UI
**Researched:** 2026-03-28
**Confidence:** MEDIUM

## Feature Landscape

### Table Stakes (Users Expect These)

Features users assume exist. Missing these makes the chair surface feel unstable or incomplete.

| Feature | Why Expected | Complexity | Notes |
|---------|--------------|------------|-------|
| Persistent room members tied to session identity | Chairs need stable room presence, not avatars that disappear whenever the queue changes or the UI rerenders | MEDIUM | Typically this means the server keeps a member registry in room state and removes entries on disconnect or replacement, so the client always renders the latest authoritative snapshot |
| Stable participant avatars inside the room circle | Avatars are the visual anchor for who is in the room, who is speaking, and who is waiting | MEDIUM | Works best when avatars are derived from room state rather than ephemeral client-only queue rows; persistence is what keeps them from dropping out |
| Readable room title presentation inside constrained space | The room title must remain legible without breaking the conference-table layout | LOW | Typical implementation is a two-line clamp with ellipsis overflow, not a free-form expansion that pushes other controls around |
| Large speaking timer urgency colors | Chairs need immediate visual feedback when a speaker is near the limit or over time | LOW | Standard pattern is a neutral state, then warning at an earlier threshold, then danger at a final threshold; once elapsed time passes zero, the timer remains red while the remaining display stays at 00:00 |
| Topic editing affordance in the chair surface | Room metadata is only useful if the chair can get back to it quickly | LOW | The current milestone’s click-to-jump behavior is the lightweight version of this: navigation to the existing menu section, not a new editor flow |

### Differentiators (Competitive Advantage)

Features that set the product apart. Not required, but valuable.

| Feature | Value Proposition | Complexity | Notes |
|---------|-------------------|------------|-------|
| Session-keyed member persistence that survives queue churn | Makes the room feel coherent during active moderation, especially when speakers are being promoted, removed, or reordered | MEDIUM | Many basic meeting tools only show transient queue entries; keeping a separate room-members view gives the chair a stronger sense of who is actually present |
| Strong visual hierarchy around the active speaker and chair controls | Helps the chair stay oriented without hunting across the screen | LOW | This is more about the overall orchestration of the surface than a single widget; the value comes from reducing attention switching |
| Clickable topic label that jumps directly to the edit section | Shortens the path from noticing stale metadata to fixing it | LOW | This is a small but meaningful workflow improvement because it turns a static label into a navigation shortcut |

### Anti-Features (Commonly Requested, Often Problematic)

Features that seem good but create problems.

| Feature | Why Requested | Why Problematic | Alternative |
|---------|---------------|-----------------|-------------|
| Separate participant roster route or a second room model | It sounds cleaner to split roster management away from the existing room surface | It duplicates the room state contract, increases synchronization risk, and undermines the current STOMP snapshot model | Keep room members inside the existing room state and render them from the same broadcast snapshot |
| Free-form room title expansion instead of clamping | People want the full title visible at all times | It breaks the conference-table layout and pushes the most important controls out of view | Use a two-line clamp with ellipsis and preserve layout stability |
| Letting the speaker timer count visibly past zero on the remaining display | It feels more precise to show exact overtime | It makes the remaining-time signal ambiguous; the chair needs urgency, not extra numeric noise | Keep elapsed time moving, keep the remaining label fixed at 00:00, and use color to signal overtime |
| Adding a brand-new metadata editor panel for the topic jump | It seems more powerful than a link | It adds another editing path and fragments the chair workflow | Reuse the existing menu section and jump there from the topic label |

## Feature Dependencies

```text
[Persistent room members keyed by session]
    └──requires──> [Room state snapshot carries member presence authoritatively]
                       └──requires──> [Existing STOMP room-state broadcast]

[Stable participant avatars inside the room circle] ──requires──> [Persistent room members keyed by session]

[Readable room title presentation inside constrained space] ──depends on──> [Conference table/avatar shell layout]

[Large speaking timer urgency colors] ──depends on──> [Current speaker timer state]

[Clickable topic label that jumps to the edit section] ──depends on──> [Existing room menu/metadata section anchor]
```

### Dependency Notes

- **Persistent room members keyed by session requires room state snapshot carries member presence authoritatively:** the client cannot keep avatars stable on its own if the server snapshot omits room membership.
- **Stable participant avatars inside the room circle requires persistent room members keyed by session:** avatars need a durable identity source so they do not disappear when queue membership changes.
- **Large speaking timer urgency colors depends on current speaker timer state:** the warning thresholds only work if the chair view already has accurate elapsed and limit values.
- **Clickable topic label that jumps to the edit section depends on the existing room menu anchor:** this feature should reuse the current menu section instead of introducing another editing surface.

## MVP Definition

### Launch With (v1)

Minimum viable product - what is needed to validate the UI improvement direction.

- [x] Persistent room members tied to session identity - the chair needs stable presence data before avatar rendering can be trusted
- [x] Stable participant avatars inside the room circle - the main visual payoff of the persistence work
- [x] Large speaking timer urgency colors - immediate usability gain with low implementation risk
- [x] Readable room title presentation inside constrained space - prevents the new chair layout from becoming unreadable

### Add After Validation (v1.x)

Features to add once the core surfaces are working.

- [x] Clickable topic label that jumps directly to the edit section - useful workflow polish after the layout and state changes are stable

### Future Consideration (v2+)

Features to defer until product-market fit is established.

- [ ] Separate participant roster management surface - defer because it duplicates the existing room-state contract and adds synchronization risk
- [ ] More aggressive overtime presentation such as flashing or sounds - defer because the current milestone only needs visual urgency cues

## Feature Prioritization Matrix

| Feature | User Value | Implementation Cost | Priority |
|---------|------------|---------------------|----------|
| Persistent room members keyed by session | HIGH | MEDIUM | P1 |
| Stable participant avatars inside the room circle | HIGH | MEDIUM | P1 |
| Large speaking timer urgency colors | HIGH | LOW | P1 |
| Readable room title presentation inside constrained space | HIGH | LOW | P1 |
| Clickable topic label that jumps to the edit section | MEDIUM | LOW | P2 |

**Priority key:**
- P1: Must have for launch
- P2: Should have, add when possible
- P3: Nice to have, future consideration

## Competitor Feature Analysis

| Feature | Competitor A | Competitor B | Our Approach |
|---------|--------------|--------------|--------------|
| Persistent member presence | Many basic meeting tools only reflect transient speaker rows | More advanced facilitation tools keep a persistent participant roster | Keep the room snapshot authoritative and session-keyed so the chair surface stays stable during queue churn |
| Timer urgency signaling | Often limited to a single color change near the limit | Some products add flashing or audio alerts | Use strong visual thresholds only, with red persistence after overrun and no noisy overcounting on remaining time |
| Metadata editing entry point | Usually buried in a settings drawer or modal | Sometimes exposed as a separate editor panel | Reuse the existing menu section and make the topic label a shortcut into it |

## Sources

- SpeakRequest project context in [.planning/PROJECT.md](../PROJECT.md)
- Current chair surface implementation in [src/main/resources/static/chair.html](../../src/main/resources/static/chair.html)
- Current room snapshot contract in [src/main/java/de/koderman/domain/State.java](../../src/main/java/de/koderman/domain/State.java)
- Current room model in [src/main/java/de/koderman/domain/Room.java](../../src/main/java/de/koderman/domain/Room.java)

---
*Feature research for: meeting moderation / room-based discussion management UI*
*Researched: 2026-03-28*