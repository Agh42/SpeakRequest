package de.koderman;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

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
    public record RequestSpeak(String name) {}
    public record Withdraw(String name) {}
    public record TimerCtrl(String action) {} // "start" | "pause" | "reset"
    public record SetLimit(int seconds) {}
    public record Join(String name) {}
    public record CreateRoom() {}

    public record Participant(String id, String name, long requestedAt) {}
    public record Current(Participant entry, long startedAtSec, int elapsedMs, boolean running, int limitSec) {}
    public record State(List<Participant> queue, Current current, long meetingStartSec, int defaultLimitSec, String roomCode) {}
    public record RoomInfo(String roomCode, boolean exists) {}

    // ---------------- Room Management ----------------
    public static class Room {
        private final String roomCode;
        private final List<Participant> queue = new ArrayList<>();
        private Current current = null;
        private final long meetingStartSec = Instant.now().getEpochSecond();
        private int defaultLimitSec = 180; // per-speaker
        private final ReentrantLock lock = new ReentrantLock();

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
                return new State(List.copyOf(queue), current, meetingStartSec, defaultLimitSec, roomCode);
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
    }

    // ---------------- In-memory meeting state ----------------
    @Controller
    @RequiredArgsConstructor
    public static class MeetingController {
        private final SimpMessagingTemplate broker;
        private final ConcurrentHashMap<String, Room> rooms = new ConcurrentHashMap<>();
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
            } while (rooms.containsKey(code));
            return code;
        }

        private Room getOrCreateRoom(String roomCode) {
            return rooms.computeIfAbsent(roomCode, Room::new);
        }

        @PostMapping("/api/rooms")
        @ResponseBody
        public RoomInfo createRoom() {
            String roomCode = createUniqueRoomCode();
            Room room = new Room(roomCode);
            rooms.put(roomCode, room);
            return new RoomInfo(roomCode, true);
        }

        @GetMapping("/api/rooms/{roomCode}")
        @ResponseBody
        public RoomInfo checkRoom(@PathVariable String roomCode) {
            String normalizedRoomCode = normalizeRoomCode(roomCode);
            return new RoomInfo(normalizedRoomCode, rooms.containsKey(normalizedRoomCode));
        }

        private String uid() { return Long.toString(System.nanoTime(), 36); }

        private void broadcast(String roomCode) {
            Room room = rooms.get(roomCode);
            if (room != null) {
                State s = room.snapshot();
                broker.convertAndSend("/topic/room/" + roomCode + "/state", s);
            }
        }

        @MessageMapping("/room/{roomCode}/join")
        public void join(@DestinationVariable String roomCode, @Payload Join msg) {
            String normalizedRoomCode = normalizeRoomCode(roomCode);
            getOrCreateRoom(normalizedRoomCode); // Ensure room exists
            broadcast(normalizedRoomCode);
        }

        @MessageMapping("/room/{roomCode}/request")
        public void request(@DestinationVariable String roomCode, @Payload RequestSpeak msg) {
            if (msg == null || msg.name() == null || msg.name().isBlank()) return;

            String normalizedRoomCode = normalizeRoomCode(roomCode);
            Room room = getOrCreateRoom(normalizedRoomCode);
            room.addParticipantToQueue(new Participant(uid(), msg.name().trim(), Instant.now().getEpochSecond()));
            broadcast(normalizedRoomCode);
        }

        @MessageMapping("/room/{roomCode}/withdraw")
        public void withdraw(@DestinationVariable String roomCode, @Payload Withdraw msg) {
            if (msg == null || msg.name() == null || msg.name().isBlank()) return;
            String normalizedRoomCode = normalizeRoomCode(roomCode);
            Room room = rooms.get(normalizedRoomCode);
            if (room == null) return;

            room.withdrawParticipant(msg.name());
            broadcast(normalizedRoomCode);
        }

        @MessageMapping("/room/{roomCode}/next")
        public void next(@DestinationVariable String roomCode) {
            String normalizedRoomCode = normalizeRoomCode(roomCode);
            Room room = rooms.get(normalizedRoomCode);
            if (room == null) return;

            room.nextParticipant();
            broadcast(normalizedRoomCode);
        }

        @MessageMapping("/room/{roomCode}/timer")
        public void timer(@DestinationVariable String roomCode, @Payload TimerCtrl ctrl) {
            if (ctrl == null || ctrl.action() == null) return;
            String normalizedRoomCode = normalizeRoomCode(roomCode);
            Room room = rooms.get(normalizedRoomCode);
            if (room == null) return;

            switch (ctrl.action().toLowerCase()) {
                case "start" -> room.startTimer();
                case "pause" -> room.pauseTimer();
                case "reset" -> room.resetTimer();
            }
            broadcast(normalizedRoomCode);
        }

        @MessageMapping("/room/{roomCode}/setLimit")
        public void setLimit(@DestinationVariable String roomCode, @Payload SetLimit msg) {
            if (msg == null) return;
            int s = Math.max(10, Math.min(3600, msg.seconds()));
            String normalizedRoomCode = normalizeRoomCode(roomCode);
            Room room = rooms.get(normalizedRoomCode);
            if (room == null) return;

            room.updateLimit(s);
            broadcast(normalizedRoomCode);
        }
    }
}
