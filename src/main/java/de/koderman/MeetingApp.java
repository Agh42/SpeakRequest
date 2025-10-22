package de.koderman;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@SpringBootApplication
public class MeetingApp {
    public static void main(String[] args) {
        SpringApplication.run(MeetingApp.class, args);
    }

    // ---------------- WebSocket / STOMP configuration ----------------
    @Configuration
    @EnableWebSocketMessageBroker
    public static class WsConfig implements WebSocketMessageBrokerConfigurer {
        @Override
        public void registerStompEndpoints(StompEndpointRegistry registry) {
            // only pure WebSocket endpoint (no SockJS)
            registry.addEndpoint("/ws").setAllowedOriginPatterns("*");
        }
        @Override
        public void configureMessageBroker(MessageBrokerRegistry registry) {
            registry.enableSimpleBroker("/topic");
            registry.setApplicationDestinationPrefixes("/app");
        }
    }

    // ---------------- ENUMs ----------------
    public enum MeetingGoal {
        SHARE_INFORMATION("Share Information", "Ensure everyone has the same facts, updates, or context."),
        ADVANCE_THINKING("Advance the Thinking", "Develop ideas further through discussion, analysis, and reflection."),
        OBTAIN_INPUT("Obtain Input", "Gather perspectives, feedback, or expertise from participants."),
        MAKE_DECISIONS("Make Decisions", "Reach agreement or choose a course of action collaboratively."),
        IMPROVE_COMMUNICATION("Improve Communication", "Strengthen clarity, understanding, and mutual trust among participants."),
        BUILD_CAPACITY("Build Capacity", "Develop participants' skills, knowledge, or confidence to act effectively."),
        BUILD_COMMUNITY("Build Community", "Foster relationships, connection, and shared purpose within the group.");
        
        private final String displayName;
        private final String description;
        
        MeetingGoal(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
        
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
    }
    
    public enum ParticipationFormat {
        STRUCTURED_GO_AROUNDS("Structured Go-Arounds", "Everyone contributes in turn, ensuring equal participation and balanced input."),
        PRESENTATIONS_AND_REPORTS("Presentations and Reports", "Individuals or teams share prepared findings or updates with the group."),
        SMALL_GROUPS("Small Groups", "Participants work in subgroups to explore topics or solve problems collaboratively."),
        LISTING_IDEAS("Listing Ideas", "The group rapidly generates and records ideas without immediate evaluation."),
        JIGSAW("Jigsaw", "Each subgroup learns part of a topic and teaches it to others, combining knowledge collaboratively."),
        INDIVIDUAL_WRITING("Individual Writing", "Participants reflect or respond in writing before sharing or discussing."),
        MULTI_TASKING("Multi-Tasking", "Participants engage in parallel activities contributing to a shared goal or outcome."),
        OPEN_DISCUSSION("Open Discussion", "Participants freely exchange views and reactions in an unstructured conversation."),
        FISHBOWLS("Fishbowls", "A small inner group discusses while others observe, then roles switch for reflection and feedback."),
        TRADESHOW("Tradeshow", "Participants display and explain their work or ideas at stations others visit in rotation."),
        SCRAMBLER("Scrambler", "Participants move between tasks, stations, or partners to stimulate diverse perspectives."),
        ROLEPLAYS("Roleplays", "Participants act out scenarios to explore perspectives, behaviors, or problem-solving strategies.");
        
        private final String displayName;
        private final String description;
        
        ParticipationFormat(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
        
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
    }
    
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

    // ---------------- DTOs ----------------
    public record RequestSpeak(
        @NotBlank(message = "Name is required")
        @Size(max = 30, message = "Name must not exceed 30 characters")
        @Pattern(regexp = "^[a-zA-Z0-9 '.-]+$", message = "Name can only contain letters, numbers, spaces, dots, hyphens, and apostrophes")
        String name
    ) {}
    
    public record Withdraw(
        @NotBlank(message = "Name is required")
        @Size(max = 30, message = "Name must not exceed 30 characters")
        @Pattern(regexp = "^[a-zA-Z0-9 '.-]+$", message = "Name can only contain letters, numbers, spaces, dots, hyphens, and apostrophes")
        String name
    ) {}
    
    public record TimerCtrl(String action) {} // "start" | "pause" | "reset"
    public record SetLimit(int seconds) {}
    
