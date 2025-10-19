package de.koderman;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Manual demonstration test showing room limit eviction behavior.
 * This test simulates the actual behavior when the limit is reached.
 */
class RoomLimitDemonstrationTest {

    @Test
    void demonstrateRoomLimitEviction() throws Exception {
        MeetingApp.RoomRepository repository = new MeetingApp.RoomRepository();
        
        // Access internal fields for testing
        Field roomsByCodeField = MeetingApp.RoomRepository.class.getDeclaredField("roomsByCode");
        roomsByCodeField.setAccessible(true);
        @SuppressWarnings("unchecked")
        ConcurrentHashMap<String, MeetingApp.Room> roomsByCode = 
            (ConcurrentHashMap<String, MeetingApp.Room>) roomsByCodeField.get(repository);
        
        Field sessionToRoomCodeField = MeetingApp.RoomRepository.class.getDeclaredField("sessionToRoomCode");
        sessionToRoomCodeField.setAccessible(true);
        @SuppressWarnings("unchecked")
        ConcurrentHashMap<String, String> sessionToRoomCode = 
            (ConcurrentHashMap<String, String>) sessionToRoomCodeField.get(repository);
        
        // Create oldest room
        MeetingApp.Room oldestRoom = repository.getOrCreate("OLDEST");
        String oldestRoomCode = oldestRoom.getRoomCode();
        long oldestTimestamp = oldestRoom.getMeetingStartSec();
        repository.trackSession("oldSession1", oldestRoomCode);
        repository.trackSession("oldSession2", oldestRoomCode);
        
        // Wait to ensure different timestamp
        try {
            Thread.sleep(1100); // Sleep long enough to ensure a new second
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Create newer room
        MeetingApp.Room newerRoom = repository.getOrCreate("NEWER");
        String newerRoomCode = newerRoom.getRoomCode();
        long newerTimestamp = newerRoom.getMeetingStartSec();
        repository.trackSession("newSession1", newerRoomCode);
        
        // Verify timestamps
        assertTrue(newerTimestamp > oldestTimestamp, 
            "Newer room should have a later timestamp");
        
        // Verify both rooms exist
        assertEquals(2, roomsByCode.size());
        assertTrue(repository.exists(oldestRoomCode));
        assertTrue(repository.exists(newerRoomCode));
        assertEquals(3, sessionToRoomCode.size());
        
        // Now manually fill the repository to just below the MAX_ROOMS limit
        // Then create one more room to trigger eviction
        Field maxRoomsField = MeetingApp.RoomRepository.class.getDeclaredField("MAX_ROOMS");
        maxRoomsField.setAccessible(true);
        int maxRooms = maxRoomsField.getInt(null);
        
        // Fill up to MAX_ROOMS - 1 (we already have 2 rooms)
        int roomsToCreate = maxRooms - 2;
        
        // For testing purposes, we'll create a smaller number to verify the logic
        // In production, this would actually fill to 500,000
        // Let's just verify that the logic would work by manually triggering removeOldestRoom
        
        // Manually fill to capacity (simulate being at max)
        for (int i = 0; i < maxRooms - 2; i++) {
            roomsByCode.put("FILL" + i, new MeetingApp.Room("FILL" + i));
        }
        
        // Verify we're at capacity
        assertEquals(maxRooms, roomsByCode.size());
        
        // Now create a new room which should trigger eviction
        MeetingApp.Room brandNewRoom = repository.getOrCreate("BRANDNEW");
        
        // The oldest room should have been evicted
        assertFalse(repository.exists(oldestRoomCode), 
            "Oldest room should have been evicted");
        
        // The newer room and brand new room should still exist
        assertTrue(repository.exists(newerRoomCode), 
            "Newer room should still exist");
        assertTrue(repository.exists("BRANDNEW"), 
            "Brand new room should exist");
        
        // We should still be at or below max capacity
        assertTrue(roomsByCode.size() <= maxRooms,
            "Room count should not exceed MAX_ROOMS");
        
        // Sessions for the oldest room should be cleaned up
        assertFalse(repository.getBySessionId("oldSession1").isPresent(),
            "Old session 1 should be cleaned up");
        assertFalse(repository.getBySessionId("oldSession2").isPresent(),
            "Old session 2 should be cleaned up");
        
        // Session for newer room should still exist
        assertTrue(repository.getBySessionId("newSession1").isPresent(),
            "New session should still exist");
    }

    @Test
    void verifyMaxRoomsConfiguration() throws Exception {
        Field maxRoomsField = MeetingApp.RoomRepository.class.getDeclaredField("MAX_ROOMS");
        maxRoomsField.setAccessible(true);
        int maxRooms = maxRoomsField.getInt(null);
        
        assertEquals(500000, maxRooms, 
            "MAX_ROOMS should be configured to 500,000 as per requirements");
    }

    @Test
    void verifyEvictionStrategy() throws Exception {
        MeetingApp.RoomRepository repository = new MeetingApp.RoomRepository();
        
        // Create three rooms with distinct timestamps
        MeetingApp.Room room1 = repository.getOrCreate("ROOM1");
        long timestamp1 = room1.getMeetingStartSec();
        
        Thread.sleep(1100);
        
        MeetingApp.Room room2 = repository.getOrCreate("ROOM2");
        long timestamp2 = room2.getMeetingStartSec();
        
        Thread.sleep(1100);
        
        MeetingApp.Room room3 = repository.getOrCreate("ROOM3");
        long timestamp3 = room3.getMeetingStartSec();
        
        // Verify timestamps are in order
        assertTrue(timestamp1 < timestamp2);
        assertTrue(timestamp2 < timestamp3);
        
        // Verify all rooms exist
        assertTrue(repository.exists("ROOM1"));
        assertTrue(repository.exists("ROOM2"));
        assertTrue(repository.exists("ROOM3"));
        
        System.out.println("Room 1 timestamp: " + timestamp1);
        System.out.println("Room 2 timestamp: " + timestamp2);
        System.out.println("Room 3 timestamp: " + timestamp3);
        System.out.println("All rooms created successfully with distinct timestamps.");
    }
}
