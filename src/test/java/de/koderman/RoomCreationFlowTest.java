package de.koderman;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Integration tests to verify the complete room creation flow
 * and ensure rooms can only be created via /api/rooms endpoint.
 */
class RoomCreationFlowTest {

    private MeetingApp.RoomRepository repository;
    private MeetingApp.MeetingController controller;

    @BeforeEach
    void setUp() {
        // Create a mock SimpMessagingTemplate since we don't need actual messaging
        SimpMessagingTemplate mockBroker = mock(SimpMessagingTemplate.class);
        controller = new MeetingApp.MeetingController(mockBroker);
        
        // Access the repository through reflection to test it directly
        try {
            var field = MeetingApp.MeetingController.class.getDeclaredField("roomRepository");
            field.setAccessible(true);
            repository = (MeetingApp.RoomRepository) field.get(controller);
        } catch (Exception e) {
            throw new RuntimeException("Failed to access roomRepository", e);
        }
    }

    @Test
    void testCreateRoomEndpoint_createsRoomWithRandomCode() {
        // Call the /api/rooms endpoint
        MeetingApp.RoomInfo roomInfo = controller.createRoom();
        
        // Verify room was created with a 4-character code
        assertNotNull(roomInfo.roomCode());
        assertEquals(4, roomInfo.roomCode().length());
        assertTrue(roomInfo.exists());
        
        // Verify room exists in repository
        assertTrue(repository.exists(roomInfo.roomCode()));
        assertTrue(repository.getByCode(roomInfo.roomCode()).isPresent());
    }

    @Test
    void testCreateRoomEndpoint_createsUniqueRoomCodes() {
        // Create multiple rooms
        MeetingApp.RoomInfo room1 = controller.createRoom();
        MeetingApp.RoomInfo room2 = controller.createRoom();
        MeetingApp.RoomInfo room3 = controller.createRoom();
        
        // Verify all room codes are unique
        assertNotEquals(room1.roomCode(), room2.roomCode());
        assertNotEquals(room1.roomCode(), room3.roomCode());
        assertNotEquals(room2.roomCode(), room3.roomCode());
        
        // Verify all rooms exist
        assertTrue(repository.exists(room1.roomCode()));
        assertTrue(repository.exists(room2.roomCode()));
        assertTrue(repository.exists(room3.roomCode()));
    }

    @Test
    void testCheckRoomEndpoint_returnsTrueForExistingRoom() {
        // Create a room
        MeetingApp.RoomInfo created = controller.createRoom();
        
        // Check if room exists
        MeetingApp.RoomInfo checked = controller.checkRoom(created.roomCode());
        
        assertTrue(checked.exists());
        assertEquals(created.roomCode(), checked.roomCode());
    }

    @Test
    void testCheckRoomEndpoint_returnsFalseForNonExistentRoom() {
        // Check a room that doesn't exist
        MeetingApp.RoomInfo checked = controller.checkRoom("FAKE");
        
        assertFalse(checked.exists());
        assertEquals("FAKE", checked.roomCode());
    }

    @Test
    void testRepositoryGetByCode_doesNotCreateRooms() {
        String roomCode = "TEST";
        
        // Try to get a non-existent room
        assertFalse(repository.getByCode(roomCode).isPresent());
        
        // Verify room was not created
        assertFalse(repository.exists(roomCode));
        
        // Try again
        assertFalse(repository.getByCode(roomCode).isPresent());
        
        // Still doesn't exist
        assertFalse(repository.exists(roomCode));
    }

    @Test
    void testCannotCreateRoomByGuessing_roomCode() {
        String guessedCode = "ABCD";
        
        // Verify room doesn't exist
        assertFalse(repository.exists(guessedCode));
        
        // Try to access it via getByCode (simulating a join attempt)
        assertFalse(repository.getByCode(guessedCode).isPresent());
        
        // Verify it still doesn't exist
        assertFalse(repository.exists(guessedCode));
        
        // Only createRoom can create it
        repository.createRoom(guessedCode);
        
        // Now it exists
        assertTrue(repository.exists(guessedCode));
        assertTrue(repository.getByCode(guessedCode).isPresent());
    }
}