    public record Join(
        @NotBlank(message = "Name is required")
        @Size(max = 30, message = "Name must not exceed 30 characters")
        @Pattern(regexp = "^[a-zA-Z0-9 '.-]+$", message = "Name can only contain letters, numbers, spaces, dots, hyphens, and apostrophes")
        String name
    ) {}
    
    public record CreateRoom() {}
    
    public record AssumeChair(
        @NotBlank(message = "Participant name is required")
        @Size(max = 30, message = "Participant name must not exceed 30 characters")
        @Pattern(regexp = "^[a-zA-Z0-9 '.-]+$", message = "Participant name can only contain letters, numbers, spaces, dots, hyphens, and apostrophes")
        String participantName,
        String requestId
    ) {}

    public record Participant(String id, String name, long requestedAt) {}
    public record Current(Participant entry, long startedAtSec, int elapsedMs, boolean running, int limitSec) {}
    public record RoomConfig(String topic, MeetingGoal meetingGoal, ParticipationFormat participationFormat, DecisionRule decisionRule, Deliverable deliverable) {}
    public record State(List<Participant> queue, Current current, long meetingStartSec, int defaultLimitSec, String roomCode, boolean chairOccupied, PollState pollState, RoomConfig roomConfig) {}
    public record RoomInfo(String roomCode, boolean exists) {}
    
    public record UpdateRoomConfig(
        @Size(max = 100, message = "Topic must not exceed 100 characters")
        String topic,
        @Size(max = 100, message = "Meeting goal must not exceed 100 characters")
        String meetingGoal,
        @Size(max = 100, message = "Participation format must not exceed 100 characters")
        String participationFormat,
        @Size(max = 100, message = "Decision rule must not exceed 100 characters")
        String decisionRule,
        @Size(max = 100, message = "Deliverable must not exceed 100 characters")
        String deliverable
    ) {}
    
    // Polling-related DTOs
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
    
    public record CastVote(
        @NotBlank(message = "Vote is required")
        String vote // "YES" or "NO"
    ) {}
    
    public record DestroyRoom() {}
    
    public record RoomDestroyed(String message, String landingUrl) {}
    
    public record PollState(
        String question,
        String pollType,
        String status, // "ACTIVE", "ENDED", "CLOSED", null
        Map<String, Integer> results, // vote option -> count
        Integer totalVotes,
        PollResults lastResults, // Results of the last ended poll
        List<String> options // For MULTISELECT polls - list of option labels
    ) {}
    
    public record PollResults(
        String question,
        String pollType,
        Map<String, Integer> results,
        Integer totalVotes,
        List<String> options // For MULTISELECT polls - list of option labels
    ) {}

    // ---------------- Room Management ----------------
    public static class Room {
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
        private PollResults lastPollResults = null;
        private List<String> pollOptions = null; // For MULTISELECT polls - list of option labels

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
                        pollOptions != null ? List.copyOf(pollOptions) : null
                    );
                } else if ("CLOSED".equals(pollStatus) && lastPollResults != null) {
                    // Poll is closed, show only last results
                    pollState = new PollState(null, null, "CLOSED", Map.of(), 0, lastPollResults, null);
                } else if (lastPollResults != null) {
                    // No active poll, but we have last results
                    pollState = new PollState(null, null, null, Map.of(), 0, lastPollResults, null);
                }
                
                RoomConfig roomConfig = new RoomConfig(topic, meetingGoal, participationFormat, decisionRule, deliverable);
                
