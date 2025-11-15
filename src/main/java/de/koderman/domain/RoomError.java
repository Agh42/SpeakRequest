package de.koderman.domain;

public record RoomError(
    String error,
    String roomCode,
    String action,
    String landingUrl
) {}