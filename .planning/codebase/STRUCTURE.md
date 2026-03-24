# STRUCTURE.md — Directory Layout & Organization

## Top-Level Layout

```
SpeakRequest/
├── application.yaml          # Spring Boot config (also baked into Docker image)
├── build.gradle              # Gradle build definition (Groovy DSL)
├── Dockerfile                # Two-stage build: gradle:8.9-jdk21 → eclipse-temurin:21-jre
├── gradlew / gradlew.bat     # Gradle wrapper scripts
├── gradle/wrapper/           # Gradle wrapper properties
├── build.sh / run.sh         # Shell convenience scripts (Linux/CI)
├── README.md
├── LICENSE
├── docs/                     # Design documentation (SHARE_BUTTON*.md, images/)
├── src/
│   ├── chair-view-redesign/  # WIP design prototype (code.html, DESIGN.md) — not in main build
│   ├── main/
│   │   ├── java/de/koderman/
│   │   │   ├── MeetingApp.java
│   │   │   ├── config/
│   │   │   ├── domain/
│   │   │   └── infrastructure/
│   │   └── resources/
│   │       └── static/       # Spring Boot serves these as static files
│   └── test/
│       └── java/de/koderman/
└── .planning/                # GSD planning artifacts (not in main build)
```

## Java Package Layout

```
de.koderman
├── MeetingApp.java                        # Spring Boot entry point
├── config/
│   ├── WsConfig.java                      # WebSocket/STOMP broker config + heartbeat scheduler
│   └── StartupPropertiesLogger.java       # @PostConstruct config logger
├── domain/
│   ├── Room.java                          # Core aggregate — all mutable state
│   ├── RoomRepository.java                # In-memory repository + session tracker
│   │
│   ├── # Request/command records (STOMP inbound)
│   ├── Join.java, RequestSpeak.java, Withdraw.java
│   ├── AssumeChair.java, TimerCtrl.java, SetLimit.java
│   ├── StartPoll.java, CastVote.java
│   ├── UpdateRoomConfig.java, CreateRoom.java, DestroyRoom.java
│   │
│   ├── # State/projection records (outbound)
│   ├── State.java                         # Full room snapshot
│   ├── Current.java                       # Current speaker info
│   ├── Participant.java                   # id + name + requestedAt
│   ├── PollState.java                     # Active/ended poll data
│   ├── PollResults.java                   # Closed poll result snapshot
│   ├── RoomConfig.java                    # topic + goal + format + rule + deliverable
│   ├── RoomInfo.java                      # roomCode + exists boolean (REST response)
│   ├── RoomDestroyed.java                 # message + landingUrl
│   ├── RoomError.java                     # message + roomCode + errorCode + redirectUrl
│   │
│   ├── # Enums with display metadata
│   ├── MeetingGoal.java, ParticipationFormat.java
│   ├── DecisionRule.java, Deliverable.java
│   │
│   └── # Exceptions
│       ├── RoomNotFoundException.java
│       └── ChairAccessException.java
└── infrastructure/
    ├── MeetingController.java             # REST + STOMP + disconnect event handler
    └── Health.java                        # /healthz
```

## Static Frontend Layout

```
src/main/resources/static/
├── landing.html              # Entry page — create room / join by code
├── chair.html                # Chair view — full meeting controls
├── participant.html          # Participant view — queue + poll
├── popout.html               # Popout timer/queue view
├── index.html                # Redirect to landing (or alternate entry)
├── legal.html                # Legal/terms page
├── styles.css                # Shared stylesheet
├── metadata-loader.js        # Fetches+caches enum metadata from /api/metadata/*
├── share.js                  # Web Share API + fallback modal
├── stomp.min.js              # STOMP client library (vendored)
├── purify.min.js             # DOMPurify (vendored)
├── qrcode.min.js             # QR code generator (vendored)
├── fontawesome/              # Font Awesome icons (vendored)
│   ├── all.min.css
│   └── webfonts/
├── img/                      # Images
└── test-*.html               # Developer test pages (test-metadata.html, test-share.html)
```

## Test Layout

```
src/test/java/de/koderman/
├── RoomCreationFlowTest.java          # Controller + repository integration (no Spring context)
├── RoomCreationSecurityTest.java      # Room code generation + normalization
├── RoomLimitDemonstrationTest.java    # Narrative demonstration of eviction behavior
├── RoomLimitIntegrationTest.java      # Eviction boundary conditions
├── RoomNotFoundExceptionTest.java     # Exception shape and messages
├── RoomRepositoryTest.java            # Repository CRUD + session tracking
├── ValidationTest.java                # Jakarta Validation on request records
└── WebSocketErrorHandlingTest.java    # Controller exception propagation
```

## Key File Locations

| What | Where |
|------|-------|
| App config | `application.yaml` (root) |
| Core domain logic | `src/main/java/de/koderman/domain/Room.java` |
| All HTTP + STOMP routes | `src/main/java/de/koderman/infrastructure/MeetingController.java` |
| WebSocket config | `src/main/java/de/koderman/config/WsConfig.java` |
| Static frontend | `src/main/resources/static/` |
| Build definition | `build.gradle` |
| Docker packaging | `Dockerfile` |
