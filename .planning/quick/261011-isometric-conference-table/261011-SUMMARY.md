# Isometric conference table — execution summary

Completed on 2026-10-11 in branch `feat/isometric-conference-table`.

Both chair and participant pages now share a circular table rendered in isometric projection. People use colored torso artwork, abbreviated labels below their icons, and full names on hover and keyboard focus. A green arrow identifies the current speaker and returns to the reserved chair seat when idle. The table occupies a full row so crowded seating has room.

The room maintains a stable roster of 20 seats, including the chair, and a speaking-turn sequence for every member who has spoken. An incoming overflow speaker replaces a member who has never spoken, then the person who spoke longest ago. Everyone else keeps their slot. A `+ N more` label counts present members outside the visible roster. Chair transfers, repeated turns, membership changes, duplicate names, and chair-created proxy participants use stable identities. Both views receive the same roster, including after a participant refresh.

Seats use equally spaced angles projected into the table plane. Layout sizing reserves space for icons, labels, and arrows. Narrow screens scroll within the table; the current floor holder is brought into view when the speaker or viewport changes. An empty chair seat is retained when the role is vacant and is excluded from the count of people.

## Implementation

- `ConferenceTable.java` owns seat retention, speaking recency, speaker matching, and overflow counts under the existing room lock.
- `RoomMember` adds a stable member ID and a proxy flag. Existing fields and the three-argument constructor remain available.
- `State` adds the chair session ID and conference-table projection. Existing STOMP routes are unchanged.
- `conference-table.js` and `conference-table.css` provide one renderer for both pages, including fixed SVG artwork and names inserted through safe text/attribute APIs.
- `RoomConferenceTableTest` covers behavioral seating requirements. Existing page wiring tests were updated for shared assets and labels outside the icons.

## Verification

- Full Gradle suite: **84 tests, 0 failures, 0 errors**.
- Ran `./gradlew test -Dorg.gradle.java.home=/home/akoderman/.vscode/extensions/redhat.java-1.56.0-linux-x64/jre/21.0.12.1-linux-x86_64` successfully. The Java path overrides the repository's existing Windows-specific Gradle setting. Restored the missing, ignored wrapper JAR locally from an available Gradle wrapper before running tests.
- Chromium layout matrix: **48 combinations** across both views, 1440px and 390px viewports, and 0, 1, 2, 3, 4, 5, 8, 12, 19, 20, 21, and 40 people. Checked seat/arrow collisions, clipping, labels below icons, half/quarter/eighth geometry, overflow counts, keyboard tooltips, focus retention, stable colors, and page width. All passed.
- Live STOMP scenario with **40 people**: verified 20 visible seats, `+ 20 more`, exactly one replaced slot per overflow speaker, never-spoken-first replacement, retention of the 19 most recent non-chair speakers, matching chair/participant rosters, speaker and idle arrows, repeated-turn recency, and the same roster after participant refresh. All 12 checks passed; no browser JavaScript exceptions were recorded.
- Visually inspected desktop and phone screenshots, including the full table with 20 avatars.
- JavaScript syntax and Git whitespace checks passed. Whitespace checking used `core.whitespace=cr-at-eol` because existing Java files use CRLF endings.

## Practical limits

- Browser checks were run in Chromium. Firefox and Safari were not exercised.
- Twenty seats include the reserved chair position. With a vacant chair there are up to 19 displayed people plus the empty chair; the overflow count excludes that placeholder.
- Speaking history follows the existing in-memory room lifetime. It survives client refreshes, but not a server restart.
- Dense tables deliberately retain readable avatar and label sizes and use horizontal scrolling on smaller screens.

All five plan tasks are complete. The implementation is local on the feature branch; it has not been committed, merged, or deployed.
