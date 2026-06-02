# Phase 12: Dot Voting: Chair + Participant Fix & Verify - Context

**Gathered:** 2026-06-02
**Status:** Ready for planning

<domain>
## Phase Boundary

Correct the label text, edge-case behavior, and results rendering for dot voting across chair.html and participant.html. No new poll types, no new routes, no architectural changes. The backend logic (castVote, _DOWN suffix, per-session limit) is already correct — this phase is frontend-only fixes against requirements DOT-01–07 and LABEL-01–02.

</domain>

<decisions>
## Implementation Decisions

### Results display (DOT-07)
- **D-01:** DOT_VOTING results are rendered by a new dedicated `renderDotVotingResults()` function, separate from `renderMultiselectResults()`. It sorts options by dot count descending and prefixes each row with a rank number (1., 2., 3., …).
- **D-02:** Both chair.html and participant.html get their own copy of `renderDotVotingResults()`. Popout is out of scope for this phase (phase 13).
- **D-03:** All existing call sites that currently route `DOT_VOTING` results through `renderMultiselectResults()` are updated to call `renderDotVotingResults()` instead.

### Label approach — LABEL-01 (chair active-poll panel)
- **D-04:** Add an `id` to the existing "Votes received:" label span (e.g., `id="pollVoteCountLabel"`) so it can be addressed by JS.
- **D-05:** In the poll-type selection handler (where `votingConfigLabel` already changes to "Dots per participant:"), also change `pollVoteCountLabel` text to "Dots placed:".
- **D-06:** In the `updatePollUI()` render path, whenever an ACTIVE DOT_VOTING poll is shown, also ensure `pollVoteCountLabel` reads "Dots placed:"; reset it to "Votes received:" for all other poll types.
- **D-07:** This is a dynamic label-swap pattern consistent with the existing `votingConfigLabel` approach — no new HTML structure needed.

### Label text fixes — LABEL-02 ("Total dots placed")
- **D-08:** Everywhere a results footer says `Total votes: ${totalVotes}`, add a poll-type branch: for `DOT_VOTING` output `Total dots placed: ${totalVotes}`; for all other types keep `Total votes: ${totalVotes}`.
- **D-09:** This applies to: the chair ENDED results block, the participant overlay ENDED results block, and the participant CLOSED/lastResults block — all three locations.

### Limit warning UX — DOT-05
- **D-10:** When `totalVotesForUser >= maxVotesPerParticipant`, the existing status-text warning ("Maximum N dot(s) placed. Remove a dot first.") stays. In addition, all main add-dot buttons are visually dimmed via opacity (e.g., `opacity: 0.4; cursor: not-allowed`).
- **D-11:** Down (remove) buttons always stay at full opacity and enabled — removal is always allowed.
- **D-12:** The opacity is applied via the existing `updateVoteButtonStyles()` or equivalent update call whenever the dot total changes.

### Verification method
- **D-13:** Manual E2E walkthrough driven by a single person using two browser tabs (one chair, one participant) against the running server.
- **D-14:** No automated frontend tests are added in this phase; backend unit tests are phase 14.

### the agent's Discretion
- Exact rank prefix formatting (e.g., "1." vs "#1" vs "1 —") as long as it is a visible numeric rank.
- Specific opacity value for the dimmed add buttons (somewhere in 0.35–0.45 range).
- Minor spacing/styling adjustments to the rank number in `renderDotVotingResults()` to keep it consistent with the existing bar-chart result row style.

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Phase scope and requirements
- `.planning/ROADMAP.md` — Phase 12 goal, requirements DOT-01–07 and LABEL-01–02, success criteria.
- `.planning/PROJECT.md` — Milestone goal (v1.2) and explicit out-of-scope notes.
- `.planning/REQUIREMENTS.md` — Full requirements list and compatibility constraints.

### Primary implementation files
- `src/main/resources/static/chair.html` — Contains the poll-type selection handler (where `votingConfigLabel` is already switched), `updatePollUI()`, `renderMultiselectResults()`, the active-poll panel HTML (with the hardcoded "Votes received:" label), and the ENDED results rendering block.
- `src/main/resources/static/participant.html` — Contains `renderDotVotingOptions()`, `updateDotVotingStatus()`, `renderMultiselectResults()`, the ENDED overlay results block, and the CLOSED/lastResults block. Also contains the existing `dotVotes` map, `maxVotesPerParticipant`, and `updateVoteButtonStyles()`.

### Backend (read-only reference — no changes needed)
- `src/main/java/de/kodermann/domain/Room.java` — `castVote()` implementation confirming DOT_VOTING add/down logic and `votesPerParticipant` enforcement. Verified correct; no backend changes in this phase.

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- `votingConfigLabel` label-swap pattern in chair.html: already changes the config label for DOT_VOTING; `pollVoteCountLabel` should follow the identical pattern.
- `renderMultiselectResults()` in both files: existing bar-chart HTML structure to reference when building `renderDotVotingResults()` (same bar style, add rank prefix and sort step).
- `updateVoteButtonStyles()` in participant.html: the existing style-update function to extend for the add-button opacity dimming.
- `updateDotVotingStatus()` in participant.html: already tracks `totalDots` and `remaining` — good place to trigger add-button opacity update.

### Established Patterns
- Dynamic label-swap via `element.textContent = '…'` (used for votingConfigLabel) — same approach for pollVoteCountLabel.
- Poll-type branch in results rendering: `if (poll.pollType === 'DOT_VOTING') { … } else { … }` pattern is already used throughout both files for routing to type-specific renderers.
- DOMPurify sanitization is in place for all user-supplied strings (option labels, question text) — preserve it in `renderDotVotingResults()`.

### Integration Points
- `updatePollUI()` in chair.html: the render path that must check `poll.pollType === 'DOT_VOTING'` for the label-swap on every state update.
- ENDED/CLOSED state blocks in participant.html: three separate code paths all currently output "Total votes:" footer — all three need the poll-type branch for "Total dots placed:".
- `renderDotVotingOptions()` onclick handler in participant.html: after updating `dotVotes` and calling `updateDotVotingStatus()`, also call the add-button opacity update.

</code_context>

<specifics>
## Specific Ideas

- The rank prefix in `renderDotVotingResults()` must be a visible numeric rank per the requirement wording "ranked option list."
- "Total dots placed" (not "Total dots" or "Total votes") is the exact label string required by LABEL-02.
- "Dots placed:" (not "Dots received:" or "Dots cast:") is the exact label for LABEL-01.

</specifics>

<deferred>
## Deferred Ideas

- Popout dot-voting display (active + ended state) → Phase 13.
- Backend unit tests for castVote DOT_VOTING branch → Phase 14.
- Automated frontend/HTML surface tests for dot-voting labels → can be added in phase 14 or as a follow-up after phase 13.

</deferred>

---

*Phase: 12-dot-voting-chair-participant-fix-verify*
*Context gathered: 2026-06-02*
