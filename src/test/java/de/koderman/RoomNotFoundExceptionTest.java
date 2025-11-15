package de.koderman;

import static org.junit.jupiter.api.Assertions.*;
import de.koderman.domain.*;
import de.koderman.infrastructure.*;


import org.junit.jupiter.api.Test;

public class RoomNotFoundExceptionTest {

    @Test
    void testRoomRepositoryGetByCodeOrThrow_throwsExceptionForNonExistentRoom() {
        RoomRepository repository = new RoomRepository();
        
        // Should throw RoomNotFoundException for non-existent room
        RoomNotFoundException exception = assertThrows(
            RoomNotFoundException.class,
            () -> repository.getByCodeOrThrow("NONEXIST")
        );
        
        assertEquals("Room not found: NONEXIST", exception.getMessage());
        assertEquals("NONEXIST", exception.getRoomCode());
    }

    @Test
    void testRoomRepositoryGetByCodeOrThrow_returnsRoomForExistingRoom() {
        RoomRepository repository = new RoomRepository();
        
        // Create a room first
        Room room = repository.createRoom("VALID");
        
        // Should return the room without throwing
        Room retrievedRoom = repository.getByCodeOrThrow("VALID");
        
        assertNotNull(retrievedRoom);
        assertEquals(room.getRoomCode(), retrievedRoom.getRoomCode());
    }

    @Test
    void testRoomNotFoundExceptionProperties() {
        RoomNotFoundException exception = new RoomNotFoundException("TEST123");
        
        assertEquals("Room not found: TEST123", exception.getMessage());
        assertEquals("TEST123", exception.getRoomCode());
    }
}