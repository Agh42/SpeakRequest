package de.koderman.domain;

import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class Room {
    private final String roomCode;
    private final List<Participant> queue = new ArrayList<>();
    private Current current = null;
    private final long meetingStartSec = Instant.now().getEpochSecond();
    private int defaultLimitSec = 180; // per-speaker
    private final ReentrantLock lock = new ReentrantLock();
    private String chairSessionId = null; // Track chair WebSocket session

    // Room configuration fields
    private String topic = null;
    private MeetingGoal meetingGoal = null;
    private ParticipationFormat participationFormat = null;
    private DecisionRule decisionRule = null;
    private Deliverable deliverable = null;

    // Polling state
    private String pollQuestion = null;
    private String pollType = null;
    private String pollStatus = null; // "ACTIVE", "ENDED", "CLOSED", null
    private final Map<String, Integer> pollResults = new HashMap<>();
    private final Map<String, String> sessionVotes = new HashMap<>(); // Track each session's vote (allows vote changes)
                                                                      // - for single selection
    private final Map<String, Set<String>> sessionMultiVotes = new HashMap<>(); // Track each session's votes (allows
                                                                                // vote changes) - for multiple
                                                                                // selection
    private PollResults lastPollResults = null;
    private List<String> pollOptions = null; // For MULTISELECT polls - list of option labels
    private Integer votesPerParticipant = 1; // For MULTISELECT_MULTIPLE polls - number of votes each participant can
                                             // cast

    public Room(String roomCode) {
        this.roomCode = roomCode;
    }

    public String getRoomCode() {
        return roomCode;
    }

    public long getMeetingStartSec() {
        return meetingStartSec;
    }

    public State snapshot() {
        lock.lock();
        try {
            PollState pollState = null;
            if (pollQuestion != null && ("ACTIVE".equals(pollStatus) || "ENDED".equals(pollStatus))) {
                // Poll is active or ended (showing results in overlay)
                int totalVotes = pollResults.values().stream().mapToInt(Integer::intValue).sum();
                pollState = new PollState(
                        pollQuestion,
                        pollType,
                        pollStatus,
                        new HashMap<>(pollResults),
                        totalVotes,
                        lastPollResults,
                        pollOptions != null ? List.copyOf(pollOptions) : null,
                        votesPerParticipant);
            } else if ("CLOSED".equals(pollStatus) && lastPollResults != null) {
                // Poll is closed, show only last results
                pollState = new PollState(null, null, "CLOSED", Map.of(), 0, lastPollResults, null, null);
            } else if (lastPollResults != null) {
                // No active poll, but we have last results
                pollState = new PollState(null, null, null, Map.of(), 0, lastPollResults, null, null);
            }

            RoomConfig roomConfig = new RoomConfig(topic, meetingGoal, participationFormat, decisionRule, deliverable);

            return new State(List.copyOf(queue), current, meetingStartSec, defaultLimitSec, roomCode,
                    chairSessionId != null, pollState, roomConfig);
        } finally {
            lock.unlock();
        }
    }

    public boolean isChairSession(String sessionId) {
        lock.lock();
        try {
            boolean isChair = Optional.ofNullable(sessionId)
                    .map(sid -> sid.equals(chairSessionId))
                    .orElse(false);
            log.debug("Room[{}] isChairSession check: sessionId={}, currentChairId={}, result={}", 
                     roomCode, sessionId, chairSessionId, isChair);
            return isChair;
        } finally {
            lock.unlock();
        }
    }

    public boolean hasChair() {
        lock.lock();
        try {
            boolean hasChair = chairSessionId != null;
            log.debug("Room[{}] hasChair check: chairSessionId={}, result={}", 
                     roomCode, chairSessionId, hasChair);
            return hasChair;
        } finally {
            lock.unlock();
        }
    }

    public void assumeChairRole(String sessionId) {
        lock.lock();
        try {
            log.info("Room[{}] assumeChairRole attempt: requestingSessionId={}, currentChairId={}", 
                     roomCode, sessionId, chairSessionId);
            
            if (isChairSession(sessionId)) {
                log.info("Room[{}] assumeChairRole: Session {} is already the chair, no-op", 
                         roomCode, sessionId);
                return; // Already the chair
            }
            
            if (chairSessionId == null) {
                chairSessionId = sessionId;
                log.info("Room[{}] assumeChairRole: Successfully assigned chair role to session {}", 
                         roomCode, sessionId);
            } else {
                log.warn("Room[{}] assumeChairRole: Chair role already occupied by session {}, rejecting request from {}", 
                         roomCode, chairSessionId, sessionId);
                throw new ChairAccessException("Chair role is already occupied", this.roomCode, sessionId);
            }
        } finally {
            lock.unlock();
        }
    }

    public void releaseChairRole(String sessionId) {
        lock.lock();
        try {
            log.info("Room[{}] releaseChairRole attempt: requestingSessionId={}, currentChairId={}", 
                     roomCode, sessionId, chairSessionId);
            
            Optional.ofNullable(sessionId)
                    .filter(sid -> sid.equals(chairSessionId))
                    .ifPresentOrElse(
                        sid -> {
                            chairSessionId = null;
                            log.info("Room[{}] releaseChairRole: Successfully released chair role from session {}", 
                                     roomCode, sessionId);
                        },
                        () -> log.debug("Room[{}] releaseChairRole: Session {} is not the chair or null, no action taken", 
                                       roomCode, sessionId)
                    );
        } finally {
            lock.unlock();
        }
    }

    private int findIndexByNameUnsafe(String name) {
        for (int i = 0; i < queue.size(); i++) {
            if (queue.get(i).name().equalsIgnoreCase(name))
                return i;
        }
        return -1;
    }

    private void requireChairAccess(String sessionId) {
        if (!isChairSession(sessionId)) {
            log.error("Room[{}] requireChairAccess: Access denied for session {} (current chair: {})", 
                     roomCode, sessionId, chairSessionId);
            throw new ChairAccessException("Chair access required for this operation", this.roomCode, sessionId);
        }
        log.debug("Room[{}] requireChairAccess: Access granted for chair session {}", 
                 roomCode, sessionId);
    }

    // DDD methods - encapsulate internal state management

    public void nextParticipant(String sessionId) {
        lock.lock();
        try {
            requireChairAccess(sessionId);
            current = null;
            if (!queue.isEmpty()) {
                Participant next = queue.remove(0);
                current = new Current(next, Instant.now().getEpochSecond(), 0, true, defaultLimitSec);
                log.info("Room[{}] nextParticipant: Set {} as current speaker (by chair session {})", 
                         roomCode, next.name(), sessionId);
            } else {
                log.info("Room[{}] nextParticipant: No participants in queue (by chair session {})", 
                         roomCode, sessionId);
            }
        } finally {
            lock.unlock();
        }
    }

    public void withdrawParticipant(String name) {
        lock.lock();
        try {
            int idx = findIndexByNameUnsafe(name);
            if (idx >= 0) {
                queue.remove(idx);
                log.info("Room[{}] withdrawParticipant: Removed {} from queue at position {}", 
                         roomCode, name, idx);
            } else {
                log.debug("Room[{}] withdrawParticipant: Participant {} not found in queue", 
                         roomCode, name);
            }
        } finally {
            lock.unlock();
        }
    }

    public void startTimer(String sessionId) {
        lock.lock();
        try {
            requireChairAccess(sessionId);
            if (current == null)
                return;
            if (!current.running()) {
                long nowSec = Instant.now().getEpochSecond();
                current = new Current(current.entry(), nowSec, current.elapsedMs(), true, current.limitSec());
            }
        } finally {
            lock.unlock();
        }
    }

    public void pauseTimer(String sessionId) {
        lock.lock();
        try {
            requireChairAccess(sessionId);
            if (current == null)
                return;
            if (current.running()) {
                long nowSec = Instant.now().getEpochSecond();
                int addMs = (int) ((nowSec - current.startedAtSec()) * 1000);
                current = new Current(current.entry(), current.startedAtSec(),
                        current.elapsedMs() + addMs, false, current.limitSec());
            }
        } finally {
            lock.unlock();
        }
    }

    public void resetTimer(String sessionId) {
        lock.lock();
        try {
            requireChairAccess(sessionId);
            if (current == null)
                return;
            long nowSec = Instant.now().getEpochSecond();
            current = new Current(current.entry(), nowSec, 0, true, current.limitSec());
        } finally {
            lock.unlock();
        }
    }

    public void updateLimit(String sessionId, int seconds) {
        lock.lock();
        try {
            requireChairAccess(sessionId);
            defaultLimitSec = seconds;
            if (current != null) {
                current = new Current(current.entry(), current.startedAtSec(),
                        current.elapsedMs(), current.running(), seconds);
            }
        } finally {
            lock.unlock();
        }
    }

    public void addParticipantToQueue(Participant participant) {
        lock.lock();
        try {
            if (current != null && current.entry().name().equalsIgnoreCase(participant.name())) {
                log.debug("Room[{}] addParticipantToQueue: {} is already the current speaker, not adding to queue", 
                         roomCode, participant.name());
                return;
            }
            if (findIndexByNameUnsafe(participant.name()) >= 0) {
                log.debug("Room[{}] addParticipantToQueue: {} is already in queue, not adding duplicate", 
                         roomCode, participant.name());
                return;
            }
            queue.add(participant);
            log.info("Room[{}] addParticipantToQueue: Added {} to queue (queue size now: {})", 
                     roomCode, participant.name(), queue.size());
        } finally {
            lock.unlock();
        }
    }

    // Polling methods
    public void startPoll(String sessionId, String question, String pollType, List<String> options,
            Integer votesPerParticipant) {
        lock.lock();
        try {
            requireChairAccess(sessionId);
            this.pollQuestion = question;
            this.pollType = pollType;
            this.pollStatus = "ACTIVE";
            this.pollResults.clear();
            this.sessionVotes.clear();
            this.sessionMultiVotes.clear();
            this.pollOptions = null;
            this.votesPerParticipant = votesPerParticipant != null ? votesPerParticipant : 1;

            // Initialize results based on poll type
            if ("YES_NO".equals(pollType)) {
                this.pollResults.put("YES", 0);
                this.pollResults.put("NO", 0);
            } else if ("GRADIENTS".equals(pollType)) {
                // Initialize 8 options for Gradients of Agreement
                this.pollResults.put("OPT_1", 0);
                this.pollResults.put("OPT_2", 0);
                this.pollResults.put("OPT_3", 0);
                this.pollResults.put("OPT_4", 0);
                this.pollResults.put("OPT_5", 0);
                this.pollResults.put("OPT_6", 0);
                this.pollResults.put("OPT_7", 0);
                this.pollResults.put("OPT_8", 0);
            } else if ("MULTISELECT".equals(pollType) || "MULTISELECT_MULTIPLE".equals(pollType)) {
                // Initialize options for multiselect poll (both single and multiple selection)
                if (options != null && !options.isEmpty()) {
                    this.pollOptions = new ArrayList<>(options);
                    for (int i = 0; i < options.size(); i++) {
                        this.pollResults.put("OPT_" + i, 0);
                    }
                }
            }
        } finally {
            lock.unlock();
        }
    }

    public boolean castVote(String sessionId, String vote) {
        lock.lock();
        try {
            // Check if poll is active
            if (!"ACTIVE".equals(pollStatus)) {
                return false;
            }

            // Check if vote is valid
            if (!pollResults.containsKey(vote)) {
                return false;
            }

            // Handle multiple selection differently
            if ("MULTISELECT_MULTIPLE".equals(pollType)) {
                Set<String> currentVotes = sessionMultiVotes.computeIfAbsent(sessionId, k -> new HashSet<>());

                // Toggle vote: if already voted for this option, remove it (deselect)
                if (currentVotes.contains(vote)) {
                    currentVotes.remove(vote);
                    pollResults.put(vote, pollResults.get(vote) - 1);
                } else {
                    // Check if participant has reached max votes
                    if (currentVotes.size() >= votesPerParticipant) {
                        return false; // Max votes reached, cannot add more
                    }
                    currentVotes.add(vote);
                    pollResults.put(vote, pollResults.get(vote) + 1);
                }
                return true;
            } else {
                // Single selection (original behavior)
                // Check if session has already voted - if so, remove the old vote
                String previousVote = sessionVotes.get(sessionId);
                if (previousVote != null) {
                    // Decrement previous vote
                    pollResults.put(previousVote, pollResults.get(previousVote) - 1);
                }

                // Record new vote
                pollResults.put(vote, pollResults.get(vote) + 1);
                sessionVotes.put(sessionId, vote);
                return true;
            }
        } finally {
            lock.unlock();
        }
    }

    public void endPoll(String sessionId) {
        lock.lock();
        try {
            requireChairAccess(sessionId);
            if (pollQuestion != null && "ACTIVE".equals(pollStatus)) {
                // Store results for display
                int totalVotes = pollResults.values().stream().mapToInt(Integer::intValue).sum();
                lastPollResults = new PollResults(
                        pollQuestion,
                        pollType,
                        new HashMap<>(pollResults),
                        totalVotes,
                        pollOptions != null ? List.copyOf(pollOptions) : null);

                // Mark poll as ended (results shown in overlay to participants)
                pollStatus = "ENDED";
            }
        } finally {
            lock.unlock();
        }
    }

    public void closePoll(String sessionId) {
        lock.lock();
        try {
            requireChairAccess(sessionId);
            if ("ENDED".equals(pollStatus)) {
                // Clear active poll state, keep lastPollResults
                pollQuestion = null;
                pollType = null;
                pollStatus = "CLOSED";
                pollResults.clear();
                sessionVotes.clear();
                pollOptions = null;
            }
        } finally {
            lock.unlock();
        }
    }

    public void cancelPoll(String sessionId) {
        lock.lock();
        try {
            requireChairAccess(sessionId);
            // Clear all poll state without saving to lastResults
            pollQuestion = null;
            pollType = null;
            pollStatus = null;
            pollResults.clear();
            sessionVotes.clear();
            pollOptions = null;
        } finally {
            lock.unlock();
        }
    }

    public boolean isPolling() {
        lock.lock();
        try {
            return "ACTIVE".equals(pollStatus);
        } finally {
            lock.unlock();
        }
    }

    public void updateRoomConfig(String sessionId, String topic, MeetingGoal meetingGoal,
            ParticipationFormat participationFormat, DecisionRule decisionRule, Deliverable deliverable) {
        lock.lock();
        try {
            requireChairAccess(sessionId);
            this.topic = topic;
            this.meetingGoal = meetingGoal;
            this.participationFormat = participationFormat;
            this.decisionRule = decisionRule;
            this.deliverable = deliverable;
        } finally {
            lock.unlock();
        }
    }
}