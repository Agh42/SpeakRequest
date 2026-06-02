# Roadmap: SpeakRequest

## Archived Milestones

- **[v1.0 - Chair View Redesign](.planning/milestones/v1.0-ROADMAP.md)** - shipped 2026-03-25 - Phases 1-5
- **[v1.1 - UI Improvements](.planning/milestones/v1.1-ROADMAP.md)** - shipped 2026-04-22 - Phases 6-11 - session presence, timer urgency, topic nav, avatar truncation, participant restyle

---

## v1.2 — Improved Voting (Phases 12–14)

**Milestone goal:** Deliver the existing dot voting feature as a fully verified, labeled, and tested capability across chair, participant, and popout views.

---

### Phase 12 — Dot Voting: Chair + Participant Fix & Verify

**Goal:** Verify and fix the dot voting flow across the chair and participant views — correct labels, edge-case behavior, and results rendering.

**Requirements:**
- DOT-01: Chair can start a dot voting poll (button → question → options → dots per participant)
- DOT-02: Chair config shows "Dots per participant" label
- DOT-03: Participant sees option list with up/down arrow buttons
- DOT-04: Dot count badge updates in real time
- DOT-05: Adding dots blocked once total equals configured limit
- DOT-06: Down button disabled when option dot count is 0
- DOT-07: Results shown as ranked option list with dot counts and percentages
- LABEL-01: Chair active-poll panel shows "Dots placed: N"
- LABEL-02: Results view shows "Total dots placed"

**Success criteria:**
1. Chair can create a dot voting poll and see the "Dots per participant" label without any "Max votes" text.
2. Participant can add dots up to the limit; adding beyond the limit shows a warning and does not increment.
3. Participant can remove a dot from any option using the down arrow; the down arrow is disabled when count is zero.
4. When the chair ends the poll, results are shown sorted by dot count with option labels and dot percentages.
5. "Total dots placed" (not "Total votes") appears in the results footer in both chair and participant views.

---

### Phase 13 — Dot Voting: Popout View

**Goal:** Ensure the popout view correctly reflects dot voting state during an active poll and after the poll ends.

**Requirements:**
- POP-02: Popout shows dot voting question + dot-placement count during active poll
- POP-03: Popout shows dot voting results (option list with dot counts) after chair ends poll

**Success criteria:**
1. When a DOT_VOTING poll is active, the popout shows the poll question and a running "Dots placed" count (not "Votes Received").
2. When the chair ends the poll, the popout transitions to showing the results — option labels sorted by dot count with counts and percentages.
3. When the chair closes the poll, the popout returns to the normal speaker/queue display.

---

### Phase 14 — Dot Voting: Backend Unit Tests

**Goal:** Cover the dot voting logic in Room.java with backend unit tests to prevent regressions.

**Requirements:**
- TEST-01: Unit test: per-participant dot limit is enforced
- TEST-02: Unit test: OPT_N_DOWN only removes when dots > 0 on that option
- TEST-03: Unit test: shared pollResults reflect the net of all add/remove operations
- TEST-04: Unit test: endPoll produces a correct PollResults for DOT_VOTING

**Success criteria:**
1. Test suite has at least 4 new tests covering the DOT_VOTING branch of `castVote()`.
2. All new tests pass with `./gradlew test`.
3. A test verifies that a participant cannot exceed `votesPerParticipant` dots.
4. A test verifies that the `_DOWN` suffix path decrements both the session tally and the shared result map correctly.

---

## Phase Summary

| # | Phase | Goal | Requirements | Success Criteria |
|---|-------|------|-------------|------------------|
| 12 | Dot Voting: Chair + Participant | Fix & verify labels, edge cases, results display | DOT-01–07, LABEL-01–02 | 5 |
| 13 | Dot Voting: Popout View | Correct active + ended state display in popout | POP-02, POP-03 | 3 |
| 14 | Dot Voting: Backend Tests | Unit test coverage for dot voting logic | TEST-01–04 | 4 |

**Total: 3 phases | 13 requirements mapped | All covered ✓**
