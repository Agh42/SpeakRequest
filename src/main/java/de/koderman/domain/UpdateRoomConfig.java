package de.koderman.domain;

import jakarta.validation.constraints.Size;

public record UpdateRoomConfig(
    @Size(max = 100, message = "Topic must not exceed 100 characters")
    String topic,
    @Size(max = 100, message = "Meeting goal must not exceed 100 characters")
    String meetingGoal,
    @Size(max = 100, message = "Participation format must not exceed 100 characters")
    String participationFormat,
    @Size(max = 100, message = "Decision rule must not exceed 100 characters")
    String decisionRule,
    @Size(max = 100, message = "Deliverable must not exceed 100 characters")
    String deliverable
) {}