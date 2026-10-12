package de.koderman.domain;

import java.util.*;

/** Room-owned seating history. All access is protected by the Room lock. */
public final class ConferenceTable {
    public static final int CAPACITY = 20;

    public record Seat(RoomMember member, boolean chair, boolean speaking, boolean queued, long lastSpokeSequence) {}
    public record View(int capacity, List<Seat> seats, int overflowCount) {}

    private final List<String> slots = new ArrayList<>();
    private final Map<String, Long> lastSpoke = new HashMap<>();
    private long turnSequence;
    private RoomMember currentMember;

    void speakerStarted(Participant speaker, List<RoomMember> members) {
        currentMember = findMember(speaker, members);
        if (currentMember == null) {
            // A queued person may disconnect before their turn. Keep the active speaker visible.
            currentMember = new RoomMember(speaker.id(), speaker.name(), speaker.requestedAt());
        }
        lastSpoke.put(currentMember.id(), ++turnSequence);
    }

    View snapshot(List<RoomMember> members, String chairSessionId, Current current, List<Participant> queue) {
        LinkedHashMap<String, RoomMember> present = new LinkedHashMap<>();
        for (RoomMember member : members) present.put(member.id(), member);
        RoomMember chair = members.stream()
                .filter(m -> !m.proxy() && m.sessionId().equals(chairSessionId)).findFirst().orElse(null);
        String chairId = chair == null ? null : chair.id();

        if (current == null) currentMember = null;
        else if (currentMember != null && present.containsKey(currentMember.id())) {
            currentMember = present.get(currentMember.id());
        }
        String speakerId = currentMember == null ? null : currentMember.id();
        LinkedHashMap<String, RoomMember> eligible = new LinkedHashMap<>(present);
        if (currentMember != null) eligible.putIfAbsent(speakerId, currentMember);
        lastSpoke.keySet().retainAll(eligible.keySet());
        eligible.remove(chairId);

        // Keep holes until they have been filled: a departure/replacement need not move neighbours.
        for (int i = 0; i < slots.size(); i++) {
            if (!eligible.containsKey(slots.get(i))) slots.set(i, null);
        }
        List<String> candidates = new ArrayList<>(eligible.keySet());
        candidates.removeAll(slots);
        candidates.sort(Comparator.<String>comparingInt(id -> id.equals(speakerId) ? 0 : 1)
                .thenComparing(Comparator.comparingLong(this::recency).reversed()));
        for (String id : candidates) {
            int hole = slots.indexOf(null);
            if (hole >= 0) slots.set(hole, id);
            else if (slots.size() < CAPACITY - 1) slots.add(id);
            else {
                // Iterate in join order to break equal-recency ties consistently.
                String oldest = eligible.keySet().stream().filter(slots::contains)
                        .filter(occupant -> !occupant.equals(speakerId))
                        .min(Comparator.comparingLong(this::recency)).orElseThrow();
                if (id.equals(speakerId) || recency(id) > recency(oldest)) {
                    slots.set(slots.indexOf(oldest), id);
                }
            }
        }
        slots.removeIf(Objects::isNull);

        Set<String> queued = new HashSet<>();
        for (Participant entry : queue) {
            RoomMember member = findMember(entry, members);
            if (member != null) queued.add(member.id());
        }
        List<Seat> seats = new ArrayList<>();
        seats.add(seat(chair, true, speakerId, queued));
        for (String id : slots) seats.add(seat(eligible.get(id), false, speakerId, queued));
        long displayed = seats.stream().filter(s -> s.member() != null && present.containsKey(s.member().id())).count();
        return new View(CAPACITY, List.copyOf(seats), present.size() - (int) displayed);
    }

    private long recency(String id) {
        return lastSpoke.getOrDefault(id, 0L);
    }

    private Seat seat(RoomMember member, boolean chair, String speakerId, Set<String> queued) {
        return new Seat(member, chair, member != null && member.id().equals(speakerId),
                member != null && queued.contains(member.id()), member == null ? 0 : recency(member.id()));
    }

    private static RoomMember findMember(Participant entry, List<RoomMember> members) {
        for (RoomMember member : members) {
            if (member.id().equals(entry.id())) return member;
        }
        // Chair-created queue IDs are per request; proxy presence IDs are stable across turns.
        for (RoomMember member : members) {
            if (entry.id().startsWith(member.sessionId() + ":proxy:")
                    && member.name().equalsIgnoreCase(entry.name())) return member;
        }
        // The chair may request a turn for an already connected person. Only an unambiguous
        // name can identify that person when the request does not carry their session ID.
        List<RoomMember> matches = members.stream()
                .filter(m -> m.name().equalsIgnoreCase(entry.name())).toList();
        return matches.size() == 1 ? matches.get(0) : null;
    }
}
