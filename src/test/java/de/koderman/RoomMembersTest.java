package de.koderman;

import de.koderman.domain.Participant;
import de.koderman.domain.Room;
import de.koderman.domain.State;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RoomMembersTest {

    @Test
    void testMembersSetIsPopulatedWhenParticipantJoins() {
        Room room = new Room("TEST");
        Participant participant1 = new Participant("id1", "Alice", System.currentTimeMillis() / 1000);
        Participant participant2 = new Participant("id2", "Bob", System.currentTimeMillis() / 1000);
        
        room.addParticipantToQueue(participant1);
        room.addParticipantToQueue(participant2);
        
        State state = room.snapshot();
        
        assertNotNull(state.members());
        assertEquals(2, state.members().size());
        assertTrue(state.members().contains(participant1));
        assertTrue(state.members().contains(participant2));
    }
    
    @Test
    void testMembersSetPersistsEvenAfterLeavingQueue() {
        Room room = new Room("TEST");
        Participant participant = new Participant("id1", "Alice", System.currentTimeMillis() / 1000);
        
        room.addParticipantToQueue(participant);
        
        State stateBefore = room.snapshot();
        assertEquals(1, stateBefore.members().size());
        assertEquals(1, stateBefore.queue().size());
        
        room.withdrawParticipant("Alice");
        
        State stateAfter = room.snapshot();
        assertEquals(1, stateAfter.members().size());
        assertEquals(0, stateAfter.queue().size());
        assertTrue(stateAfter.members().contains(participant));
    }
}
