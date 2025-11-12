package de.koderman;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;

public class WebSocketErrorHandlingTest {

    @Test
    void testJoinMethodThrowsExceptionForNonExistentRoom() {
        // Create a controller with a mock messaging template
        SimpMessagingTemplate mockTemplate = mock(SimpMessagingTemplate.class);
        MeetingApp.MeetingController controller = new MeetingApp.MeetingController(mockTemplate);
        MeetingApp.Join joinMessage = new MeetingApp.Join("TestUser");
        
        // Mock StompHeaderAccessor
        StompHeaderAccessor headerAccessor = mock(StompHeaderAccessor.class);
        when(headerAccessor.getSessionId()).thenReturn("test-session");
        
        // Should throw RoomNotFoundException for non-existent room
        MeetingApp.RoomNotFoundException exception = assertThrows(
            MeetingApp.RoomNotFoundException.class,
            () -> controller.join("NONEXIST", joinMessage, headerAccessor)
        );
        
        assertEquals("Room not found: NONEXIST", exception.getMessage());
        assertEquals("NONEXIST", exception.getRoomCode());
    }
    
    @Test
    void testRequestMethodThrowsExceptionForNonExistentRoom() {
        SimpMessagingTemplate mockTemplate = mock(SimpMessagingTemplate.class);
        MeetingApp.MeetingController controller = new MeetingApp.MeetingController(mockTemplate);
        MeetingApp.RequestSpeak requestMessage = new MeetingApp.RequestSpeak("TestSpeaker");
        
        // Should throw RoomNotFoundException for non-existent room
        MeetingApp.RoomNotFoundException exception = assertThrows(
            MeetingApp.RoomNotFoundException.class,
            () -> controller.request("NONEXIST", requestMessage)
        );
        
        assertEquals("Room not found: NONEXIST", exception.getMessage());
        assertEquals("NONEXIST", exception.getRoomCode());
    }
    
    @Test
    void testWithdrawMethodThrowsExceptionForNonExistentRoom() {
        SimpMessagingTemplate mockTemplate = mock(SimpMessagingTemplate.class);
        MeetingApp.MeetingController controller = new MeetingApp.MeetingController(mockTemplate);
        MeetingApp.Withdraw withdrawMessage = new MeetingApp.Withdraw("TestUser");
        
        // Should throw RoomNotFoundException for non-existent room
        MeetingApp.RoomNotFoundException exception = assertThrows(
            MeetingApp.RoomNotFoundException.class,
            () -> controller.withdraw("NONEXIST", withdrawMessage)
        );
        
        assertEquals("Room not found: NONEXIST", exception.getMessage());
        assertEquals("NONEXIST", exception.getRoomCode());
    }
}