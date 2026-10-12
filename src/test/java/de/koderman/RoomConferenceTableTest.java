package de.koderman;

import de.koderman.domain.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RoomConferenceTableTest {
    private Room roomWithPeople(int count) {
        Room room = new Room("TABLE");
        room.upsertMember("chair", "Facilitator");
        room.assumeChairRole("chair");
        for (int i = 1; i < count; i++) room.upsertMember("p" + i, "Person " + i);
        return room;
    }

    private void speak(Room room, String id, String name) {
        room.nextParticipant("chair"); // End the previous turn before requesting a repeat turn.
        room.addParticipantToQueue(new Participant(id, name, 1));
        room.nextParticipant("chair");
    }

    private List<String> ids(Room room) {
        return room.snapshot().conferenceTable().seats().stream()
                .map(s -> s.member() == null ? "vacant" : s.member().id()).toList();
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4, 5, 8, 12, 19, 20, 21, 40})
    void capacityAndOverflowCountEveryPresentPersonOnce(int people) {
        Room room = roomWithPeople(people);
        var state = room.snapshot();
        var table = state.conferenceTable();
        assertEquals(20, table.capacity());
        assertEquals(Math.min(20, people), table.seats().size());
        assertEquals(Math.max(0, people - 20), table.overflowCount());
        assertEquals(table.seats().size(), ids(room).stream().distinct().count());
        assertEquals("chair", state.chairSessionId());
        assertEquals("chair", table.seats().getFirst().member().id());
        assertTrue(table.seats().getFirst().chair());
        assertEquals(table, room.snapshot().conferenceTable());
    }

    @Test
    void overflowSpeakerReplacesNeverSpokenPersonInExactlyOneSlot() {
        Room room = roomWithPeople(22);
        speak(room, "p1", "Person 1");
        List<String> before = ids(room);
        speak(room, "p20", "Person 20");
        List<String> after = ids(room);
        for (int i = 0; i < before.size(); i++) {
            assertEquals(i == 2 ? "p20" : before.get(i), after.get(i));
        }
        assertEquals(2, room.snapshot().conferenceTable().overflowCount());
        assertEquals("p20", room.snapshot().conferenceTable().seats().stream()
                .filter(ConferenceTable.Seat::speaking).findFirst().orElseThrow().member().id());
    }

    @Test
    void oldestTurnIsEvictedAfterEveryVisiblePersonHasSpoken() {
        Room room = roomWithPeople(21);
        for (int i = 1; i <= 19; i++) speak(room, "p" + i, "Person " + i);
        speak(room, "p1", "Person 1"); // A repeat turn makes p1 recent again.
        var before = room.snapshot().conferenceTable();
        room.pauseTimer("chair");
        room.startTimer("chair");
        room.resetTimer("chair");
        room.updateLimit("chair", 90);
        assertEquals(before, room.snapshot().conferenceTable());
        speak(room, "p20", "Person 20");
        assertTrue(ids(room).contains("p1"));
        assertFalse(ids(room).contains("p2"));
        assertEquals("p20", ids(room).get(2));
        assertEquals(21, room.snapshot().conferenceTable().seats().get(2).lastSpokeSequence());
    }

    @Test
    void disconnectRefillsTheSameSlotAndOverflowCountsPresentPeople() {
        Room room = roomWithPeople(22);
        List<String> before = ids(room);
        room.removeMember("p5");
        List<String> after = ids(room);
        for (int i = 0; i < before.size(); i++) assertEquals(i == 5 ? "p20" : before.get(i), after.get(i));
        assertEquals(1, room.snapshot().conferenceTable().overflowCount());
    }

    @Test
    void currentSpeakerStaysVisibleAfterDisconnectUntilTurnEnds() {
        Room room = roomWithPeople(21);
        speak(room, "p1", "Person 1");
        room.removeMember("p1");
        assertTrue(ids(room).contains("p1"));
        assertEquals(1, room.snapshot().conferenceTable().overflowCount());
        room.nextParticipant("chair");
        assertFalse(ids(room).contains("p1"));
        assertTrue(ids(room).contains("p20"));
        assertEquals(0, room.snapshot().conferenceTable().overflowCount());
    }

    @Test
    void vacantChairAndChairHandoverKeepExactlyOneReservedSeat() {
        Room empty = new Room("EMPTY");
        assertNull(empty.snapshot().chairSessionId());
        assertNull(empty.snapshot().conferenceTable().seats().getFirst().member());
        assertEquals(0, empty.snapshot().conferenceTable().overflowCount());
        for (int i = 0; i < 20; i++) empty.upsertMember("p" + i, "Person " + i);
        assertEquals(20, empty.snapshot().conferenceTable().seats().size());
        assertEquals(1, empty.snapshot().conferenceTable().overflowCount());

        Room room = roomWithPeople(21);
        speak(room, "p1", "Person 1");
        room.releaseChairRole("chair");
        room.assumeChairRole("p20");
        var state = room.snapshot();
        assertEquals("p20", state.chairSessionId());
        assertEquals("p20", state.conferenceTable().seats().getFirst().member().id());
        assertEquals(1, state.conferenceTable().seats().stream().filter(ConferenceTable.Seat::chair).count());
        assertTrue(ids(room).contains("p1"));
    }

    @Test
    void formerChairKeepsRecentSpeakerPriorityWhenTheNewChairWasInOverflow() {
        Room room = roomWithPeople(21);
        speak(room, "chair", "Facilitator");
        speak(room, "p1", "Person 1");
        room.releaseChairRole("chair");
        room.assumeChairRole("p20");
        assertEquals("p20", ids(room).getFirst());
        assertTrue(ids(room).contains("chair"));
        assertTrue(ids(room).contains("p1"));
        assertFalse(ids(room).contains("p2"));
    }

    @Test
    void duplicateNamesUseSessionIdentityAndRenamesKeepTheirSeatAndHistory() {
        Room room = roomWithPeople(1);
        room.upsertMember("a", "Alex");
        room.upsertMember("b", "Alex");
        speak(room, "b", "Alex");
        var table = room.snapshot().conferenceTable();
        assertFalse(table.seats().get(1).speaking());
        assertTrue(table.seats().get(2).speaking());
        room.upsertMember("b", "Alexandra");
        var renamed = room.snapshot().conferenceTable().seats().get(2);
        assertEquals("b", renamed.member().id());
        assertEquals("Alexandra", renamed.member().name());
        assertEquals(1, renamed.lastSpokeSequence());
        assertTrue(renamed.speaking());
    }

    @Test
    void proxyMembersHaveStableSeparateIdentitiesAcrossTurns() {
        Room room = roomWithPeople(1);
        room.addProxyMember("chair", "Ada");
        room.addProxyMember("chair", "Ben");
        List<String> before = ids(room);
        assertEquals(3, before.stream().distinct().count());
        speak(room, "chair:proxy:request-1", "Ada");
        assertTrue(room.snapshot().conferenceTable().seats().get(1).speaking());
        assertFalse(room.snapshot().conferenceTable().seats().getFirst().speaking());
        speak(room, "chair:proxy:request-2", "Ben");
        speak(room, "chair:proxy:request-3", "Ada");
        assertEquals(before, ids(room));
        assertEquals(3, room.snapshot().conferenceTable().seats().get(1).lastSpokeSequence());
        room.removeMemberByName("Ada");
        room.nextParticipant("chair");
        assertEquals(2, ids(room).size());
    }
}
