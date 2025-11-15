package de.koderman.domain;

public enum ParticipationFormat {
    STRUCTURED_GO_AROUNDS("Structured Go-Arounds", "Everyone contributes in turn, ensuring equal participation and balanced input."),
    PRESENTATIONS_AND_REPORTS("Presentations and Reports", "Individuals or teams share prepared findings or updates with the group."),
    SMALL_GROUPS("Small Groups", "Participants work in subgroups to explore topics or solve problems collaboratively."),
    LISTING_IDEAS("Listing Ideas", "The group rapidly generates and records ideas without immediate evaluation."),
    JIGSAW("Jigsaw", "Each subgroup learns part of a topic and teaches it to others, combining knowledge collaboratively."),
    INDIVIDUAL_WRITING("Individual Writing", "Participants reflect or respond in writing before sharing or discussing."),
    MULTI_TASKING("Multi-Tasking", "Participants engage in parallel activities contributing to a shared goal or outcome."),
    OPEN_DISCUSSION("Open Discussion", "Participants freely exchange views and reactions in an unstructured conversation."),
    FISHBOWLS("Fishbowls", "A small inner group discusses while others observe, then roles switch for reflection and feedback."),
    TRADESHOW("Tradeshow", "Participants display and explain their work or ideas at stations others visit in rotation."),
    SCRAMBLER("Scrambler", "Participants move between tasks, stations, or partners to stimulate diverse perspectives."),
    ROLEPLAYS("Roleplays", "Participants act out scenarios to explore perspectives, behaviors, or problem-solving strategies.");
    
    private final String displayName;
    private final String description;
    
    ParticipationFormat(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
}