# TESTING.md — Test Structure & Practices

## Framework

- **JUnit 5** (Jupiter) via `spring-boot-starter-test`
- **Mockito** — for mocking `SimpMessagingTemplate` and `StompHeaderAccessor`
- **Jakarta Validation API** — instantiated directly in `ValidationTest` (no Spring context)
- Tests run with `./gradlew test`
- Single test class: `./gradlew test --tests "de.koderman.ClassName"`
- Single test method: `./gradlew test --tests "de.koderman.ClassName.methodName"`

## Test Organization

All test classes are in `src/test/java/de/koderman/` (no subpackages).

| Test Class | What It Tests |
|------------|--------------|
| `RoomCreationFlowTest` | Controller + repository integration via reflection — room creation, code uniqueness, room lookup |
| `RoomCreationSecurityTest` | Room code generation (charset validation, no zeros), code normalization |
| `RoomLimitIntegrationTest` | Eviction boundary — oldest room removed when limit reached, session cleanup |
| `RoomLimitDemonstrationTest` | Narrative demonstration of eviction semantics (more documentation than assertion) |
| `RoomNotFoundExceptionTest` | Exception message format and `getRoomCode()` accessor |
| `RoomRepositoryTest` | Repository CRUD, session tracking, `trackSession`, `untrackSession`, `getBySessionId` |
| `ValidationTest` | Jakarta Validation on `RequestSpeak` — valid names, too long, invalid chars, blank, null |
| `WebSocketErrorHandlingTest` | `RoomNotFoundException` thrown by `join`, `request`, `withdraw` for non-existent rooms |

## Testing Approach

### No Spring Context in Unit/Integration Tests
Tests instantiate controllers and repositories directly — no `@SpringBootTest`, no `@WebMvcTest`, no test slice. Spring context startup is avoided for speed.

```java
@BeforeEach
void setUp() {
    SimpMessagingTemplate mockBroker = mock(SimpMessagingTemplate.class);
    controller = new MeetingController(mockBroker);
}
```

### Reflection for Internal State Access
Tests that need to inspect or configure internal fields (e.g., `maxRooms`, `roomsByCode`) use `Field.setAccessible(true)`:
```java
Field field = MeetingController.class.getDeclaredField("roomRepository");
field.setAccessible(true);
repository = (RoomRepository) field.get(controller);
```

### Mockito for Infrastructure Dependencies
`SimpMessagingTemplate` and `StompHeaderAccessor` are mocked; messaging side effects aren't verified in most tests.

```java
StompHeaderAccessor headerAccessor = mock(StompHeaderAccessor.class);
when(headerAccessor.getSessionId()).thenReturn("test-session");
```

### Validation Tests Using Direct Validator
`ValidationTest` creates `Validator` directly without Spring:
```java
ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
validator = factory.getValidator();
```

## Coverage Gaps (Observed)

- No tests for `Room` methods directly (timer, poll lifecycle, chair release).
- No tests for `MeetingController` STOMP broadcast behavior (messaging template interactions not verified).
- No tests for disconnect event handler (`SessionDisconnectEvent`).
- No end-to-end WebSocket integration tests (no embedded STOMP client).
- No tests at the `@SpringBootTest` level; boot context correctness is assumed.
- `RoomLimitDemonstrationTest` is more of a working example than an automated assertion — field name `MAX_ROOMS` referenced in test doesn't match actual field `maxRooms` in `RoomRepository`.

## Test Conventions

- Test data uses simple strings: `"TEST"`, `"OLDEST"`, `"MIDDLE"`, `"NEWER"`, `"FAKE"`.
- `Thread.sleep(50)` used in eviction tests to ensure distinct timestamps.
- `assertThrows` used for exception path verification.
- No test utilities, factories, or helpers — each test sets up its own state inline.
