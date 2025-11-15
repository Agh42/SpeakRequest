package de.koderman.domain;

public record RoomConfig(String topic, MeetingGoal meetingGoal, ParticipationFormat participationFormat, DecisionRule decisionRule, Deliverable deliverable) {}