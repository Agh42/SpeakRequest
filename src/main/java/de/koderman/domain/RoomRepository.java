package de.koderman.domain;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class RoomRepository {
    @Value("${app.room.max-rooms:2500}")
    private int maxRooms = 100; // Default for manual instantiation in tests
    
    private final ConcurrentHashMap<String, Room> roomsByCode = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> sessionToRoomCode = new ConcurrentHashMap<>();
    // TreeMap sorted by creation timestamp for efficient oldest room lookup
    private final TreeMap<Long, Room> roomsByTimestamp = new TreeMap<>();
    private final Object roomCreationLock = new Object();

    @jakarta.annotation.PostConstruct
    public void logConfiguredLimit() {
        log.info("RoomRepository initialized with maxRooms: {}", maxRooms);
    }

    public Optional<Room> getByCode(String roomCode) {
        return Optional.ofNullable(roomsByCode.get(roomCode));
    }
    
    public Room getByCodeOrThrow(String roomCode) {
        Room room = roomsByCode.get(roomCode);
        if (room == null) {
            log.warn("Room not found: {}", roomCode);
            throw new RoomNotFoundException(roomCode);
        }
        return room;
    }
    
    public Room createRoom(String roomCode) {
        synchronized (roomCreationLock) {
            // Check if room already exists
            Room existingRoom = roomsByCode.get(roomCode);
            if (existingRoom != null) {
                log.info("Room already exists: {}", roomCode);
                return existingRoom;
            }
            
            // Before creating a new room, check if we've reached the limit
            if (roomsByCode.size() >= maxRooms) {
                log.warn("Room limit reached ({}), removing oldest room", maxRooms);
                removeOldestRoom();
            }
            
            Room newRoom = new Room(roomCode);
            roomsByCode.put(roomCode, newRoom);
            roomsByTimestamp.put(newRoom.getMeetingStartSec(), newRoom);
            log.info("Created room: {} (total: {})", roomCode, roomsByCode.size());
            
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
            Date creationTime = new Date(oldestEntry.getKey());
            List<String> activeSessions = getSessionsForRoom(oldestRoomCode);
            
            log.warn("REMOVING OLDEST ROOM: {} created at {} with {} active sessions", 
                oldestRoomCode, creationTime, activeSessions.size());
            
            if (!activeSessions.isEmpty()) {
                log.error("WARNING: Removing room {} with {} ACTIVE sessions! Sessions: {}", 
                    oldestRoomCode, activeSessions.size(), activeSessions);
            }
            
            // Count sessions to be cleaned up
            long sessionsToCleanup = sessionToRoomCode.entrySet().stream()
                .filter(entry -> oldestRoomCode.equals(entry.getValue()))
                .count();
            
            // Remove from both maps
            roomsByCode.remove(oldestRoomCode);
            roomsByTimestamp.remove(oldestEntry.getKey());
            
            // Clean up session tracking for the removed room
            sessionToRoomCode.entrySet().removeIf(entry -> 
                oldestRoomCode.equals(entry.getValue())
            );
            
            log.warn("Removed oldest room: {}, cleaned up {} session mappings, remaining rooms: {}", 
                oldestRoomCode, sessionsToCleanup, roomsByCode.size());
        } else {
            log.warn("Attempted to remove oldest room but no rooms exist in timestamp map");
        }
    }

    public boolean exists(String roomCode) {
        return roomsByCode.containsKey(roomCode);
    }

    public Optional<Room> getBySessionId(String sessionId) {
        String roomCode = sessionToRoomCode.get(sessionId);
        if (roomCode != null) {
            Room room = roomsByCode.get(roomCode);
            if (room == null) {
                log.warn("Orphaned session mapping: {} -> {}", sessionId, roomCode);
                sessionToRoomCode.remove(sessionId);
            }
            return Optional.ofNullable(room);
        }
        return Optional.empty();
    }

    public void trackSession(String sessionId, String roomCode) {
        String previousRoomCode = sessionToRoomCode.put(sessionId, roomCode);
        if (previousRoomCode != null && !previousRoomCode.equals(roomCode)) {
            log.warn("Session remapped: {} from {} to {}", sessionId, previousRoomCode, roomCode);
        }
        if (!roomsByCode.containsKey(roomCode)) {
            log.error("Tracking session for non-existent room: {}", roomCode);
        }
    }

    public void untrackSession(String sessionId) {
        sessionToRoomCode.remove(sessionId);
    }
    
    public void destroyRoom(String roomCode) {
        synchronized (roomCreationLock) {
            Room room = roomsByCode.remove(roomCode);
            if (room != null) {
                List<String> activeSessions = getSessionsForRoom(roomCode);
                if (!activeSessions.isEmpty()) {
                    log.warn("Destroying room {} with {} active sessions", roomCode, activeSessions.size());
                }
                roomsByTimestamp.remove(room.getMeetingStartSec());
                sessionToRoomCode.entrySet().removeIf(entry -> roomCode.equals(entry.getValue()));
                log.info("Destroyed room: {} (remaining: {})", roomCode, roomsByCode.size());
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