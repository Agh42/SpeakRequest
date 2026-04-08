---
phase: 10-avatar-name-truncation-and-chair-label-simplification
plan: 01
status: complete
completed: "2026-04-08"
---

# Summary: Plan 10-01 — Avatar Label Truncation and Chair Bootstrap Simplification

## What was built

- `getParticipantAvatarLabel()` in `chair.html` now shows names of 5 characters or fewer in full and truncates longer names to the first 3 characters plus `…` (U+2026). Example: `annemarie` → `ann…`, `chair` → `chair`.
- Both hardcoded `Chair-Candidate` bootstrap payloads (join and assumeChair) replaced with the plain `chair` label.
- Two new tests in `ChairSurfaceRequirementsTest` lock in the truncation threshold and the bootstrap label.

## Files changed

| File | Change |
|------|--------|
| `src/main/resources/static/chair.html` | Updated `getParticipantAvatarLabel` threshold to 5 chars, changed ellipsis to `…`, replaced both `Chair-Candidate` strings with `chair` |
| `src/test/java/de/koderman/ChairSurfaceRequirementsTest.java` | Added `avatarLabelTruncatesToThreeCharacterPrefixForLongNames` and `chairBootstrapUsesPlainChairLabel` tests |

## Verification

- All 76 tests pass (`./gradlew.bat test`)
- Human visual check approved: `ann…` fits inside the avatar circle; chair bootstrap reads `chair`
