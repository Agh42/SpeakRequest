package de.koderman.domain;

import java.util.List;
import java.util.Set;

public record State(List<Participant> queue, Set<Participant> members, Current current, long meetingStartSec, int defaultLimitSec, String roomCode, boolean chairOccupied, PollState pollState, RoomConfig roomConfig) {}