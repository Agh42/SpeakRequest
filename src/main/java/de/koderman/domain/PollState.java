package de.koderman.domain;

import java.util.List;
import java.util.Map;

public record PollState(
    String question,
    String pollType,
    String status, // "ACTIVE", "ENDED", "CLOSED", null
    Map<String, Integer> results, // vote option -> count
    Integer totalVotes,
    PollResults lastResults, // Results of the last ended poll
    List<String> options, // For MULTISELECT polls - list of option labels
    Integer votesPerParticipant // For MULTISELECT_MULTIPLE polls - number of votes each participant can cast
) {}