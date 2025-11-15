package de.koderman.domain;

public class ChairAccessException extends RuntimeException {
    private final String roomCode;
    private final String sessionId;
    
    public ChairAccessException(String message, String roomCode, String sessionId) {
        super(message);
        this.roomCode = roomCode;
        this.sessionId = sessionId;
    }
    
    public String getRoomCode() {
        return roomCode;
    }
    
    public String getSessionId() {
        return sessionId;
    }
}