# Pitfalls Research

**Domain:** Meeting-room chair UI and room-state updates
**Researched:** 2026-03-28
**Confidence:** HIGH

## Critical Pitfalls

### Pitfall 1: Treating room members as client-only state instead of room state

**What goes wrong:**
Speaker avatars disappear on reconnect, duplicate after replacement, or drift out of sync with the actual queue because the member list only exists in the browser or is keyed by mutable display names.

**Why it happens:**
The current app already relies on the room snapshot as the single source of truth, but adding persistent members tempts developers to stash them in the chair DOM or in per-client variables instead of the `Room` state. That breaks as soon as a session reconnects, a speaker changes name, or multiple clients watch the same room.

**How to avoid:**
Store members in server-side room state keyed by WebSocket session ID, derive avatars from the broadcast snapshot, and make disconnect/replacement cleanup part of the state mutation path. Keep name display separate from identity so a rename cannot create a second member record.

**Warning signs:**
Avatars vanish after a state refresh, the same person appears twice in the conference table, or reconnecting with the same name creates a fresh speaker card.

**Phase to address:**
Phase 04-01, where the room model and member lifecycle should be extended before any UI polish depends on it.

---

### Pitfall 2: Breaking the live STOMP contract while adding the new UI affordances

**What goes wrong:**
The chair view stops updating, participant actions silently fail, or room state becomes inconsistent because the implementation introduces new routes, changes destination names, or stops broadcasting the same snapshot shape after each mutation.

**Why it happens:**
The new features look like UI-only changes, so it is easy to add separate events or a new navigation path for the topic label, then forget that the existing browser code and tests depend on the current `/topic/room/{code}/state` flow and the existing publish actions.

**How to avoid:**
Keep the same destinations and message types, add fields to the existing room snapshot instead of branching into new flows, and make the chair topic label jump to the existing menu section rather than to a new page or API. Every mutation should still end in the same broadcast path.

**Warning signs:**
New subscribe/send destinations appear, the chair page needs a refresh to see changes, or a state field is moved instead of extended and older UI branches start failing.

**Phase to address:**
Phase 04-02, because this is where the chair DOM, snapshot rendering, and in-page navigation should be aligned with the current STOMP contract.

---

### Pitfall 3: Computing timer warnings from the wrong clock or the wrong display element

**What goes wrong:**
The large timer changes color too early or too late, the warning turns off after the limit passes, or the remaining-time label starts counting below zero instead of freezing at 00:00.

**Why it happens:**
The chair UI has both an elapsed display and a remaining display, and it is easy to wire warning colors to the wrong one. Another common mistake is applying the red state only when the timer is running, which makes the overrun state disappear as soon as the next broadcast arrives.

**How to avoid:**
Drive warning thresholds from the same elapsed speaker timer used for the large display, clamp remaining time at zero, and keep the red state latched while elapsed time continues past the limit. Test the 25% and 10% boundaries directly instead of eyeballing the color behavior.

**Warning signs:**
The timer flips color back and forth near the threshold, the remaining label shows negative time, or the warning colors stop after pause/resume cycles.

**Phase to address:**
Phase 04-02, because timer presentation and its DOM update loop are part of the chair surface migration.

---

### Pitfall 4: Rendering topic, room title, or participant text as raw HTML

**What goes wrong:**
Room titles wrap badly, topic labels become unreadable, or user-supplied text leaks markup into the chair UI because the implementation switches to `innerHTML` for truncation, click handling, or avatar text.

**Why it happens:**
The new layout needs tighter typography and clickable affordances, so developers often reach for HTML fragments instead of text-safe rendering. That is especially risky here because the app already accepts free-form participant names and room metadata.

**How to avoid:**
Keep user-visible strings on text-safe paths such as `textContent`, keep DOMPurify or equivalent sanitization in the render path where HTML is unavoidable, and use CSS line-clamp plus ellipsis for the two-line title limit. The clickable topic label should be a button or anchor that only changes scroll position, not a block of injected markup.

**Warning signs:**
Markup appears in the title/topic, line-clamp breaks on long room names, or a click target starts accepting HTML content instead of plain text.

**Phase to address:**
Phase 04-02, with verification repeated in Phase 05-01 when the browser pass checks the final migrated chair view.

---

### Pitfall 5: Losing disconnect consistency when member persistence is added

**What goes wrong:**
Members stay visible after they leave, stale avatars remain in the room circle, or reconnecting from the same session leaves ghost entries behind.

**Why it happens:**
The repository already tracks session-to-room mappings, but the new member persistence makes disconnect cleanup more important. If the disconnect path only untracks the room and does not remove the member entry from room state, the snapshot will keep broadcasting dead participants.

**How to avoid:**
Make disconnect handling remove both the session mapping and the corresponding room member record, and test replacement flows where a session reconnects before the old one is fully cleared. Treat disconnect cleanup as part of the invariant, not as an optional best-effort step.

**Warning signs:**
The queue count and avatar count disagree, room state contains entries for sessions no longer present, or a reconnect causes the same person to appear twice.

**Phase to address:**
Phase 04-01, with final browser verification in Phase 05-02 after the state lifecycle changes land.

---

## Technical Debt Patterns

Shortcuts that seem reasonable but create long-term problems.

