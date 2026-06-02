# Phase 12: Dot Voting: Chair + Participant Fix & Verify - Discussion Log

> **Audit trail only.** Do not use as input to planning, research, or execution agents.
> Decisions are captured in 12-CONTEXT.md — this log preserves the alternatives considered.

**Date:** 2026-06-02
**Phase:** 12-dot-voting-chair-participant-fix-verify
**Areas discussed:** Results ranking, Label approach (LABEL-01), Limit warning UX (DOT-05), Verification scope

---

## Results Ranking

| Option | Description | Selected |
|--------|-------------|----------|
| Sort descending, no rank labels | Reorder by dot count; no numeric prefix | |
| Sort descending + add rank number prefix | "1. Option A — 5 dots (42%), 2. Option B — …" | ✓ |
| Keep original creation order | Show counts/percentages without sorting | |

**User's choice:** Sort descending + add rank number prefix
**Notes:** Results rendered by a new `renderDotVotingResults()` function (not extending `renderMultiselectResults()`). Applies to both chair.html and participant.html. Popout deferred to phase 13.

---

## Label Approach — LABEL-01

| Option | Description | Selected |
|--------|-------------|----------|
| Dynamic label-swap (extend votingConfigLabel pattern) | Add id to label span, switch text via JS | ✓ |
| Separate show/hide div for dots-placed | Independent HTML element toggled by display | |

**User's choice:** Dynamic label-swap
**Notes:** Label updated in both the poll-type selection handler (already updates `votingConfigLabel`) and in the `updatePollUI()` render path. Reset to "Votes received:" for non-dot-voting types.

---

## Limit Warning UX — DOT-05

| Option | Description | Selected |
|--------|-------------|----------|
| Text message only | Existing status line is sufficient | |
| Text + visually dim all add buttons at limit | Opacity reduction on all main add-dot buttons | ✓ |

**User's choice:** Text + opacity dimming on add buttons
**Notes:** Opacity reduction (e.g., 0.4) on all main add-dot buttons when `totalVotesForUser >= maxVotesPerParticipant`. Down buttons always remain fully visible and enabled.

---

## Verification Scope

| Option | Description | Selected |
|--------|-------------|----------|
| Manual E2E walkthrough only | Walk the dot-poll flow manually; no code tests | ✓ |
| Update existing HTML-based surface tests | Add assertions to ChairSurfaceRequirementsTest | |

**User's choice:** Manual E2E — single person with two browser tabs (chair + participant)
**Notes:** Backend unit tests are scoped to phase 14. No automated frontend tests in this phase.

---

## the agent's Discretion

- Exact rank prefix format ("1." vs "#1" vs "1 —")
- Specific opacity value for dimmed add buttons (0.35–0.45 range)
- Minor styling of rank number inside `renderDotVotingResults()` rows

## Deferred Ideas

- Popout dot-voting display → Phase 13
- Backend unit tests for castVote DOT_VOTING → Phase 14
- Automated frontend surface tests for dot-voting labels → Phase 14 or follow-up
