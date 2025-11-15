package de.koderman.infrastructure;

import de.koderman.domain.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import jakarta.validation.Valid;
import java.time.Instant;
import java.util.*;

@Controller
@RequiredArgsConstructor
public class MeetingController {
    private final SimpMessagingTemplate broker;
    private final RoomRepository roomRepository = new RoomRepository();
    private final Random random = new Random();
    
    @MessageExceptionHandler
    public void handleRoomNotFound(RoomNotFoundException ex) {
        // Send error message to the specific room topic so clients can handle it
        RoomError error = new RoomError(
            "Room not found: " + ex.getRoomCode(),
            ex.getRoomCode(),
            "room_not_found",
            "/landing.html"
        );
        broker.convertAndSend("/topic/room/" + ex.getRoomCode() + "/error", error);
    }
    
    @MessageExceptionHandler
    public void handleChairAccessException(ChairAccessException ex) {
        // Send error message only to the specific client that made the illegal access attempt
        RoomError error = new RoomError(
            "Unauthorized chair access: " + ex.getMessage(),
            ex.getRoomCode(),
            "chair_access_denied",
            "/landing.html"
        );
        // Send to the specific user/session that caused the exception
        broker.convertAndSendToUser(ex.getSessionId(), "/queue/error", error);
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

    @PostMapping("/api/rooms")
    @ResponseBody
    public RoomInfo createRoom() {
        String roomCode = createUniqueRoomCode();
        roomRepository.createRoom(roomCode);
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
        try {
            Room room = roomRepository.getByCodeOrThrow(roomCode);
            State s = room.snapshot();
            broker.convertAndSend("/topic/room/" + roomCode + "/state", s);
        } catch (RoomNotFoundException ex) {
            // Room was destroyed during the operation, send error to clients
            RoomError error = new RoomError(
                "Room no longer exists",
                roomCode,
                "room_destroyed", 
                "/landing.html"
            );
            broker.convertAndSend("/topic/room/" + roomCode + "/error", error);
        }
    }

    @MessageMapping("/room/{roomCode}/join")
    public void join(@DestinationVariable String roomCode, @Valid @Payload Join msg, StompHeaderAccessor headerAccessor) {
        String normalizedRoomCode = normalizeRoomCode(roomCode);
        String sessionId = headerAccessor.getSessionId();
        
        // This will throw RoomNotFoundException if room doesn't exist
        Room room = roomRepository.getByCodeOrThrow(normalizedRoomCode);
        
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
        Room room = roomRepository.getByCodeOrThrow(normalizedRoomCode);
        
        room.addParticipantToQueue(new Participant(uid(), msg.name().trim(), Instant.now().getEpochSecond()));
        broadcast(normalizedRoomCode);
    }

    @MessageMapping("/room/{roomCode}/withdraw")
    public void withdraw(@DestinationVariable String roomCode, @Valid @Payload Withdraw msg) {
        if (msg == null || msg.name() == null || msg.name().isBlank()) return;
        String normalizedRoomCode = normalizeRoomCode(roomCode);
        
        Room room = roomRepository.getByCodeOrThrow(normalizedRoomCode);
        room.withdrawParticipant(msg.name());
        broadcast(normalizedRoomCode);
    }

    @MessageMapping("/room/{roomCode}/next")
    public void next(@DestinationVariable String roomCode, StompHeaderAccessor headerAccessor) {
        String normalizedRoomCode = normalizeRoomCode(roomCode);
        String sessionId = headerAccessor.getSessionId();
        
        Room room = roomRepository.getByCodeOrThrow(normalizedRoomCode);
        room.nextParticipant(sessionId);
        broadcast(normalizedRoomCode);
    }

    @MessageMapping("/room/{roomCode}/timer")
    public void timer(@DestinationVariable String roomCode, @Payload TimerCtrl ctrl, StompHeaderAccessor headerAccessor) {
        if (ctrl == null || ctrl.action() == null) return;
        String normalizedRoomCode = normalizeRoomCode(roomCode);
        String sessionId = headerAccessor.getSessionId();
        
        Room room = roomRepository.getByCodeOrThrow(normalizedRoomCode);
        switch (ctrl.action().toLowerCase()) {
            case "start" -> room.startTimer(sessionId);
            case "pause" -> room.pauseTimer(sessionId);
            case "reset" -> room.resetTimer(sessionId);
        }
        broadcast(normalizedRoomCode);
    }

    @MessageMapping("/room/{roomCode}/setLimit")
    public void setLimit(@DestinationVariable String roomCode, @Payload SetLimit msg, StompHeaderAccessor headerAccessor) {
        if (msg == null) return;
        int s = Math.max(10, Math.min(3600, msg.seconds()));
        String normalizedRoomCode = normalizeRoomCode(roomCode);
        String sessionId = headerAccessor.getSessionId();
        
        Room room = roomRepository.getByCodeOrThrow(normalizedRoomCode);
        room.updateLimit(sessionId, s);
        broadcast(normalizedRoomCode);
    }

    @MessageMapping("/room/{roomCode}/assumeChair")
    public void assumeChair(@DestinationVariable String roomCode, @Valid @Payload AssumeChair msg, StompHeaderAccessor headerAccessor) {
        String normalizedRoomCode = normalizeRoomCode(roomCode);
        String sessionId = headerAccessor.getSessionId();
        
        Room room = roomRepository.getByCodeOrThrow(normalizedRoomCode);
        
        // Try to assume chair role - Room entity handles the check
        room.assumeChairRole(sessionId);
        roomRepository.trackSession(sessionId, normalizedRoomCode);
        broadcast(normalizedRoomCode);
        
        // Send success response back on the general topic but include request ID
        broker.convertAndSend("/topic/room/" + normalizedRoomCode + "/chairAssumed", 
            Map.of("success", true, "requestId", msg.requestId()));
    }
    
    @MessageMapping("/room/{roomCode}/poll/start")
    public void startPoll(@DestinationVariable String roomCode, @Valid @Payload StartPoll msg, StompHeaderAccessor headerAccessor) {
        String normalizedRoomCode = normalizeRoomCode(roomCode);
        String sessionId = headerAccessor.getSessionId();
        
        Room room = roomRepository.getByCodeOrThrow(normalizedRoomCode);
        
        room.startPoll(sessionId, msg.question(), msg.pollType(), msg.options(), msg.votesPerParticipant());
        broadcast(normalizedRoomCode);
    }
    
    @MessageMapping("/room/{roomCode}/poll/vote")
    public void castVote(@DestinationVariable String roomCode, @Valid @Payload CastVote msg, StompHeaderAccessor headerAccessor) {
        String normalizedRoomCode = normalizeRoomCode(roomCode);
        String sessionId = headerAccessor.getSessionId();
        
        Room room = roomRepository.getByCodeOrThrow(normalizedRoomCode);
        
        if (room.castVote(sessionId, msg.vote())) {
            broadcast(normalizedRoomCode);
        }
    }
    
    @MessageMapping("/room/{roomCode}/poll/end")
    public void endPoll(@DestinationVariable String roomCode, StompHeaderAccessor headerAccessor) {
        String normalizedRoomCode = normalizeRoomCode(roomCode);
        String sessionId = headerAccessor.getSessionId();
        
        Room room = roomRepository.getByCodeOrThrow(normalizedRoomCode);
        
        room.endPoll(sessionId);
        broadcast(normalizedRoomCode);
    }
    
    @MessageMapping("/room/{roomCode}/poll/close")
    public void closePoll(@DestinationVariable String roomCode, StompHeaderAccessor headerAccessor) {
        String normalizedRoomCode = normalizeRoomCode(roomCode);
        String sessionId = headerAccessor.getSessionId();
        
        Room room = roomRepository.getByCodeOrThrow(normalizedRoomCode);
        
        room.closePoll(sessionId);
        broadcast(normalizedRoomCode);
    }
    
    @MessageMapping("/room/{roomCode}/poll/cancel")
    public void cancelPoll(@DestinationVariable String roomCode, StompHeaderAccessor headerAccessor) {
        String normalizedRoomCode = normalizeRoomCode(roomCode);
        String sessionId = headerAccessor.getSessionId();
        
        Room room = roomRepository.getByCodeOrThrow(normalizedRoomCode);
        
        room.cancelPoll(sessionId);
        broadcast(normalizedRoomCode);
    }
    
    @MessageMapping("/room/{roomCode}/updateConfig")
    public void updateRoomConfig(@DestinationVariable String roomCode, @Payload UpdateRoomConfig msg, StompHeaderAccessor headerAccessor) {
        String normalizedRoomCode = normalizeRoomCode(roomCode);
        String sessionId = headerAccessor.getSessionId();
        
        Room room = roomRepository.getByCodeOrThrow(normalizedRoomCode);
        
        // Parse enum values (allow null/empty)
        MeetingGoal meetingGoal = parseEnum(MeetingGoal.class, msg.meetingGoal());
        ParticipationFormat participationFormat = parseEnum(ParticipationFormat.class, msg.participationFormat());
        DecisionRule decisionRule = parseEnum(DecisionRule.class, msg.decisionRule());
        Deliverable deliverable = parseEnum(Deliverable.class, msg.deliverable());
        
        room.updateRoomConfig(sessionId, msg.topic(), meetingGoal, participationFormat, decisionRule, deliverable);
        broadcast(normalizedRoomCode);
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
        
        Room room = roomRepository.getByCodeOrThrow(normalizedRoomCode);
        
        // Only chair can destroy the room - check via Room's internal validation
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