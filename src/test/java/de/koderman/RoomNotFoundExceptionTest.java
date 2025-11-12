package de.koderman;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class RoomNotFoundExceptionTest {

    @Test
    void testRoomRepositoryGetByCodeOrThrow_throwsExceptionForNonExistentRoom() {
        MeetingApp.RoomRepository repository = new MeetingApp.RoomRepository();
        
        // Should throw RoomNotFoundException for non-existent room
        MeetingApp.RoomNotFoundException exception = assertThrows(
            MeetingApp.RoomNotFoundException.class,
            () -> repository.getByCodeOrThrow("NONEXIST")
        );
        
        assertEquals("Room not found: NONEXIST", exception.getMessage());
        assertEquals("NONEXIST", exception.getRoomCode());
    }

    @Test
    void testRoomRepositoryGetByCodeOrThrow_returnsRoomForExistingRoom() {
        MeetingApp.RoomRepository repository = new MeetingApp.RoomRepository();
        
        // Create a room first
        MeetingApp.Room room = repository.createRoom("VALID");
        
        // Should return the room without throwing
        MeetingApp.Room retrievedRoom = repository.getByCodeOrThrow("VALID");
        
        assertNotNull(retrievedRoom);
        assertEquals(room.getRoomCode(), retrievedRoom.getRoomCode());
    }

    @Test
    void testRoomNotFoundExceptionProperties() {
        MeetingApp.RoomNotFoundException exception = new MeetingApp.RoomNotFoundException("TEST123");
        
        assertEquals("Room not found: TEST123", exception.getMessage());
        assertEquals("TEST123", exception.getRoomCode());
    }
}