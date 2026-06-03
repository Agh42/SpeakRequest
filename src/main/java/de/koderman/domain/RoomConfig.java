package de.koderman.domain;

import java.util.List;

public record RoomConfig(String topic, MeetingGoal meetingGoal, ParticipationFormat participationFormat, DecisionRule decisionRule, Deliverable deliverable, List<String> agenda) {}