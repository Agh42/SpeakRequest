# Requirements: SpeakRequest — UI Improvements

**Defined:** 2026-03-28
**Core Value:** The chair can see every participant's status at a glance - who is speaking, who is next, and who is waiting - without leaving the main screen.

## v1 Requirements

Requirements for the UI Improvements milestone. Each maps to roadmap phases.

### Room Presence

- [ ] **PRES-01**: When a client requests to speak for the first time, the server stores that participant's name against the client's session ID in room state.
- [ ] **PRES-02**: If the same client session requests to speak again with a different name, the server replaces the stored name for that session instead of creating a duplicate member.
- [ ] **PRES-03**: When a client session disconnects, the server removes that session's member entry from room state.
- [ ] **PRES-04**: Speaker avatars remain visible in the room circle from room state until the session disconnects or is replaced.

### Display

- [ ] **VIEW-01**: The room title inside the avatar circle is limited to two lines.
- [ ] **VIEW-02**: Long room titles are truncated with an ellipsis so they fit inside the circle without overflowing adjacent content.
- [ ] **VIEW-03**: The avatar circle rendering keeps participant names readable even when the room title is long.

### Timer

- [ ] **TIME-01**: The large speaking timer turns yellow when 25% of the allotted time remains.
- [ ] **TIME-02**: The large speaking timer turns red when 10% of the allotted time remains.
- [ ] **TIME-03**: When the allotted time is reached, the remaining-time display stays at 00:00 while the elapsed timer continues increasing and the large timer remains red.

### Metadata Navigation

- [ ] **META-01**: The chair can click the topic label to jump to the existing room menu section for editing.

## v2 Requirements

Deferred to future release.

### Participant View Redesign

- **PART-01**: Participant view (participant.html) redesigned to match the updated chair surface.
- **PART-02**: Participant view mirrors the room-member presence model in a read-only layout.

### Popout View

- **POP-01**: Popout view (popout.html) refreshed to match the updated UI language.

### Timer Enhancements

- **TIME-04**: Add stronger overtime presentation such as flashing or audio cues.

## Out of Scope

Explicitly excluded from this milestone.

| Feature | Reason |
|---------|--------|
| Separate participant roster route or second room model | Duplicates the existing room-state contract and increases sync risk |
| Popout view redesign | Kept stable for this milestone; only timer thresholds are in scope if needed |
| New backend routes or API surfaces | The milestone extends the existing Room state and current STOMP contract only |
| Database or Redis persistence for members | The feature is session-scoped presence, not durable storage |
| Profile pictures or external avatar images | No backend support for images; initials and name-based avatars are sufficient |
| More aggressive overtime effects like flashing or sounds | Deferred to a later milestone; this milestone only needs color thresholds |

## Traceability

Updated during roadmap creation.

| Requirement | Phase | Status |
|-------------|-------|--------|
| PRES-01 | Pending | Pending |
| PRES-02 | Pending | Pending |
| PRES-03 | Pending | Pending |
| PRES-04 | Pending | Pending |
| VIEW-01 | Pending | Pending |
| VIEW-02 | Pending | Pending |
| VIEW-03 | Pending | Pending |
| TIME-01 | Pending | Pending |
| TIME-02 | Pending | Pending |
| TIME-03 | Pending | Pending |
| META-01 | Pending | Pending |

**Coverage:**
- v1 requirements: 11 total
- Mapped to phases: 0
- Unmapped: 11 ⚠

---
*Requirements defined: 2026-03-28*
*Last updated: 2026-03-28 after v1.1 milestone start*