| Shortcut | Immediate Benefit | Long-term Cost | When Acceptable |
|----------|-------------------|----------------|-----------------|
| Keep avatars in the chair DOM only | Faster UI prototyping | Member state drifts on reconnect and cannot survive refreshes | Never |
| Add a separate STOMP topic for member updates | Smaller UI diff | Duplicate state channels and harder regression testing | Never |
| Compute timer thresholds from the remaining label text | Quick implementation | Thresholds break at pause/resume and after zero | Never |
| Use `innerHTML` for clamped title/topic rendering | Simple layout code | Sanitization regressions and markup injection risk | Never |

## Integration Gotchas

Common mistakes when connecting the new room-member and chair interactions to the existing app.

| Integration | Common Mistake | Correct Approach |
|-------------|----------------|------------------|
| STOMP room snapshot | Adding a new member event instead of extending the existing state snapshot | Keep one broadcast source and extend the `State` payload |
| Chair topic label | Linking to a new route or settings page | Scroll to the existing menu section inside the current chair page |
| Disconnect lifecycle | Only removing the session mapping | Remove both the mapping and the room-member record |
| Timer rendering | Updating only one of the timer labels | Update elapsed, remaining, and warning classes from the same state pass |

## Performance Traps

Patterns that work at small scale but fail as usage grows.

| Trap | Symptoms | Prevention | When It Breaks |
|------|----------|------------|----------------|
| Rebuilding the full chair shell on every broadcast | Janky scrolling, cursor loss in inputs, and focus jumps | Patch only the dynamic nodes that changed | When broadcasts become frequent during active meetings |
| Recomputing avatar layout on every tiny timer tick | Unnecessary layout thrash and CPU churn | Keep avatar rendering tied to room-state changes, not timer refreshes | As the timer interval increases or more participants join |
| Searching members by display name on every update | Duplicate detection gets slower and less reliable | Use session IDs or stable member IDs | When names repeat or rooms get busy |

## Security Mistakes

Domain-specific security issues beyond general web security.

| Mistake | Risk | Prevention |
|---------|------|------------|
| Rendering user names, topic text, or room titles with raw HTML | XSS or markup injection in the chair surface | Keep user text on safe text-rendering paths and sanitize any HTML-bearing branch |
| Exposing session identity in visible member labels | Session correlation and easier impersonation | Keep session IDs server-side and show only display-safe names/avatars |
| Letting the topic label open an arbitrary URL | Phishing or navigation outside the meeting flow | Make the control jump only to a known in-page section |

## UX Pitfalls

Common user experience mistakes in this domain.

| Pitfall | User Impact | Better Approach |
|---------|-------------|-----------------|
| Making the topic label look like plain text only | Users do not discover the shortcut to edit room metadata | Style it as an explicit in-page action with hover and focus states |
| Clamping the room title without testing long names | The title becomes unreadable or overlaps the avatar circle | Use a two-line clamp with ellipsis and verify it in the actual circle layout |
| Turning the timer red only after the limit is exceeded by a full tick | Chairs miss the urgency window | Switch at 25% and 10% remaining, then keep the overrun state visible |
| Letting avatars disappear while the current speaker changes | The room feels unstable and harder to scan | Preserve visible members until disconnect or replacement |

## "Looks Done But Isn't" Checklist

Things that appear complete but are missing critical pieces.

- [ ] **Persistent room members:** Often missing disconnect cleanup or stable session keys - verify the snapshot still shows the right avatars after reconnect.
- [ ] **Timer warning colors:** Often missing the latched overrun state - verify the timer stays red after the limit hits zero.
- [ ] **Room title clamp:** Often missing ellipsis behavior in the real avatar circle - verify long titles still fit and remain legible.
- [ ] **Topic label shortcut:** Often missing keyboard focus and scroll offset handling - verify the control works from both desktop and mobile layouts.

## Recovery Strategies

When pitfalls occur despite prevention, how to recover.

| Pitfall | Recovery Cost | Recovery Steps |
|---------|---------------|----------------|
| Member state drift | HIGH | Move identity back into the room model, clear stale session mappings, and rebroadcast a clean snapshot |
| STOMP contract drift | HIGH | Restore the original destinations and payload shape, then re-run the browser and message-path checks |
| Timer threshold regression | MEDIUM | Recompute warning classes from elapsed time and add boundary tests at 25% and 10% |
| Unsafe text rendering | HIGH | Replace raw HTML paths with sanitized text rendering and re-audit all user-supplied fields |

## Pitfall-to-Phase Mapping

How roadmap phases should address these pitfalls.

| Pitfall | Prevention Phase | Verification |
|---------|------------------|--------------|
| Room members treated as client-only state | Phase 04-01 | Reconnect and replacement tests still show stable avatars in the room snapshot |
| STOMP contract drift | Phase 04-02 | Existing destinations and payloads still drive the live chair view |
| Timer warning regressions | Phase 04-02 | Boundary checks confirm 25% and 10% transitions and the red overrun state |
| Unsafe title/topic rendering | Phase 04-02 | Long titles and topic text render without markup or overflow regressions |
| Disconnect cleanup gaps | Phase 05-02 | Browser smoke tests confirm no ghost members remain after disconnect and reconnect |

## Sources

- `.planning/PROJECT.md`
- `.planning/codebase/CONCERNS.md`
- `.planning/codebase/TESTING.md`
- `src/main/java/de/koderman/domain/Room.java`
- `src/main/java/de/koderman/domain/RoomRepository.java`
- `src/main/java/de/koderman/infrastructure/MeetingController.java`
- `src/main/resources/static/chair.html`

---
*Pitfalls research for: Meeting-room chair UI and room-state updates*
*Researched: 2026-03-28*