package de.koderman;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests to verify that rooms cannot be created through join or assumeChair actions,
 * only through the explicit /api/rooms endpoint.
 */
class RoomCreationSecurityTest {

    private MeetingApp.RoomRepository repository;

    @BeforeEach
    void setUp() {
        repository = new MeetingApp.RoomRepository();
    }

    @Test
    void testGetByCode_returnsEmptyForNonExistentRoom() {
        // Verify that getByCode doesn't create a room
        assertFalse(repository.getByCode("NONEXIST").isPresent());
        assertFalse(repository.exists("NONEXIST"));
    }

    @Test
    void testGetByCode_onlyReturnsExistingRooms() {
        // Create a room explicitly
        repository.createRoom("VALID");
        
        // getByCode should return the existing room
        assertTrue(repository.getByCode("VALID").isPresent());
        
        // But should not create non-existing rooms
        assertFalse(repository.getByCode("INVALID").isPresent());
        assertFalse(repository.exists("INVALID"));
    }

    @Test
    void testCreateRoom_isOnlyWayToCreateRooms() {
        String roomCode = "TEST123";
        
        // Room doesn't exist initially
        assertFalse(repository.exists(roomCode));
        assertFalse(repository.getByCode(roomCode).isPresent());
        
        // Create room explicitly
        MeetingApp.Room room = repository.createRoom(roomCode);
        assertNotNull(room);
        
        // Now room exists
        assertTrue(repository.exists(roomCode));
        assertTrue(repository.getByCode(roomCode).isPresent());
    }

    @Test
    void testGetByCode_doesNotAutoCreateRooms() {
        // Multiple calls to getByCode should not create the room
        assertFalse(repository.getByCode("AUTO1").isPresent());
        assertFalse(repository.getByCode("AUTO1").isPresent());
        assertFalse(repository.getByCode("AUTO1").isPresent());
        
        // Room should still not exist
        assertFalse(repository.exists("AUTO1"));
    }

    @Test
    void testMultipleCallsToCreateRoom_returnSameInstance() {
        String roomCode = "MULTI";
        
        MeetingApp.Room room1 = repository.createRoom(roomCode);
        MeetingApp.Room room2 = repository.createRoom(roomCode);
        
        assertSame(room1, room2, "Multiple createRoom calls should return the same instance");
    }
}
