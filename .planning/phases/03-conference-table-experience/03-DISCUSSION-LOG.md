# Phase 3: Conference Table Experience - Discussion Log (Assumptions Mode)

> **Audit trail only.** Do not use as input to planning, research, or execution agents.
> Decisions captured in CONTEXT.md — this log preserves the analysis.

**Date:** 2026-03-25
**Phase:** 03-conference-table-experience
**Mode:** assumptions
**Areas analyzed:** Layout Scope, Identity Rendering, Status Encoding, Timer Hierarchy

## Assumptions Presented

### Layout Scope
| Assumption | Confidence | Evidence |
|------------|-----------|----------|
| Phase 3 should convert the center column into a desktop-only round-table visualization that replaces the current list-only center shell, while leaving the mobile section navigation intact and the non-table sections usable on small screens. | Confident | `.planning/ROADMAP.md`, `.planning/phases/02-structural-layout-shell/02-CONTEXT.md`, `src/main/resources/static/chair.html` |

### Identity Rendering
| Assumption | Confidence | Evidence |
|------------|-----------|----------|
| Participants should be represented as monogram avatars with deterministic name-derived colors rather than photos or generic icons. | Confident | `.planning/ROADMAP.md`, `src/main/resources/static/chair.html`, `src/chair-view-redesign/code.html` |

### Status Encoding
| Assumption | Confidence | Evidence |
|------------|-----------|----------|
| The active speaker should be emphasized with the tertiary highlight treatment, and queued participants should show explicit ordering markers so the table reads as an operational queue, not just a decorative circle. | Confident | `.planning/ROADMAP.md`, `src/chair-view-redesign/DESIGN.md`, `src/chair-view-redesign/code.html` |

### Timer Hierarchy
| Assumption | Confidence | Evidence |
|------------|-----------|----------|
| The centerpiece should prioritize the active speaker timer and room code as the primary read, with the meeting timer remaining secondary only if it fits without diluting the table. | Likely | `.planning/ROADMAP.md`, `src/main/resources/static/chair.html`, `src/chair-view-redesign/code.html` |

## Corrections Made

### Timer Hierarchy
- **Original assumption:** The centerpiece should prioritize the active speaker timer and room code as the primary read, with the meeting timer remaining secondary only if it fits without diluting the table.
- **User correction:** Use the current speaker countdown and room topic as the main focal point; remove room code from the center table; keep showing current speaker initials, total meeting time, and remaining speaker time in smaller text.
- **Reason:** Not provided.

## External Research

None.
