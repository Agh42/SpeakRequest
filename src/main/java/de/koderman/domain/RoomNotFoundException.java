package de.koderman.domain;

public class RoomNotFoundException extends RuntimeException {
    private final String roomCode;
    
    public RoomNotFoundException(String roomCode) {
        super("Room not found: " + roomCode);
        this.roomCode = roomCode;
    }
    
    public String getRoomCode() {
        return roomCode;
    }
}