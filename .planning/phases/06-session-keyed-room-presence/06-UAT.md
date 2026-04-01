---
status: complete
phase: 06-session-keyed-room-presence
source: [06-01-SUMMARY.md]
started: 2026-03-30T14:05:48.9627140Z
updated: 2026-04-01T00:00:00.0000000Z
---

## Current Test
<!-- OVERWRITE each test - shows where we are -->

[testing complete]

## Tests

### 1. Same chair session can create multiple on-behalf members
expected: When the chair uses request-to-speak on behalf of a participant, the room adds a new member entry for that participant rather than overwriting the chair's own avatar. Multiple on-behalf participants tied to the same chair session stay visible as separate avatars until withdrawn.
result: pass
reported: "resolved by appending chair-proxy members and removing them on withdraw"
severity: major

### 2. Same session updates the existing member instead of duplicating it
expected: From the same participant session, request to speak again with a different display name. The chair surface updates the existing avatar/member entry to the new name and does not create a second avatar for the same session.
result: pass

### 3. Disconnect removes the member entry from presence
expected: With a participant currently present, close or disconnect that participant session. The chair surface removes that participant from the room members list and the avatar disappears on the next state update.
result: pass

### 4. Chair avatars stay driven by room snapshot state across refresh
expected: With participants present, refresh the chair page. The currently connected participants remain visible from the room snapshot state after reload, without requiring a new speak request.
result: pass

## Summary

total: 4
passed: 4
issues: 0
pending: 0
skipped: 0

## Gaps

- truth: "Chair can create multiple on-behalf participant avatars from the same session"
  status: passed
  reason: "Chair proxy requests now append visible members and unique queue entries, and withdraw removes the named proxy presence before broadcast."
  severity: major
  test: 1
  root_cause: "Resolved by branching chair-session requests away from the normal session-replacement path and by updating the chair label helper."
  artifacts: []
  missing: []
  debug_session: ""
