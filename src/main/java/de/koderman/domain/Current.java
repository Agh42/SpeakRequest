package de.koderman.domain;

public record Current(Participant entry, long startedAtSec, int elapsedMs, boolean running, int limitSec) {}