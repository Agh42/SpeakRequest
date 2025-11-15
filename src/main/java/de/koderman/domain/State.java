package de.koderman.domain;

import java.util.List;

public record State(List<Participant> queue, Current current, long meetingStartSec, int defaultLimitSec, String roomCode, boolean chairOccupied, PollState pollState, RoomConfig roomConfig) {}