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

    // ---------------- DTOs ----------------
    public record RequestSpeak(
        @NotBlank(message = "Name is required")
        @Size(max = 30, message = "Name must not exceed 30 characters")
        @Pattern(regexp = "^[a-zA-Z0-9 '-]+$", message = "Name can only contain letters, numbers, spaces, hyphens, and apostrophes")
        String name
    ) {}
    
    public record Withdraw(
        @NotBlank(message = "Name is required")
        @Size(max = 30, message = "Name must not exceed 30 characters")
        @Pattern(regexp = "^[a-zA-Z0-9 '-]+$", message = "Name can only contain letters, numbers, spaces, hyphens, and apostrophes")
        String name
    ) {}
    
    public record TimerCtrl(String action) {} // "start" | "pause" | "reset"
    public record SetLimit(int seconds) {}
    
    public record Join(
        @NotBlank(message = "Name is required")
        @Size(max = 30, message = "Name must not exceed 30 characters")
        @Pattern(regexp = "^[a-zA-Z0-9 '-]+$", message = "Name can only contain letters, numbers, spaces, hyphens, and apostrophes")
        String name
    ) {}
    
    public record CreateRoom() {}
    
    public record AssumeChair(
        @NotBlank(message = "Participant name is required")
        @Size(max = 30, message = "Participant name must not exceed 30 characters")
        @Pattern(regexp = "^[a-zA-Z0-9 '-]+$", message = "Participant name can only contain letters, numbers, spaces, hyphens, and apostrophes")
        String participantName,
        String requestId
    ) {}

    public record Participant(String id, String name, long requestedAt) {}
    public record Current(Participant entry, long startedAtSec, int elapsedMs, boolean running, int limitSec) {}
    public record State(List<Participant> queue, Current current, long meetingStartSec, int defaultLimitSec, String roomCode, boolean chairOccupied, PollState pollState) {}
    public record RoomInfo(String roomCode, boolean exists) {}
    
    // Polling-related DTOs
    public record StartPoll(
        @NotBlank(message = "Question is required")
        @Size(max = 200, message = "Question must not exceed 200 characters")
        String question,
        @NotBlank(message = "Poll type is required")
        String pollType // "YES_NO" or "GRADIENTS" for Gradients of Agreement
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
        PollResults lastResults // Results of the last ended poll
    ) {}
    
    public record PollResults(
        String question,
        Map<String, Integer> results,
        Integer totalVotes
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
        
        // Polling state
        private String pollQuestion = null;
        private String pollType = null;
        private String pollStatus = null; // "ACTIVE", "ENDED", "CLOSED", null
        private final Map<String, Integer> pollResults = new HashMap<>();
        private final Map<String, String> sessionVotes = new HashMap<>(); // Track each session's vote (allows vote changes)
        private PollResults lastPollResults = null;

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
                        lastPollResults
                    );
                } else if ("CLOSED".equals(pollStatus) && lastPollResults != null) {
                    // Poll is closed, show only last results
                    pollState = new PollState(null, null, "CLOSED", Map.of(), 0, lastPollResults);
                } else if (lastPollResults != null) {
                    // No active poll, but we have last results
                    pollState = new PollState(null, null, null, Map.of(), 0, lastPollResults);
                }
                
                return new State(List.copyOf(queue), current, meetingStartSec, defaultLimitSec, roomCode, chairSessionId != null, pollState);
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
        public void startPoll(String question, String pollType) {
            lock.lock();
            try {
                this.pollQuestion = question;
                this.pollType = pollType;
                this.pollStatus = "ACTIVE";
                this.pollResults.clear();
                this.sessionVotes.clear();
                
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
                        new HashMap<>(pollResults),
                        totalVotes
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
                            room.startPoll(msg.question(), msg.pollType());
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
