package de.koderman;

import static org.junit.jupiter.api.Assertions.*;
import de.koderman.domain.*;
import de.koderman.infrastructure.*;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;

public class WebSocketErrorHandlingTest {

    @Test
    void testJoinMethodThrowsExceptionForNonExistentRoom() {
        // Create a controller with a mock messaging template
        SimpMessagingTemplate mockTemplate = mock(SimpMessagingTemplate.class);
        MeetingController controller = new MeetingController(mockTemplate);
        Join joinMessage = new Join("TestUser");
        
        // Mock StompHeaderAccessor
        StompHeaderAccessor headerAccessor = mock(StompHeaderAccessor.class);
        when(headerAccessor.getSessionId()).thenReturn("test-session");
        
        // Should throw RoomNotFoundException for non-existent room
        RoomNotFoundException exception = assertThrows(
            RoomNotFoundException.class,
            () -> controller.join("NONEXIST", joinMessage, headerAccessor)
        );
        
        assertEquals("Room not found: NONEXIST", exception.getMessage());
        assertEquals("NONEXIST", exception.getRoomCode());
    }
    
    @Test
    void testRequestMethodThrowsExceptionForNonExistentRoom() {
        SimpMessagingTemplate mockTemplate = mock(SimpMessagingTemplate.class);
        MeetingController controller = new MeetingController(mockTemplate);
        RequestSpeak requestMessage = new RequestSpeak("TestSpeaker");
        
        // Should throw RoomNotFoundException for non-existent room
        RoomNotFoundException exception = assertThrows(
            RoomNotFoundException.class,
            () -> controller.request("NONEXIST", requestMessage)
        );
        
        assertEquals("Room not found: NONEXIST", exception.getMessage());
        assertEquals("NONEXIST", exception.getRoomCode());
    }
    
    @Test
    void testWithdrawMethodThrowsExceptionForNonExistentRoom() {
        SimpMessagingTemplate mockTemplate = mock(SimpMessagingTemplate.class);
        MeetingController controller = new MeetingController(mockTemplate);
        Withdraw withdrawMessage = new Withdraw("TestUser");
        
        // Should throw RoomNotFoundException for non-existent room
        RoomNotFoundException exception = assertThrows(
            RoomNotFoundException.class,
            () -> controller.withdraw("NONEXIST", withdrawMessage)
        );
        
        assertEquals("Room not found: NONEXIST", exception.getMessage());
        assertEquals("NONEXIST", exception.getRoomCode());
    }
}