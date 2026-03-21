package de.koderman;

import static org.junit.jupiter.api.Assertions.*;
import de.koderman.config.MessageService;
import de.koderman.domain.*;
import de.koderman.infrastructure.*;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;

public class WebSocketErrorHandlingTest {

    private MeetingController createController() {
        SimpMessagingTemplate mockTemplate = mock(SimpMessagingTemplate.class);
        RoomRepository mockRepository = mock(RoomRepository.class);
        MessageService mockMessageService = mock(MessageService.class);
        
        // Configure mocks
        when(mockMessageService.getMessage(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
        when(mockMessageService.getMessage(anyString(), any(Object[].class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(mockRepository.getByCodeOrThrow(anyString())).thenThrow(new RoomNotFoundException("NONEXIST"));
        
        return new MeetingController(mockTemplate, mockRepository, mockMessageService);
    }

    @Test
    void testJoinMethodThrowsExceptionForNonExistentRoom() {
        // Create a controller with mocks
        MeetingController controller = createController();
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
        MeetingController controller = createController();
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
        MeetingController controller = createController();
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