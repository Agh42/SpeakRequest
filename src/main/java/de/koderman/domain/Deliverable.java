package de.koderman.domain;

public enum Deliverable {
    DEFINE_PROBLEM("Define a problem", "Clearly articulate the issue or challenge that needs to be addressed"),
    CREATE_MILESTONE_MAP("Create a milestone map", "Identify key checkpoints and timeline for project phases"),
    ANALYZE_PROBLEM("Analyze a problem", "Examine causes, effects, and context of the issue in depth"),
    CREATE_WORK_BREAKDOWN("Create a work breakdown structure", "Break down the project into manageable tasks and subtasks"),
    IDENTIFY_ROOT_CAUSES("Identify root causes", "Determine the fundamental reasons behind the problem"),
    CONDUCT_RESOURCE_ANALYSIS("Conduct a resource analysis", "Assess available resources including time, budget, and personnel"),
    IDENTIFY_PATTERNS("Identify underlying patterns", "Recognize recurring themes or trends in the data or situation"),
    CONDUCT_RISK_ASSESSMENT("Conduct a risk assessment", "Evaluate potential risks and their impact on the project"),
    SORT_IDEAS_INTO_THEMES("Sort a list of ideas into themes", "Organize and categorize ideas into coherent groups"),
    DEFINE_SELECTION_CRITERIA("Define selection criteria", "Establish the standards for evaluating and choosing options"),
    REARRANGE_BY_PRIORITY("Rearrange a list of items by priority", "Order items based on importance, urgency, or value"),
    EVALUATE_OPTIONS("Evaluate options", "Assess and compare different alternatives against criteria"),
    DRAW_FLOWCHART("Draw a flowchart", "Create a visual diagram showing process steps and decision points"),
    IDENTIFY_SUCCESS_FACTORS("Identify critical success factors", "Determine the key elements necessary for success"),
    IDENTIFY_CORE_VALUES("Identify core values", "Define the fundamental principles guiding decisions and actions"),
    EDIT_STATEMENT("Edit and/or wordsmith a statement", "Refine and improve the clarity and impact of written text");
    
    private final String displayName;
    private final String description;
    
    Deliverable(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
}