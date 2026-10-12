package de.koderman.domain;

public record RoomMember(String sessionId, String name, long joinedAtSec, String id, boolean proxy) {
    public RoomMember(String sessionId, String name, long joinedAtSec) {
        this(sessionId, name, joinedAtSec, sessionId, false);
    }
}
