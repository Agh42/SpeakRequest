# Requirements — v1.2 Improved Voting

*Milestone goal:* Deliver the dot voting feature as a fully verified, labeled, and tested capability across all three views (chair, participant, popout).

---

## Active Requirements

### DOT-VOTING — Core Behavior

- [ ] **DOT-01**: Chair can start a dot voting poll by clicking the "Dot Voting" button, entering a question, adding options, and setting the number of dots per participant.
- [ ] **DOT-02**: Chair's dot voting configuration panel shows a "Dots per participant" label (not "Max votes") that is specific to the dot voting type.
- [ ] **DOT-03**: Participant sees a list of options with an add-dot button (up) and a remove-dot button (down) per option during an active dot voting poll.
- [ ] **DOT-04**: Participant's dot count badge on each option reflects the current distribution in real time as dots are added or removed.
- [ ] **DOT-05**: Participant cannot place more total dots than the configured limit; the add button is blocked and a status message explains the cap.
- [ ] **DOT-06**: Participant cannot remove more dots from an option than they have placed on it; the down button is disabled when count is 0.
- [ ] **DOT-07**: When the chair ends the poll, results are displayed as a ranked option list sorted by dot count, with dot counts and percentages visible.

### DOT-VOTING — Label Accuracy

- [ ] **LABEL-01**: Chair view shows "Dots placed: N" (not "Votes received: N") in the active poll status panel when the poll type is DOT_VOTING.
- [ ] **LABEL-02**: Results view (chair, participant, popout) shows "Total dots placed: N" instead of "Total votes: N" when the poll type is DOT_VOTING.

### DOT-VOTING — Popout View

- [ ] **POP-02**: Popout view shows the dot voting poll question and dot-placement count during an active DOT_VOTING poll.
- [ ] **POP-03**: Popout view shows dot voting results with option labels, dot counts, and percentages after the chair ends the poll.

### DOT-VOTING — Backend Tests

- [ ] **TEST-01**: Backend unit test: a participant placing dots up to the configured limit succeeds; placing one more is rejected.
- [ ] **TEST-02**: Backend unit test: a participant can remove a dot from an option (OPT_N_DOWN) only when they have dots placed on that option.
- [ ] **TEST-03**: Backend unit test: dot counts on the shared `pollResults` map correctly reflect the net of all add/remove operations across participants.
- [ ] **TEST-04**: Backend unit test: ending a dot voting poll produces a `PollResults` object with correct option totals and `pollType = "DOT_VOTING"`.

---

## Future Requirements

*(Deferred — not in scope for v1.2)*

- **POP-01**: Popout view full UI refresh to match updated chair/participant visual language (deferred from v1.1).
- **TIME-04**: Overtime presentation with flashing or audio cues (deferred from v1.1).
- New poll types (ranked choice, open-ended) — out of scope for this milestone.

---

## Out of Scope

- New poll types beyond dot voting — this milestone is limited to verifying and fixing the existing implementation.
- Popout UI redesign — POP-02/POP-03 cover functionality only, not a full visual overhaul.
- New routes or API surfaces — no new STOMP destinations or HTTP endpoints.

---

## Traceability

| Requirement | Phase |
|------------|-------|
| DOT-01, DOT-02, LABEL-01 | Phase 12 |
| DOT-03, DOT-04, DOT-05, DOT-06, LABEL-02 | Phase 12 |
| DOT-07 | Phase 12 |
| POP-02, POP-03 | Phase 13 |
| TEST-01, TEST-02, TEST-03, TEST-04 | Phase 14 |
