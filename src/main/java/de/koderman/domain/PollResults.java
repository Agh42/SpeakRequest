package de.koderman.domain;

import java.util.List;
import java.util.Map;

public record PollResults(
    String question,
    String pollType,
    Map<String, Integer> results,
    Integer totalVotes,
    List<String> options // For MULTISELECT polls - list of option labels
) {}