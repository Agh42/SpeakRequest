# Roadmap: SpeakRequest v1.1 UI Improvements

## Overview

This milestone extends the shipped chair surface without changing the existing STOMP contract or static-page architecture. The work is organized around five delivery boundaries: session-keyed room presence, chair-surface readability, timer urgency and topic navigation, avatar label truncation, then final integration verification. Phase numbering continues from the previous milestone, so v1.1 starts at Phase 6.

## Milestone Constraints

- Room members must live in Room state keyed by WebSocket session ID.
- The existing STOMP destinations and static HTML pages stay in place.
- Topic label navigation must jump to the existing room menu section, not a new route.
- Timer warning colors apply only to the large timer; the remaining-time display stays at 00:00 after expiry.

## Phases

- [x] **Phase 6: Session-Keyed Room Presence** - Persist speaker membership by session and keep avatars stable until disconnect or replacement.
- [x] **Phase 7: Chair Surface Readability** - Keep the room title and presence rendering readable inside the avatar circle.
- [x] **Phase 8: Timer Urgency and Topic Editing** - Add timer warning colors and make the topic label jump to the room menu section.
- [x] **Phase 9: Integration Verification and Closeout** - Verify the combined chair experience and close the milestone.
- [x] **Phase 10: Avatar name truncation and chair label simplification** - Keep avatar names inside the circle by truncating long labels and shorten the chair label to a plain `chair` value.

## Phase Details

### Phase 6: Session-Keyed Room Presence
**Goal**: The room state keeps participant presence tied to WebSocket session IDs so the chair surface can show stable avatars until a session disconnects or is replaced.
**Depends on**: Nothing (first phase)
**Requirements**: PRES-01, PRES-02, PRES-03, PRES-04
**Success Criteria** (what must be TRUE):
	1. When a participant requests to speak for the first time, the server stores that participant name against the client's session ID in room state.
	2. If the same session requests to speak again with a different name, the stored member entry is replaced rather than duplicated.
	3. When a client session disconnects, that session's member entry is removed from room state.
	4. Speaker avatars remain visible in the room circle from room state until the session disconnects or is replaced.
**Plans**: 2 plans
- [x] 06-01-PLAN.md — Add session-keyed room-member state, wire lifecycle cleanup, and keep the chair avatars snapshot-driven
- [x] 06-02-PLAN.md — Close the chair-proxy presence gap and tighten avatar label rendering
**UI hint**: yes

### Phase 7: Chair Surface Readability
**Goal**: The avatar circle and room title stay readable together, even when the room name is long or presence is crowded.
**Depends on**: Phase 6
**Requirements**: VIEW-01, VIEW-02, VIEW-03
**Success Criteria** (what must be TRUE):
	1. The room title inside the avatar circle is limited to two lines.
	2. Long room titles truncate with an ellipsis instead of overflowing the circle.
	3. Participant names remain readable even when the room title is long and the avatar circle stays visually balanced.
**Plans**: TBD
**UI hint**: yes

### Phase 8: Timer Urgency and Topic Editing
**Goal**: The chair can read time pressure immediately and jump from the topic label to the existing room menu edit section.
**Depends on**: Phase 7
**Requirements**: TIME-01, TIME-02, TIME-03, META-01
**Success Criteria** (what must be TRUE):
	1. The large speaking timer turns yellow when 25% of the allotted time remains.
	2. The large speaking timer turns red when 10% of the allotted time remains.
	3. When the allotted time is reached, the remaining-time display stays at 00:00 while the elapsed timer continues increasing and the large timer remains red.
	4. The chair can click the topic label to jump to the existing room menu section for editing.
**Plans**: TBD
**UI hint**: yes

### Phase 9: Integration Verification and Closeout
**Goal**: The chair surface changes work together cleanly, with the current STOMP and page architecture preserved end to end.
**Depends on**: Phase 8
**Requirements**: None new
**Success Criteria** (what must be TRUE):
	1. The presence lifecycle, room title handling, timer thresholds, and topic-label jump all work together on the chair surface.
	2. The existing STOMP destinations and static pages remain intact after the UI changes.
	3. The milestone can be closed with no additional v1 requirements left unmapped.
**Plans**: TBD
**UI hint**: yes

### Phase 10: Avatar name truncation and chair label simplification
**Goal**: The avatar circle keeps long names inside its bounds by truncating labels to a short prefix plus ellipsis, and the chair uses a plain `chair` label instead of the current `Chair-C...` text.
**Depends on**: Phase 9
**Requirements**: TBD
**Success Criteria** (what must be TRUE):
	1. Participant names in the avatar circle truncate to a short prefix plus `...` when they would otherwise overflow.
	2. The chair label displays as `chair` instead of the current longer form.
	3. The avatar circle remains visually balanced after the name truncation change.
**Plans**: 1 plan

Plans:
- [x] 10-01-PLAN.md — Truncate avatar labels to a three-character prefix plus ellipsis and replace the chair bootstrap label with plain `chair`
**UI hint**: yes

## Progress

| Phase | Plans Complete | Status | Completed |
|-------|----------------|--------|-----------|
| 6. Session-Keyed Room Presence | 2/2 | Complete | 2026-04-01 |
| 7. Chair Surface Readability | 1/1 | Complete | 2026-04-01 |
| 8. Timer Urgency and Topic Editing | 1/1 | Complete | 2026-04-01 |
| 9. Integration Verification and Closeout | 1/1 | Complete | 2026-04-01 |
| 10. Avatar name truncation and chair label simplification | 1/1 | Complete | 2026-04-08 |

## Traceability

| Requirement | Phase | Status |
|-------------|-------|--------|
| PRES-01 | Phase 6 | Complete |
| PRES-02 | Phase 6 | Complete |
| PRES-03 | Phase 6 | Complete |
| PRES-04 | Phase 6 | Complete |
| VIEW-01 | Phase 7 | Complete |
| VIEW-02 | Phase 7 | Complete |
| VIEW-03 | Phase 7 | Complete |
| TIME-01 | Phase 8 | Complete |
| TIME-02 | Phase 8 | Complete |
| TIME-03 | Phase 8 | Complete |
| META-01 | Phase 8 | Complete |

---
*Roadmap created: 2026-03-28*
