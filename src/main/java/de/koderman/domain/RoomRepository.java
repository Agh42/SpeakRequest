package de.koderman.domain;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RoomRepository {
    @Value("${app.room.max-rooms:2500}")
    private int maxRooms;
    
    private final ConcurrentHashMap<String, Room> roomsByCode = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> sessionToRoomCode = new ConcurrentHashMap<>();
    // TreeMap sorted by creation timestamp for efficient oldest room lookup
    private final TreeMap<Long, Room> roomsByTimestamp = new TreeMap<>();
    private final Object roomCreationLock = new Object();

    public Optional<Room> getByCode(String roomCode) {
        return Optional.ofNullable(roomsByCode.get(roomCode));
    }
    
    public Room getByCodeOrThrow(String roomCode) {
        return Optional.ofNullable(roomsByCode.get(roomCode))
                .orElseThrow(() -> new RoomNotFoundException(roomCode));
    }
    
    public Room createRoom(String roomCode) {
        synchronized (roomCreationLock) {
            // Check if room already exists
            Room existingRoom = roomsByCode.get(roomCode);
            if (existingRoom != null) {
                return existingRoom;
            }
            
            // Before creating a new room, check if we've reached the limit
            if (roomsByCode.size() >= maxRooms) {
                removeOldestRoom();
            }
            
            Room newRoom = new Room(roomCode);
            roomsByCode.put(roomCode, newRoom);
            roomsByTimestamp.put(newRoom.getMeetingStartSec(), newRoom);
            return newRoom;
        }
    }
    
    private void removeOldestRoom() {
        // Must be called within synchronized block
        // Get the first (oldest) entry from the TreeMap
        Map.Entry<Long, Room> oldestEntry = roomsByTimestamp.firstEntry();
        
        if (oldestEntry != null) {
            Room oldestRoom = oldestEntry.getValue();
            String oldestRoomCode = oldestRoom.getRoomCode();
            
            // Remove from both maps
            roomsByCode.remove(oldestRoomCode);
            roomsByTimestamp.remove(oldestEntry.getKey());
            
            // Clean up session tracking for the removed room
            sessionToRoomCode.entrySet().removeIf(entry -> 
                oldestRoomCode.equals(entry.getValue())
            );
        }
    }

    public boolean exists(String roomCode) {
        return roomsByCode.containsKey(roomCode);
    }

    public Optional<Room> getBySessionId(String sessionId) {
        return Optional.ofNullable(sessionToRoomCode.get(sessionId))
                .map(roomsByCode::get);
    }

    public void trackSession(String sessionId, String roomCode) {
        sessionToRoomCode.put(sessionId, roomCode);
    }

    public void untrackSession(String sessionId) {
        sessionToRoomCode.remove(sessionId);
    }
    
    public void destroyRoom(String roomCode) {
        synchronized (roomCreationLock) {
            // Remove room from registry
            Room room = roomsByCode.remove(roomCode);
            
            // Remove from timestamp map
            if (room != null) {
                roomsByTimestamp.remove(room.getMeetingStartSec());
                
                // Remove all session tracking for this room
                sessionToRoomCode.entrySet().removeIf(entry -> roomCode.equals(entry.getValue()));
            }
        }
    }
    
    public List<String> getSessionsForRoom(String roomCode) {
        return sessionToRoomCode.entrySet().stream()
                .filter(entry -> roomCode.equals(entry.getValue()))
                .map(Map.Entry::getKey)
                .toList();
    }
}