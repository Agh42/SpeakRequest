# GitHub Copilot Instructions for SpeakRequest

## Project Overview

SpeakRequest is a real-time meeting manager for live and hybrid meetings, built with Spring Boot and WebSocket technology. It helps facilitators manage speak requests, conduct polls, and track speaking time during meetings.

### Key Technologies
- **Java 21** - Primary programming language
- **Spring Boot 3.3.3** - Application framework
- **WebSocket/STOMP** - Real-time bidirectional communication
- **Gradle 8.9** - Build system
- **Lombok** - Code generation for boilerplate reduction
- **Jakarta Validation** - Input validation

## Architecture

### Monolithic Design
The application follows a single-file architecture pattern where all components are defined in `MeetingApp.java`:
- Domain models (Room, Participant, etc.)
- WebSocket configuration
- REST controllers
- Message handlers
- Repository layer

### Key Components

1. **Domain Entities**
   - `Room`: Core entity managing meeting state, speak queue, timer, polls, and configuration
   - `Participant`: Represents a person in the speak queue
   - `Current`: Tracks the currently speaking participant with timer state
   - `RoomRepository`: In-memory storage with concurrent access control

2. **Communication Layer**
   - REST endpoints for room management (`/api/rooms/*`)
   - WebSocket endpoints for real-time updates (`/ws`)
   - STOMP messaging for bidirectional communication
   - Topic-based broadcasting (`/topic/room/{roomCode}/state`)

3. **State Management**
   - Thread-safe using `ReentrantLock` for Room operations
   - `ConcurrentHashMap` for room and session tracking
   - Automatic room limit enforcement (max 500,000 rooms)
   - Oldest room eviction when limit is reached

## Coding Standards

### Code Style
- **Single-file monolith**: All code resides in `MeetingApp.java`
- **Records for DTOs**: Use Java records for immutable data transfer objects
- **Lombok annotations**: Use `@Getter`, `@Setter`, `@RequiredArgsConstructor` where appropriate
- **Minimal comments**: Code should be self-documenting; avoid excessive comments
- **Java 21 features**: Utilize pattern matching, records, and modern Java syntax

### Validation
- Use Jakarta Validation annotations on records:
  - `@NotBlank` for required strings
  - `@Size` for length constraints
  - `@Pattern` for format validation
  - Maximum field sizes: names (30 chars), topics (100 chars), questions (200 chars)
- Name validation pattern: `^[a-zA-Z0-9 '.-]+$`

### Thread Safety
- Always use `lock.lock()` and `try-finally` pattern in Room methods
- Room state modifications must be synchronized
- Use `ConcurrentHashMap` for repository-level collections
- Session tracking must be thread-safe

### WebSocket Patterns
- Message handlers use `@MessageMapping("/room/{roomCode}/action")`
- Always normalize room codes with `normalizeRoomCode()` (converts "0" to "O")
- Broadcast state changes using `broadcast(roomCode)`
- Include session validation for chair-only operations
- Use `StompHeaderAccessor` to get session IDs

### Room Code Handling
- 4-character alphanumeric codes (A-Z, 1-9, excluding "0")
- Always normalize input: `normalizeRoomCode()` converts "0" to "O"
- Generate unique codes with collision checking
- Case-insensitive matching

## Development Workflow

### Building
```bash
./gradlew build
```

### Running Tests
```bash
./gradlew test
```

### Running Locally
```bash
./gradlew bootRun
# Or
java -jar build/libs/SpeakRequest-0.0.1-SNAPSHOT.jar
```
Application starts on `http://localhost:8080`

### Docker
```bash
docker build -t speakrequest .
docker run -p 8080:8080 speakrequest
```

## Feature Areas

### 1. Speak Request Management
- Participants request to speak via `RequestSpeak` message
- Chair advances through queue with `next` action
- Participants can withdraw from queue
- Duplicate names are prevented (case-insensitive)

### 2. Timer System
- Tracks speaking time for current participant
- Actions: start, pause, reset
- Configurable time limits (10-3600 seconds)
- Real-time elapsed time calculation

### 3. Chair Role Management
- Single chair per room (first-come, first-served)
- Chair role automatically released on disconnect
- Chair-only operations: polls, room config, room destruction
- Session-based authentication

### 4. Polling System
Three poll types:
- **YES_NO**: Binary choice polls
- **GRADIENTS**: 8-level gradient of agreement (OPT_1 to OPT_8)
- **MULTISELECT**: Custom options with configurable choices

Poll lifecycle:
1. ACTIVE - Voting in progress
2. ENDED - Results shown in overlay to participants
3. CLOSED - Poll hidden, results available in history

### 5. Room Configuration
Configurable aspects:
- **Topic**: Meeting subject
- **Meeting Goal**: Information sharing, decision making, etc.
- **Participation Format**: Structured, open discussion, etc.
- **Decision Rule**: Unanimity, majority, consensus, etc.
- **Deliverable**: Problem definition, milestone map, etc.

### 6. Room Lifecycle
- Creation: Generate unique 4-char code
- Destruction: Chair-initiated, broadcasts to all participants
- Auto-cleanup: Oldest rooms removed when limit reached
- Session tracking: Cleanup on disconnect

## Adding New Features

