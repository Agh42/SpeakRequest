package de.koderman;

import de.koderman.domain.Participant;
import de.koderman.domain.Room;
import de.koderman.domain.RoomMember;
import de.koderman.domain.State;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RoomMemberPresenceTest {

    @Test
    void snapshotIncludesMembersInSessionOrder() {
        Room room = new Room("TEST");

        room.upsertMember("session-a", "Ada");
        room.upsertMember("session-b", "Ben");

        State state = room.snapshot();

        assertEquals(2, state.members().size());
        assertEquals("session-a", state.members().get(0).sessionId());
        assertEquals("Ada", state.members().get(0).name());
        assertEquals("session-b", state.members().get(1).sessionId());
        assertEquals("Ben", state.members().get(1).name());
    }

    @Test
    void upsertMemberReplacesNameForExistingSession() {
        Room room = new Room("TEST");

        room.upsertMember("session-a", "Ada");
        room.upsertMember("session-a", "Ava");

        State state = room.snapshot();

        assertEquals(1, state.members().size());
        assertEquals(new RoomMember("session-a", "Ava", state.members().get(0).joinedAtSec()), state.members().get(0));
    }

    @Test
    void removeMemberDeletesPresenceFromSnapshot() {
        Room room = new Room("TEST");

        room.upsertMember("session-a", "Ada");
        room.upsertMember("session-b", "Ben");
        room.removeMember("session-a");

        State state = room.snapshot();

        assertEquals(1, state.members().size());
        assertEquals("session-b", state.members().get(0).sessionId());
        assertEquals("Ben", state.members().get(0).name());
    }

    @Test
    void proxyMembersAppendWithoutReplacingExistingSessionPresence() {
        Room room = new Room("TEST");

        room.upsertMember("chair-session", "Chair");
        room.addProxyMember("chair-session", "Ada");
        room.addProxyMember("chair-session", "Ben");

        State state = room.snapshot();

        assertEquals(3, state.members().size());
        assertEquals("Chair", state.members().get(0).name());
        assertEquals("Ada", state.members().get(1).name());
        assertEquals("Ben", state.members().get(2).name());
    }

    @Test
    void removeMemberByNameDeletesMatchingProxyPresence() {
        Room room = new Room("TEST");

        room.upsertMember("chair-session", "Chair");
        room.addProxyMember("chair-session", "Ada");
        room.addProxyMember("chair-session", "Ben");
        room.removeMemberByName("Ada");

        State state = room.snapshot();

        assertEquals(2, state.members().size());
        assertEquals("Chair", state.members().get(0).name());
        assertEquals("Ben", state.members().get(1).name());
    }

    @Test
    void queueEntryIsUpdatedWhenSameSessionRequestsAgain() {
        Room room = new Room("TEST");

        room.addParticipantToQueue(new Participant("session-a", "Ada", 1L));
        room.addParticipantToQueue(new Participant("session-a", "Ava", 2L));

        State state = room.snapshot();

        assertEquals(1, state.queue().size());
        assertEquals("Ava", state.queue().get(0).name());
        assertEquals("session-a", state.queue().get(0).id());
    }
}