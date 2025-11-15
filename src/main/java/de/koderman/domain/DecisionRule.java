package de.koderman.domain;

public enum DecisionRule {
    UNANIMITY("Unanimity", "All participants must fully agree before a decision is made."),
    GRADIENTS_OF_AGREEMENT("Gradients of Agreement", "Participants express varying levels of support, revealing nuanced consensus rather than a simple yes/no."),
    DOT_VOTING("Dot Voting", "Each person allocates a limited number of votes (dots) to indicate preferences or priorities visually."),
    SUPERMAJORITY("Supermajority", "A decision requires a higher-than-simple majority, such as two-thirds or three-quarters agreement."),
    MAJORITY("Majority", "The option with more than half of the votes wins."),
    PLURALITY("Plurality", "The option with the most votes wins, even if it lacks a majority."),
    CONSENT("Consent", "A proposal moves forward unless there is a reasoned and paramount objection."),
    PERSON_IN_CHARGE("Person in Charge", "A designated leader makes the final decision after input from others."),
    COMMISSION("Commission", "A smaller group or committee is empowered to deliberate and decide on behalf of the whole."),
    FLIP_A_COIN("Flip a Coin", "A neutral random choice is used to decide between equally acceptable or deadlocked options.");
    
    private final String displayName;
    private final String description;
    
    DecisionRule(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
}