### Adding a New Message Handler
```java
@MessageMapping("/room/{roomCode}/newAction")
public void newAction(@DestinationVariable String roomCode, 
                     @Valid @Payload NewActionDTO msg, 
                     StompHeaderAccessor headerAccessor) {
    String normalizedRoomCode = normalizeRoomCode(roomCode);
    String sessionId = headerAccessor.getSessionId();
    
    roomRepository.getByCode(normalizedRoomCode)
        .ifPresent(room -> {
            // Validate permissions if needed
            if (room.isChairSession(sessionId)) {
                // Perform action with thread safety
                room.lock.lock();
                try {
                    // Modify room state
                } finally {
                    room.lock.unlock();
                }
                broadcast(normalizedRoomCode);
            }
        });
}
```

### Adding a New DTO
```java
public record NewActionDTO(
    @NotBlank(message = "Field is required")
    @Size(max = 100, message = "Field must not exceed 100 characters")
    String field
) {}
```

### Adding Room State
1. Add field to `Room` class
2. Include in `snapshot()` method
3. Update `State` record if needed for client visibility
4. Implement thread-safe getter/setter methods

## Testing Guidelines

### Test Structure
- Tests located in `src/test/java/de/koderman/`
- Use JUnit 5 (`@Test`, `@BeforeEach`, etc.)
- Spring Boot test support with `@SpringBootTest`
- Test concurrent access patterns

### Test Categories
1. **Unit Tests**: Individual Room methods
2. **Integration Tests**: WebSocket message flows
3. **Repository Tests**: Concurrent access and limits
4. **Validation Tests**: Input validation rules

### Writing Tests
```java
@Test
void shouldValidateName() {
    // Test validation logic
    assertThrows(ValidationException.class, () -> {
        // Invalid input
    });
}

@Test
void shouldHandleConcurrentAccess() {
    // Test thread safety
}
```

## Common Patterns

### Broadcasting State Updates
Always broadcast after state changes:
```java
private void broadcast(String roomCode) {
    roomRepository.getByCode(roomCode)
        .ifPresent(room -> {
            State s = room.snapshot();
            broker.convertAndSend("/topic/room/" + roomCode + "/state", s);
        });
}
```

### Thread-Safe State Modification
```java
public void modifyState() {
    lock.lock();
    try {
        // Modify internal state
    } finally {
        lock.unlock();
    }
}
```

### Optional Chaining
Use Optional for null-safe operations:
```java
roomRepository.getByCode(roomCode)
    .ifPresent(room -> {
        // Process room
    });
```

## Security Considerations

### Input Validation
- All user inputs validated with Jakarta Validation
- Size limits enforced on all string inputs
- Pattern validation for names and codes
- XSS protection: Client-side uses DOMPurify

### Session Management
- WebSocket sessions tracked for permission checks
- Chair role tied to session ID
- Automatic cleanup on disconnect
- No persistent authentication (stateless per-room)

### Rate Limiting
- No explicit rate limiting implemented
- Room limit (500K) prevents resource exhaustion
- Consider adding rate limiting for production deployments

## Frontend Integration

### Static Resources
- Location: `src/main/resources/static/`
- Pages: landing.html, chair.html, participant.html, popout.html
- Libraries: stomp.min.js, qrcode.min.js, purify.min.js

### WebSocket Connection
```javascript
const socket = new WebSocket('ws://localhost:8080/ws');
const stompClient = Stomp.over(socket);
stompClient.connect({}, frame => {
    stompClient.subscribe(`/topic/room/${roomCode}/state`, message => {
        // Handle state update
    });
});
```

## Deployment

### Configuration
- `application.yaml`: Server configuration
- Forward headers enabled for reverse proxy support
- Remote IP and protocol headers configured
- Use relative redirects

### Environment
- Java 21 required
- Port 8080 (configurable)
- No database required (in-memory storage)
- WebSocket support required

### Docker
- Multi-stage build using Gradle
- Runs as non-root user
- Exposes port 8080
- Health check: `/healthz`

## Troubleshooting

### Common Issues

1. **WebSocket Connection Failures**
   - Check CORS configuration
   - Verify endpoint path `/ws`
   - Ensure WebSocket support in reverse proxy

2. **Thread Safety Issues**
   - Always use lock in Room modifications
   - Check for race conditions in concurrent access
   - Verify proper finally blocks for lock release

3. **Room Code Confusion**
   - Remember "0" is normalized to "O"
   - Case-insensitive matching
   - 4-character alphanumeric only

4. **Memory Issues**
   - Monitor room count (max 500K)
   - Check session cleanup on disconnect
   - Verify oldest room eviction works

## Best Practices

1. **Maintain Monolith**: Keep code in single file unless complexity demands refactoring
2. **Use Records**: Prefer records for DTOs and immutable data
3. **Validate Early**: Use Jakarta Validation on all inputs
4. **Lock Consistently**: Always use lock for Room state modifications
5. **Broadcast Changes**: Update clients after every state change
6. **Normalize Codes**: Always normalize room codes before use
7. **Test Concurrency**: Include concurrent access tests for new features
8. **Document Enums**: Provide display names and descriptions
9. **Handle Disconnects**: Clean up resources properly
10. **Use Optionals**: Embrace Optional for null safety

## Future Considerations

- Consider extracting to multi-file architecture if complexity grows
- Add persistent storage layer if needed
- Implement rate limiting for production
- Add authentication for advanced features
- Consider horizontal scaling (requires shared state)
- Add analytics and logging
- Implement room expiration based on inactivity
