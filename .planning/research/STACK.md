# Stack Research

**Domain:** Collaborative meeting moderation UI and realtime room-state synchronization
**Researched:** 2026-03-28
**Confidence:** MEDIUM

## Recommended Stack

### Core Technologies

| Technology | Version | Purpose | Why Recommended |
|------------|---------|---------|-----------------|
| Spring Boot | 3.3.3 | Backend monolith, REST, and STOMP/WebSocket endpoints | This milestone only extends the existing room-state contract; Spring Boot already provides the controller, validation, and websocket plumbing needed for room-member persistence and chair actions. |
| Java | 21 | Server runtime and domain model | The project is already pinned to Java 21 in the Gradle toolchain, so there is no stack change required to support the new room-state fields or timer behavior. |
| Vanilla HTML/CSS/JavaScript | Current static frontend | Chair, participant, popout, and landing views | The requested changes are presentation/state updates inside the current static app. Native DOM updates, CSS clamping, and existing click/scroll behavior are enough. |
| STOMP over WebSocket | Bundled client (`stomp.min.js`) | Realtime room-state broadcast delivery | The milestone continues the existing `/topic/room/{code}/state` model. Keeping the same STOMP client avoids contract drift and preserves all current listeners. |
| DOMPurify | 3.0.6 | Sanitizing dynamic room, topic, and participant text | Avatar labels, room titles, and topic strings still come from user-controlled state, so the existing sanitizer remains the right protection layer. |
| Tailwind CSS CDN | Unpinned CDN runtime | Utility styling in the chair surface | The chair view already uses the CDN runtime and tokenized palette. The UI work here is layout/class tuning, not a styling-system migration. |

### Supporting Libraries

| Library | Version | Purpose | When to Use |
|---------|---------|---------|-------------|
| QRCode.js | Bundled client (`qrcode.min.js`) | Join/share QR rendering | Keep for the existing room-share flow; not needed for this milestone’s new features. |
| Native browser APIs | N/A | `scrollIntoView`, `IntersectionObserver`, `classList`, `setInterval`, CSS line clamp | Use these for clickable topic navigation, active-section highlighting, timer thresholds, and two-line room-title truncation. No extra dependency is needed. |

### Development Tools

| Tool | Purpose | Notes |
|------|---------|-------|
| Gradle Wrapper | 9.4.0 | Builds and tests the Spring Boot app | Keep the existing wrapper; no build plugin changes are required for this milestone. |
| Browser DevTools | UI inspection and layout debugging | Useful for verifying timer color thresholds, line clamping, and anchor scrolling on the static pages. |

## Installation

```bash
# No new runtime packages are required for this milestone.
# Keep the existing Gradle-managed backend and bundled static assets.
```

## Alternatives Considered

| Recommended | Alternative | When to Use Alternative |
|-------------|-------------|-------------------------|
| Existing Spring Boot monolith | Split frontend into a SPA framework | Only if the product later needs client-side routing, heavy local state, or shared component libraries. Not justified for this milestone. |
| Existing static HTML + vanilla JS | React/Vue/Svelte | Only if the chair/participant surfaces become substantially more interactive than simple state rendering and section navigation. |
| In-memory room state | Database or Redis-backed presence store | Only if the app must survive multi-node deployment or long-lived member identity across restarts. This milestone does not need that. |
| Native CSS and DOM APIs | New scroll/layout helper library | Only if the browser-native approach becomes too inconsistent across supported browsers. Current feature scope does not require it. |

## What NOT to Use

| Avoid | Why | Use Instead |
|-------|-----|-------------|
| React or another SPA framework | It adds a build pipeline and state-management surface without solving any requirement in this milestone. | Keep the existing static pages and update DOM/CSS directly. |
| A new client router | The topic jump is an intra-page scroll action, not navigation. | Reuse the existing `scrollIntoView` / section-navigation pattern already present in the chair view. |
| Database persistence for room members | The feature is session-scoped presence, not durable business data. | Extend the existing `Room` state and WebSocket-session mapping. |
| Redis or another cache layer | Overkill for single-process room presence and unnecessary for the current milestone scope. | Keep state in the current in-memory room model. |
| A new websocket protocol or message broker | It would break the existing room-state broadcast contract. | Preserve STOMP destinations and extend the payload shape only. |

## Stack Patterns by Variant

**If the change is only visual or navigation-related:**
- Stay in the current chair HTML/CSS/JS surface.
- Use native DOM APIs for click targets, scroll behavior, and timer class toggles.
- Because these changes are local to the rendered state and do not alter transport or persistence.

**If the change needs state to survive disconnects within the same room process:**
- Extend the existing `Room` aggregate and session tracking.
- Because the current architecture already treats the room state broadcast as the source of truth.

**If future milestones require cross-node persistence:**
- Introduce an external store only then, not now.
- Because the present milestone is still single-process and the extra infrastructure would be premature.

## Version Compatibility

| Package A | Compatible With | Notes |
|-----------|-----------------|-------|
| Spring Boot 3.3.3 | Java 21 | Already pinned in `build.gradle`; no upgrade needed for this milestone. |
| DOMPurify 3.0.6 | Current static chair/participant/popout pages | Existing sanitizer version is sufficient for room names, topic text, and avatar labels. |
| Gradle 9.4.0 | Java 21 toolchain | Matches the current wrapper and build configuration. |

## Sources

- `build.gradle` — confirms Spring Boot 3.3.3, Java 21, Web/WebSocket/Validation, and no frontend package manager.
- `gradle/wrapper/gradle-wrapper.properties` — confirms Gradle 9.4.0.
- `src/main/resources/static/chair.html` — confirms the current static UI uses Tailwind CDN, STOMP, DOMPurify, and native section scrolling.
- `src/main/resources/static/participant.html` — confirms the same vanilla JS/STOMP/DOMPurify pattern on the participant surface.
- `src/main/resources/static/purify.min.js` — confirms the bundled DOMPurify 3.0.6 asset.

---
*Stack research for: v1.1 UI Improvements milestone*
*Researched: 2026-03-28*
