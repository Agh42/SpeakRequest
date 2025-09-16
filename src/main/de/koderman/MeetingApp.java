package de.koderman;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.time.Instant;
import java.util.*;
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
    public record RequestSpeak(String name, String role) {}
    public record Withdraw(String name) {}
    public record TimerCtrl(String action) {} // "start" | "pause" | "reset"
    public record SetLimit(int seconds) {}
    public record Join(String name, String role) {}

    public record Participant(String id, String name, String role, long requestedAt) {}
    public record Current(Participant entry, long startedAtSec, int elapsedMs, boolean running, int limitSec) {}
    public record State(List<Participant> queue, Current current, long meetingStartSec, int defaultLimitSec) {}

    // ---------------- In-memory meeting state ----------------
    @Controller
    public static class MeetingController {
        private final SimpMessagingTemplate broker;
        private final ReentrantLock lock = new ReentrantLock();

        private final List<Participant> queue = new ArrayList<>();
        private Current current = null;
        private final long meetingStartSec = Instant.now().getEpochSecond();
        private int defaultLimitSec = 180; // per-speaker

        public MeetingController(SimpMessagingTemplate broker) {
            this.broker = broker;
        }

        @RestController
        public static class Health {
            @GetMapping("/healthz")
            public Map<String, String> ok() { return Map.of("status", "ok"); }
        }

        private String uid() { return Long.toString(System.nanoTime(), 36); }
        private int findIndexByNameUnsafe(String name) {
            for (int i = 0; i < queue.size(); i++) {
                if (queue.get(i).name().equalsIgnoreCase(name)) return i;
            }
            return -1;
        }
        private int findIndexByName(String name) {
            lock.lock();
            try {
                return findIndexByNameUnsafe(name);
            } finally {
                lock.unlock();
            }
        }
        private void broadcast() {
            State s = snapshot();
            broker.convertAndSend("/topic/state", s);
        }
        private State snapshot() {
            lock.lock();
            try {
                return new State(List.copyOf(queue), current, meetingStartSec, defaultLimitSec);
            } finally {
                lock.unlock();
            }
        }

        @MessageMapping("/join")
        public void join(@Payload Join msg) { broadcast(); }

        @MessageMapping("/request")
        public void request(@Payload RequestSpeak msg) {
            if (msg == null || msg.name() == null || msg.name().isBlank()) return;
            String role = (msg.role() == null || msg.role().isBlank()) ? "Member" : msg.role().trim();

            lock.lock();
            try {
                if (current != null && current.entry().name().equalsIgnoreCase(msg.name())) return;
                if (findIndexByNameUnsafe(msg.name()) >= 0) return;
                queue.add(new Participant(uid(), msg.name().trim(), role, Instant.now().getEpochSecond()));
            } finally { lock.unlock(); }
            broadcast();
        }

        @MessageMapping("/withdraw")
        public void withdraw(@Payload Withdraw msg) {
            if (msg == null || msg.name() == null || msg.name().isBlank()) return;
            lock.lock();
            try {
                int idx = findIndexByNameUnsafe(msg.name());
                if (idx >= 0) queue.remove(idx);
            } finally { lock.unlock(); }
            broadcast();
        }

        @MessageMapping("/next")
        public void next() {
            lock.lock();
            try {
                current = null;
                if (!queue.isEmpty()) {
                    Participant next = queue.remove(0);
                    current = new Current(next, Instant.now().getEpochSecond(), 0, true, defaultLimitSec);
                }
            } finally { lock.unlock(); }
            broadcast();
        }

        @MessageMapping("/timer")
        public void timer(@Payload TimerCtrl ctrl) {
            if (ctrl == null || ctrl.action() == null) return;
            lock.lock();
            try {
                if (current == null) return;
                long nowSec = Instant.now().getEpochSecond();
                switch (ctrl.action().toLowerCase()) {
                    case "start" -> {
                        if (!current.running())
                            current = new Current(current.entry(), nowSec, current.elapsedMs(), true, current.limitSec());
                    }
                    case "pause" -> {
                        if (current.running()) {
                            int addMs = (int) ((nowSec - current.startedAtSec()) * 1000);
                            current = new Current(current.entry(), current.startedAtSec(),
                                    current.elapsedMs() + addMs, false, current.limitSec());
                        }
                    }
                    case "reset" -> current = new Current(current.entry(), nowSec, 0, true, current.limitSec());
                }
            } finally { lock.unlock(); }
            broadcast();
        }

        @MessageMapping("/setLimit")
        public void setLimit(@Payload SetLimit msg) {
            if (msg == null) return;
            int s = Math.max(10, Math.min(3600, msg.seconds()));
            lock.lock();
            try {
                defaultLimitSec = s;
                if (current != null) {
                    current = new Current(current.entry(), current.startedAtSec(), current.elapsedMs(), current.running(), s);
                }
            } finally { lock.unlock(); }
            broadcast();
        }
    }
}