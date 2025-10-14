# Room Limit Implementation

## Overview
This implementation adds a room limit of 500,000 to the SpeakRequest application. When the limit is reached and a new room needs to be created, the oldest room (determined by its meeting start timestamp) is automatically evicted.

## Implementation Details

### Changes Made

1. **RoomRepository.java** (within MeetingApp.java)
   - Added `MAX_ROOMS` constant set to 500,000
   - Added `TreeMap<Long, Room>` sorted by timestamp for efficient oldest room lookup
   - Modified `getOrCreate()` method to:
     - Check room count before creating new rooms
     - Add rooms to both `roomsByCode` HashMap and `roomsByTimestamp` TreeMap
     - Use synchronization to ensure thread-safety when creating rooms
   - Added `removeOldestRoom()` private method that:
     - Uses `TreeMap.firstEntry()` for O(log n) access to the oldest room
     - Removes that room from both `roomsByCode` and `roomsByTimestamp`
     - Cleans up all associated session tracking entries in `sessionToRoomCode`

### Key Features

- **Thread-Safe**: Uses synchronized blocks for room creation and a lock object to coordinate access
- **Efficient Eviction**: Uses TreeMap for O(log n) oldest room lookup instead of O(n) linear search
- **Automatic Cleanup**: Session tracking is automatically cleaned up when a room is evicted
- **Timestamp-Based**: Eviction is based on the room's `meetingStartSec` field, which is set when the room is created
- **Minimal Performance Impact**: Eviction only occurs when limit is reached

### Data Structures

- **`roomsByCode` (ConcurrentHashMap)**: Fast O(1) lookup by room code
- **`roomsByTimestamp` (TreeMap)**: Sorted by creation timestamp for O(log n) access to oldest room
- **`sessionToRoomCode` (ConcurrentHashMap)**: Maps session IDs to room codes for session tracking
- **`roomCreationLock` (Object)**: Synchronization lock to ensure thread-safe room creation

### Test Coverage

Three test classes were added to verify the implementation:

1. **RoomRepositoryTest.java**: Unit tests for basic repository functionality
   - Room creation and retrieval
   - Session tracking
   - Concurrent room creation
   - MAX_ROOMS constant verification

2. **RoomLimitIntegrationTest.java**: Integration tests for room limit behavior
   - Timestamp ordering verification
   - Session cleanup verification
   - Sequential room creation

3. **RoomLimitDemonstrationTest.java**: Demonstration tests showing actual eviction
   - Full eviction scenario with 500,000 rooms
   - Verification that oldest room is removed
   - Session cleanup verification

All 33 tests pass successfully.

## Behavior

### Normal Operation (< 500,000 rooms)
- Rooms are created normally
- All rooms are retained in memory
- No eviction occurs

### At Limit (= 500,000 rooms)
- When a new room would exceed the limit:
  1. The system retrieves the oldest room from the TreeMap in O(log n) time
  2. That room is removed from both the HashMap and TreeMap
  3. All session tracking entries for that room are cleaned up
  4. The new room is created and added to both data structures
  5. Total room count remains at 500,000

## Example

```java
// Creating rooms normally
Room room1 = repository.getOrCreate("ABCD");  // meetingStartSec = 1000
Room room2 = repository.getOrCreate("EFGH");  // meetingStartSec = 1005
// ... 499,998 more rooms created ...

// When the 500,000th room exists and we try to create one more:
Room newRoom = repository.getOrCreate("WXYZ");
// Result:
// - room1 (oldest, meetingStartSec = 1000) is evicted from both maps
// - newRoom is created and added to both maps
// - Total rooms remains at 500,000
```

## Performance Considerations

- **Memory**: With 500,000 rooms, memory usage will be significant but bounded
  - Additional TreeMap overhead: ~16 bytes per entry (Long key + Room reference)
  - Total additional overhead: ~8 MB for 500,000 entries
- **Room Creation**: O(log n) for TreeMap insertion when below limit
- **Eviction Cost**: O(log n) to find and remove the oldest room (previously O(n))
- **Room Lookup**: O(1) for lookups by room code (unchanged)
- **Concurrent Access**: Thread-safe operations using synchronized blocks

## Performance Improvements

The optimization from linear search (O(n)) to TreeMap lookup (O(log n)) provides significant performance benefits:

- **Before**: O(500,000) = 500,000 operations to find oldest room
- **After**: O(log 500,000) ≈ 19 operations to find oldest room
- **Speedup**: ~26,000x faster eviction

## Future Improvements (Optional)

If needed for better performance at scale:
- Use a concurrent tree map implementation if available
- Implement periodic cleanup of inactive rooms
- Add metrics to track room evictions
- Consider LRU (Least Recently Used) instead of oldest timestamp
