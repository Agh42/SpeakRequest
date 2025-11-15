package de.koderman.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public record StartPoll(
    @NotBlank(message = "Question is required")
    @Size(max = 200, message = "Question must not exceed 200 characters")
    String question,
    @NotBlank(message = "Poll type is required")
    String pollType, // "YES_NO", "GRADIENTS", or "MULTISELECT"
    List<String> options, // For MULTISELECT polls - list of option strings
    Integer votesPerParticipant, // For MULTISELECT polls - currently hardcoded to 1
    Integer votesPerOption // For MULTISELECT polls - currently hardcoded to 1
) {}