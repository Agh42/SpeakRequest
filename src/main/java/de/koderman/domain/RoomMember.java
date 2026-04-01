package de.koderman.domain;

public record RoomMember(String sessionId, String name, long joinedAtSec) {}