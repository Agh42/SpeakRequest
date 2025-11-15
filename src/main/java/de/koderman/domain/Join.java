package de.koderman.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record Join(
    @NotBlank(message = "Name is required")
    @Size(max = 30, message = "Name must not exceed 30 characters")
    @Pattern(regexp = "^[a-zA-Z0-9 '.-]+$", message = "Name can only contain letters, numbers, spaces, dots, hyphens, and apostrophes")
    String name
) {}