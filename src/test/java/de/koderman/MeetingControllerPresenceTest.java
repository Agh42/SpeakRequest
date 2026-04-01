package de.koderman;

import de.koderman.domain.Join;
import de.koderman.domain.RequestSpeak;
import de.koderman.domain.Room;
import de.koderman.domain.RoomMember;
import de.koderman.domain.RoomRepository;
import de.koderman.domain.State;
import de.koderman.infrastructure.MeetingController;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MeetingControllerPresenceTest {

    @Test
    void joinAndRequestTrackMembersBySessionAndReplaceNames() {
        SimpMessagingTemplate broker = mock(SimpMessagingTemplate.class);
        RoomRepository repository = new RoomRepository();
        MeetingController controller = new MeetingController(broker, repository);
        repository.createRoom("TEST");

        StompHeaderAccessor headerAccessor = mock(StompHeaderAccessor.class);
        when(headerAccessor.getSessionId()).thenReturn("session-a");

        controller.join("TEST", new Join("Anonymous"), headerAccessor);
        controller.request("TEST", new RequestSpeak("Ada"), headerAccessor);
        controller.request("TEST", new RequestSpeak("Ava"), headerAccessor);

        State state = repository.getByCodeOrThrow("TEST").snapshot();

        assertEquals(1, state.members().size());
        assertEquals(new RoomMember("session-a", "Ava", state.members().get(0).joinedAtSec()), state.members().get(0));
        assertEquals(1, state.queue().size());
        assertEquals("Ava", state.queue().get(0).name());
        assertEquals("session-a", state.queue().get(0).id());
    }

    @Test
    void chairSessionRequestAppendsProxyMembersAndWithdrawRemovesRequestedName() {
        SimpMessagingTemplate broker = mock(SimpMessagingTemplate.class);
        RoomRepository repository = new RoomRepository();
        MeetingController controller = new MeetingController(broker, repository);
        repository.createRoom("TEST");

        StompHeaderAccessor headerAccessor = mock(StompHeaderAccessor.class);
        when(headerAccessor.getSessionId()).thenReturn("chair-session");

        controller.join("TEST", new Join("Chair"), headerAccessor);
        controller.request("TEST", new RequestSpeak("Ada"), headerAccessor);
        controller.request("TEST", new RequestSpeak("Ben"), headerAccessor);

        State queuedState = repository.getByCodeOrThrow("TEST").snapshot();

        assertEquals(3, queuedState.members().size());
        assertEquals("Chair", queuedState.members().get(0).name());
        assertEquals("Ada", queuedState.members().get(1).name());
        assertEquals("Ben", queuedState.members().get(2).name());
        assertEquals(2, queuedState.queue().size());

        controller.withdraw("TEST", new de.koderman.domain.Withdraw("Ada"));

        State withdrawnState = repository.getByCodeOrThrow("TEST").snapshot();

        assertEquals(2, withdrawnState.members().size());
        assertEquals("Chair", withdrawnState.members().get(0).name());
        assertEquals("Ben", withdrawnState.members().get(1).name());
        assertEquals(1, withdrawnState.queue().size());
        assertEquals("Ben", withdrawnState.queue().get(0).name());
    }

    @Test
    void disconnectRemovesMemberAndBroadcastsUpdatedState() {
        SimpMessagingTemplate broker = mock(SimpMessagingTemplate.class);
        RoomRepository repository = new RoomRepository();
        MeetingController controller = new MeetingController(broker, repository);
        Room room = repository.createRoom("TEST");

        StompHeaderAccessor headerAccessor = mock(StompHeaderAccessor.class);
        when(headerAccessor.getSessionId()).thenReturn("chair-session");

        controller.join("TEST", new Join("Chair"), headerAccessor);

        SessionDisconnectEvent event = mock(SessionDisconnectEvent.class);
        when(event.getSessionId()).thenReturn("chair-session");

        controller.handleWebSocketDisconnect(event);

        State state = room.snapshot();

        assertTrue(state.members().isEmpty());
        verify(broker).convertAndSend("/topic/room/TEST/state", state);
    }
}