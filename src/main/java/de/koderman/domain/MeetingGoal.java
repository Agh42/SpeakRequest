package de.koderman.domain;

public enum MeetingGoal {
    SHARE_INFORMATION("Share Information", "Ensure everyone has the same facts, updates, or context."),
    ADVANCE_THINKING("Advance the Thinking", "Develop ideas further through discussion, analysis, and reflection."),
    OBTAIN_INPUT("Obtain Input", "Gather perspectives, feedback, or expertise from participants."),
    MAKE_DECISIONS("Make Decisions", "Reach agreement or choose a course of action collaboratively."),
    IMPROVE_COMMUNICATION("Improve Communication", "Strengthen clarity, understanding, and mutual trust among participants."),
    BUILD_CAPACITY("Build Capacity", "Develop participants' skills, knowledge, or confidence to act effectively."),
    BUILD_COMMUNITY("Build Community", "Foster relationships, connection, and shared purpose within the group.");
    
    private final String displayName;
    private final String description;
    
    MeetingGoal(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
}