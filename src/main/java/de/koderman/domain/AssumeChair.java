package de.koderman.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AssumeChair(
    @NotBlank(message = "Participant name is required")
    @Size(max = 30, message = "Participant name must not exceed 30 characters")
    @Pattern(regexp = "^[a-zA-Z0-9 '.-]+$", message = "Participant name can only contain letters, numbers, spaces, dots, hyphens, and apostrophes")
    String participantName,
    String requestId
) {}