                return new State(List.copyOf(queue), current, meetingStartSec, defaultLimitSec, roomCode, chairSessionId != null, pollState, roomConfig);
            } finally {
                lock.unlock();
            }
        }

        public boolean isChairSession(String sessionId) {
            lock.lock();
            try {
                return Optional.ofNullable(sessionId)
                        .map(sid -> sid.equals(chairSessionId))
                        .orElse(false);
            } finally {
                lock.unlock();
            }
        }

        public boolean hasChair() {
            lock.lock();
            try {
                return chairSessionId != null;
            } finally {
                lock.unlock();
            }
        }

        public boolean assumeChairRole(String sessionId) {
            lock.lock();
            try {
                if (chairSessionId == null) {
                    chairSessionId = sessionId;
                    return true;
                }
                return false; // Chair already occupied
            } finally {
                lock.unlock();
            }
        }

        public void releaseChairRole(String sessionId) {
            lock.lock();
            try {
                Optional.ofNullable(sessionId)
                        .filter(sid -> sid.equals(chairSessionId))
                        .ifPresent(sid -> chairSessionId = null);
            } finally {
                lock.unlock();
            }
        }

        private int findIndexByNameUnsafe(String name) {
            for (int i = 0; i < queue.size(); i++) {
                if (queue.get(i).name().equalsIgnoreCase(name)) return i;
            }
            return -1;
        }

        // DDD methods - encapsulate internal state management
        
        public void nextParticipant() {
            lock.lock();
            try {
                current = null;
                if (!queue.isEmpty()) {
                    Participant next = queue.remove(0);
                    current = new Current(next, Instant.now().getEpochSecond(), 0, true, defaultLimitSec);
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
                }
            } finally {
                lock.unlock();
            }
        }

        public void startTimer() {
            lock.lock();
            try {
                if (current == null) return;
                if (!current.running()) {
                    long nowSec = Instant.now().getEpochSecond();
                    current = new Current(current.entry(), nowSec, current.elapsedMs(), true, current.limitSec());
                }
            } finally {
                lock.unlock();
            }
        }

        public void pauseTimer() {
            lock.lock();
            try {
                if (current == null) return;
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

        public void resetTimer() {
            lock.lock();
            try {
                if (current == null) return;
                long nowSec = Instant.now().getEpochSecond();
                current = new Current(current.entry(), nowSec, 0, true, current.limitSec());
            } finally {
                lock.unlock();
            }
        }

        public void updateLimit(int seconds) {
            lock.lock();
            try {
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
                    return;
                }
                if (findIndexByNameUnsafe(participant.name()) >= 0) {
                    return;
                }
                queue.add(participant);
            } finally {
                lock.unlock();
            }
        }
        
        // Polling methods
        public void startPoll(String question, String pollType, List<String> options) {
            lock.lock();
            try {
                this.pollQuestion = question;
                this.pollType = pollType;
                this.pollStatus = "ACTIVE";
                this.pollResults.clear();
                this.sessionVotes.clear();
                this.pollOptions = null;
                
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
                } else if ("MULTISELECT".equals(pollType)) {
                    // Initialize options for multiselect poll
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
            } finally {
                lock.unlock();
            }
        }
        
        public void endPoll() {
            lock.lock();
            try {
                if (pollQuestion != null && "ACTIVE".equals(pollStatus)) {
                    // Store results for display
                    int totalVotes = pollResults.values().stream().mapToInt(Integer::intValue).sum();
                    lastPollResults = new PollResults(
                        pollQuestion,
                        pollType,
                        new HashMap<>(pollResults),
                        totalVotes,
                        pollOptions != null ? List.copyOf(pollOptions) : null
                    );
                    
                    // Mark poll as ended (results shown in overlay to participants)
                    pollStatus = "ENDED";
                }
            } finally {
                lock.unlock();
            }
        }
        
        public void closePoll() {
            lock.lock();
            try {
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
        
        public void cancelPoll() {
            lock.lock();
            try {
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
        
        public void updateRoomConfig(String topic, MeetingGoal meetingGoal, ParticipationFormat participationFormat, DecisionRule decisionRule, Deliverable deliverable) {
            lock.lock();
            try {
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

    // ---------------- Room Repository ----------------
    public static class RoomRepository {
        private static final int MAX_ROOMS = 500000;
        private final ConcurrentHashMap<String, Room> roomsByCode = new ConcurrentHashMap<>();
        private final ConcurrentHashMap<String, String> sessionToRoomCode = new ConcurrentHashMap<>();
        // TreeMap sorted by creation timestamp for efficient oldest room lookup
        private final TreeMap<Long, Room> roomsByTimestamp = new TreeMap<>();
        private final Object roomCreationLock = new Object();

        public Room getOrCreate(String roomCode) {
            // First check if room already exists (fast path, no lock needed)
            Room existingRoom = roomsByCode.get(roomCode);
            if (existingRoom != null) {
                return existingRoom;
            }
            
            // Room doesn't exist, need to create it (synchronized)
            synchronized (roomCreationLock) {
                // Double-check after acquiring lock
                existingRoom = roomsByCode.get(roomCode);
                if (existingRoom != null) {
                    return existingRoom;
                }
                
                // Before creating a new room, check if we've reached the limit
                if (roomsByCode.size() >= MAX_ROOMS) {
                    removeOldestRoom();
                }
                
                Room newRoom = new Room(roomCode);
                roomsByCode.put(roomCode, newRoom);
                roomsByTimestamp.put(newRoom.getMeetingStartSec(), newRoom);
                return newRoom;
            }
        }
        
        private void removeOldestRoom() {
            // Must be called within synchronized block
            // Get the first (oldest) entry from the TreeMap
            Map.Entry<Long, Room> oldestEntry = roomsByTimestamp.firstEntry();
            
            if (oldestEntry != null) {
                Room oldestRoom = oldestEntry.getValue();
                String oldestRoomCode = oldestRoom.getRoomCode();
                
                // Remove from both maps
                roomsByCode.remove(oldestRoomCode);
                roomsByTimestamp.remove(oldestEntry.getKey());
                
                // Clean up session tracking for the removed room
                sessionToRoomCode.entrySet().removeIf(entry -> 
                    oldestRoomCode.equals(entry.getValue())
                );
            }
        }

        public Optional<Room> getByCode(String roomCode) {
            return Optional.ofNullable(roomsByCode.get(roomCode));
        }

        public boolean exists(String roomCode) {
            return roomsByCode.containsKey(roomCode);
        }

        public Optional<Room> getBySessionId(String sessionId) {
            return Optional.ofNullable(sessionToRoomCode.get(sessionId))
                    .map(roomsByCode::get);
        }

        public void trackSession(String sessionId, String roomCode) {
            sessionToRoomCode.put(sessionId, roomCode);
        }

        public void untrackSession(String sessionId) {
            sessionToRoomCode.remove(sessionId);
        }
        
        public void destroyRoom(String roomCode) {
            synchronized (roomCreationLock) {
                // Remove room from registry
                Room room = roomsByCode.remove(roomCode);
                
                // Remove from timestamp map
                if (room != null) {
                    roomsByTimestamp.remove(room.getMeetingStartSec());
                    
                    // Remove all session tracking for this room
                    sessionToRoomCode.entrySet().removeIf(entry -> roomCode.equals(entry.getValue()));
                }
            }
        }
        
        public List<String> getSessionsForRoom(String roomCode) {
            return sessionToRoomCode.entrySet().stream()
                    .filter(entry -> roomCode.equals(entry.getValue()))
                    .map(Map.Entry::getKey)
                    .toList();
        }
    }

    // ---------------- In-memory meeting state ----------------
    @Controller
    @RequiredArgsConstructor
    public static class MeetingController {
        private final SimpMessagingTemplate broker;
        private final RoomRepository roomRepository = new RoomRepository();
        private final Random random = new Random();

        @RestController
        public static class Health {
            @GetMapping("/healthz")
            public Map<String, String> ok() { return Map.of("status", "ok"); }
        }

        // Route handlers for different views
        @GetMapping("/")
        public String home() {
            return "redirect:/landing.html";
        }

        @GetMapping("/chair/{roomCode}")
        public String chairView(@PathVariable String roomCode) {
            String normalizedRoomCode = normalizeRoomCode(roomCode);
            return "redirect:/chair.html?room=" + normalizedRoomCode;
        }

        @GetMapping("/room/{roomCode}")
        public String participantView(@PathVariable String roomCode) {
            String normalizedRoomCode = normalizeRoomCode(roomCode);
            return "redirect:/participant.html?room=" + normalizedRoomCode;
        }

        private String generateRoomCode() {
            String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ123456789"; // Removed "0" to avoid confusion with "O"
            StringBuilder code = new StringBuilder();
            for (int i = 0; i < 4; i++) {
                code.append(chars.charAt(random.nextInt(chars.length())));
            }
            return code.toString();
        }

        private String normalizeRoomCode(String roomCode) {
            if (roomCode == null) return null;
            // Convert "0" to "O" to avoid confusion when users enter room codes
            return roomCode.toUpperCase().replace("0", "O");
        }

        private String createUniqueRoomCode() {
            String code;
            do {
                code = generateRoomCode();
            } while (roomRepository.exists(code));
            return code;
        }

        private Room getOrCreateRoom(String roomCode) {
            return roomRepository.getOrCreate(roomCode);
        }

        @PostMapping("/api/rooms")
        @ResponseBody
        public RoomInfo createRoom() {
            String roomCode = createUniqueRoomCode();
            Room room = new Room(roomCode);
            roomRepository.getOrCreate(roomCode);
            return new RoomInfo(roomCode, true);
        }

        @GetMapping("/api/rooms/{roomCode}")
        @ResponseBody
        public RoomInfo checkRoom(@PathVariable String roomCode) {
            String normalizedRoomCode = normalizeRoomCode(roomCode);
            return new RoomInfo(normalizedRoomCode, roomRepository.exists(normalizedRoomCode));
        }

        // Metadata endpoints for enum descriptions
        @GetMapping("/api/metadata/meeting-goals")
        @ResponseBody
        public Map<String, Object> getMeetingGoals() {
            List<Map<String, String>> goals = new ArrayList<>();
            for (MeetingGoal goal : MeetingGoal.values()) {
                goals.add(Map.of(
                    "value", goal.name(),
                    "displayName", goal.getDisplayName(),
                    "description", goal.getDescription()
                ));
            }
            return Map.of("version", "1.0", "data", goals);
        }

        @GetMapping("/api/metadata/participation-formats")
        @ResponseBody
        public Map<String, Object> getParticipationFormats() {
            List<Map<String, String>> formats = new ArrayList<>();
            for (ParticipationFormat format : ParticipationFormat.values()) {
                formats.add(Map.of(
                    "value", format.name(),
                    "displayName", format.getDisplayName(),
                    "description", format.getDescription()
                ));
            }
            return Map.of("version", "1.0", "data", formats);
        }

        @GetMapping("/api/metadata/decision-rules")
        @ResponseBody
        public Map<String, Object> getDecisionRules() {
            List<Map<String, String>> rules = new ArrayList<>();
            for (DecisionRule rule : DecisionRule.values()) {
                rules.add(Map.of(
                    "value", rule.name(),
                    "displayName", rule.getDisplayName(),
                    "description", rule.getDescription()
                ));
            }
            return Map.of("version", "1.0", "data", rules);
        }

        @GetMapping("/api/metadata/deliverables")
        @ResponseBody
        public Map<String, Object> getDeliverables() {
            List<Map<String, String>> deliverables = new ArrayList<>();
            for (Deliverable deliverable : Deliverable.values()) {
                deliverables.add(Map.of(
                    "value", deliverable.name(),
                    "displayName", deliverable.getDisplayName(),
                    "description", deliverable.getDescription()
                ));
            }
            return Map.of("version", "1.0", "data", deliverables);
        }

        private String uid() { return Long.toString(System.nanoTime(), 36); }

        private void broadcast(String roomCode) {
            roomRepository.getByCode(roomCode)
                    .ifPresent(room -> {
                        State s = room.snapshot();
                        broker.convertAndSend("/topic/room/" + roomCode + "/state", s);
                    });
        }

        @MessageMapping("/room/{roomCode}/join")
        public void join(@DestinationVariable String roomCode, @Valid @Payload Join msg, StompHeaderAccessor headerAccessor) {
            String normalizedRoomCode = normalizeRoomCode(roomCode);
            String sessionId = headerAccessor.getSessionId();
            
            Room room = roomRepository.getOrCreate(normalizedRoomCode);
            roomRepository.trackSession(sessionId, normalizedRoomCode);
            
            // Check if this is a chair joining (by checking the name)
            if ("Chair".equals(msg.name())) {
                room.assumeChairRole(sessionId);
            }
            
            broadcast(normalizedRoomCode);
        }

        @MessageMapping("/room/{roomCode}/request")
        public void request(@DestinationVariable String roomCode, @Valid @Payload RequestSpeak msg) {
            if (msg == null || msg.name() == null || msg.name().isBlank()) return;

            String normalizedRoomCode = normalizeRoomCode(roomCode);
            Room room = roomRepository.getOrCreate(normalizedRoomCode);
            room.addParticipantToQueue(new Participant(uid(), msg.name().trim(), Instant.now().getEpochSecond()));
            broadcast(normalizedRoomCode);
        }

        @MessageMapping("/room/{roomCode}/withdraw")
        public void withdraw(@DestinationVariable String roomCode, @Valid @Payload Withdraw msg) {
            if (msg == null || msg.name() == null || msg.name().isBlank()) return;
            String normalizedRoomCode = normalizeRoomCode(roomCode);
            
            roomRepository.getByCode(normalizedRoomCode)
                    .ifPresent(room -> {
                        room.withdrawParticipant(msg.name());
                        broadcast(normalizedRoomCode);
                    });
        }

        @MessageMapping("/room/{roomCode}/next")
        public void next(@DestinationVariable String roomCode) {
            String normalizedRoomCode = normalizeRoomCode(roomCode);
            
            roomRepository.getByCode(normalizedRoomCode)
                    .ifPresent(room -> {
                        room.nextParticipant();
                        broadcast(normalizedRoomCode);
                    });
        }

        @MessageMapping("/room/{roomCode}/timer")
        public void timer(@DestinationVariable String roomCode, @Payload TimerCtrl ctrl) {
            if (ctrl == null || ctrl.action() == null) return;
            String normalizedRoomCode = normalizeRoomCode(roomCode);
            
            roomRepository.getByCode(normalizedRoomCode)
                    .ifPresent(room -> {
                        switch (ctrl.action().toLowerCase()) {
                            case "start" -> room.startTimer();
                            case "pause" -> room.pauseTimer();
                            case "reset" -> room.resetTimer();
                        }
                        broadcast(normalizedRoomCode);
                    });
        }

        @MessageMapping("/room/{roomCode}/setLimit")
        public void setLimit(@DestinationVariable String roomCode, @Payload SetLimit msg) {
            if (msg == null) return;
            int s = Math.max(10, Math.min(3600, msg.seconds()));
            String normalizedRoomCode = normalizeRoomCode(roomCode);
            
            roomRepository.getByCode(normalizedRoomCode)
                    .ifPresent(room -> {
                        room.updateLimit(s);
                        broadcast(normalizedRoomCode);
                    });
        }

        @MessageMapping("/room/{roomCode}/assumeChair")
        public void assumeChair(@DestinationVariable String roomCode, @Valid @Payload AssumeChair msg, StompHeaderAccessor headerAccessor) {
            String normalizedRoomCode = normalizeRoomCode(roomCode);
            
            roomRepository.getByCode(normalizedRoomCode)
                    .ifPresentOrElse(
                            room -> {
                                String sessionId = headerAccessor.getSessionId();
                                
                                // Try to assume chair role - Room entity handles the check
                                if (room.assumeChairRole(sessionId)) {
                                    roomRepository.trackSession(sessionId, normalizedRoomCode);
                                    broadcast(normalizedRoomCode);
                                    
                                    // Send success response back on the general topic but include request ID
                                    broker.convertAndSend("/topic/room/" + normalizedRoomCode + "/chairAssumed", 
                                        Map.of("success", true, "requestId", msg.requestId()));
                                } else {
                                    // Chair is already occupied, fail silently - just broadcast state update
                                    broadcast(normalizedRoomCode);
                                }
                            },
                            () -> {
                                // Room doesn't exist, fail silently
                            }
                    );
        }
        
        @MessageMapping("/room/{roomCode}/poll/start")
        public void startPoll(@DestinationVariable String roomCode, @Valid @Payload StartPoll msg, StompHeaderAccessor headerAccessor) {
            String normalizedRoomCode = normalizeRoomCode(roomCode);
            String sessionId = headerAccessor.getSessionId();
            
            roomRepository.getByCode(normalizedRoomCode)
                    .ifPresent(room -> {
                        // Only chair can start a poll
                        if (room.isChairSession(sessionId)) {
                            room.startPoll(msg.question(), msg.pollType(), msg.options());
                            broadcast(normalizedRoomCode);
                        }
                    });
        }
        
        @MessageMapping("/room/{roomCode}/poll/vote")
        public void castVote(@DestinationVariable String roomCode, @Valid @Payload CastVote msg, StompHeaderAccessor headerAccessor) {
            String normalizedRoomCode = normalizeRoomCode(roomCode);
            String sessionId = headerAccessor.getSessionId();
            
            roomRepository.getByCode(normalizedRoomCode)
                    .ifPresent(room -> {
                        if (room.castVote(sessionId, msg.vote())) {
                            broadcast(normalizedRoomCode);
                        }
                    });
        }
        
        @MessageMapping("/room/{roomCode}/poll/end")
        public void endPoll(@DestinationVariable String roomCode, StompHeaderAccessor headerAccessor) {
            String normalizedRoomCode = normalizeRoomCode(roomCode);
            String sessionId = headerAccessor.getSessionId();
            
            roomRepository.getByCode(normalizedRoomCode)
                    .ifPresent(room -> {
                        // Only chair can end a poll
                        if (room.isChairSession(sessionId)) {
                            room.endPoll();
                            broadcast(normalizedRoomCode);
                        }
                    });
        }
        
        @MessageMapping("/room/{roomCode}/poll/close")
        public void closePoll(@DestinationVariable String roomCode, StompHeaderAccessor headerAccessor) {
            String normalizedRoomCode = normalizeRoomCode(roomCode);
            String sessionId = headerAccessor.getSessionId();
            
            roomRepository.getByCode(normalizedRoomCode)
                    .ifPresent(room -> {
                        // Only chair can close a poll
                        if (room.isChairSession(sessionId)) {
                            room.closePoll();
                            broadcast(normalizedRoomCode);
                        }
                    });
        }
        
        @MessageMapping("/room/{roomCode}/poll/cancel")
        public void cancelPoll(@DestinationVariable String roomCode, StompHeaderAccessor headerAccessor) {
            String normalizedRoomCode = normalizeRoomCode(roomCode);
            String sessionId = headerAccessor.getSessionId();
            
            roomRepository.getByCode(normalizedRoomCode)
                    .ifPresent(room -> {
                        // Only chair can cancel a poll
                        if (room.isChairSession(sessionId)) {
                            room.cancelPoll();
                            broadcast(normalizedRoomCode);
                        }
                    });
        }
        
        @MessageMapping("/room/{roomCode}/updateConfig")
        public void updateRoomConfig(@DestinationVariable String roomCode, @Payload UpdateRoomConfig msg, StompHeaderAccessor headerAccessor) {
            String normalizedRoomCode = normalizeRoomCode(roomCode);
            String sessionId = headerAccessor.getSessionId();
            
            roomRepository.getByCode(normalizedRoomCode)
                    .ifPresent(room -> {
                        // Only chair can update room configuration
                        if (room.isChairSession(sessionId)) {
                            // Parse enum values (allow null/empty)
                            MeetingGoal meetingGoal = parseEnum(MeetingGoal.class, msg.meetingGoal());
                            ParticipationFormat participationFormat = parseEnum(ParticipationFormat.class, msg.participationFormat());
                            DecisionRule decisionRule = parseEnum(DecisionRule.class, msg.decisionRule());
                            Deliverable deliverable = parseEnum(Deliverable.class, msg.deliverable());
                            
                            room.updateRoomConfig(msg.topic(), meetingGoal, participationFormat, decisionRule, deliverable);
                            broadcast(normalizedRoomCode);
                        }
                    });
        }
        
        private <E extends Enum<E>> E parseEnum(Class<E> enumClass, String value) {
            if (value == null || value.isEmpty()) {
                return null;
            }
            try {
                return Enum.valueOf(enumClass, value);
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
        
        @MessageMapping("/room/{roomCode}/destroy")
        public void destroyRoom(@DestinationVariable String roomCode, StompHeaderAccessor headerAccessor) {
            String normalizedRoomCode = normalizeRoomCode(roomCode);
            String sessionId = headerAccessor.getSessionId();
            
            roomRepository.getByCode(normalizedRoomCode)
                    .ifPresent(room -> {
                        // Only chair can destroy the room
                        if (room.isChairSession(sessionId)) {
                            // Create destruction message
                            String landingUrl = "/landing.html";
                            RoomDestroyed destroyedMsg = new RoomDestroyed(
                                "The room has been closed by the chair.",
                                landingUrl
                            );
                            
                            // Broadcast to all participants and popout windows
                            broker.convertAndSend("/topic/room/" + normalizedRoomCode + "/destroyed", destroyedMsg);
                            
                            // Get all sessions in this room for cleanup
                            List<String> sessions = roomRepository.getSessionsForRoom(normalizedRoomCode);
                            
                            // Remove the room and all references
                            roomRepository.destroyRoom(normalizedRoomCode);
                            
                            // Untrack all sessions
                            sessions.forEach(roomRepository::untrackSession);
                        }
                    });
        }

        @EventListener
        public void handleWebSocketDisconnect(SessionDisconnectEvent event) {
            String sessionId = event.getSessionId();
            
            roomRepository.getBySessionId(sessionId)
                    .filter(room -> room.isChairSession(sessionId))
                    .ifPresent(room -> {
                        room.releaseChairRole(sessionId);
                        broadcast(room.getRoomCode());
                    });
            
            roomRepository.untrackSession(sessionId);
        }
    }
}
