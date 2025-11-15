package de.koderman.domain;

import jakarta.validation.constraints.NotBlank;

public record CastVote(
    @NotBlank(message = "Vote is required")
    String vote // "YES" or "NO"
) {}