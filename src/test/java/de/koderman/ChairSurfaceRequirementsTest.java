package de.koderman;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class ChairSurfaceRequirementsTest {

    private static final Path CHAIR_HTML = Path.of("src/main/resources/static/chair.html");

    private String chairHtml() throws IOException {
        return Files.readString(CHAIR_HTML, StandardCharsets.UTF_8);
    }

    @Test
    void roomTitleIsClampedInsideTheAvatarCircle() throws IOException {
        String html = chairHtml();

        assertAll(
            () -> assertTrue(html.contains("conference-table-topic-button"), "topic button class is present"),
            () -> assertTrue(html.contains("-webkit-line-clamp: 2;"), "topic is clamped to two lines"),
            () -> assertTrue(html.contains("text-overflow: ellipsis;"), "topic uses ellipsis overflow handling")
        );
    }

    @Test
    void topicLabelJumpsToTheExistingRoomMenuSection() throws IOException {
        String html = chairHtml();

        assertAll(
            () -> assertTrue(html.contains("id=\"conferenceTableTopic\""), "conference table topic element exists"),
            () -> assertTrue(html.contains("data-menu-anchor=\"section-menu\""), "topic is wired to the menu anchor"),
            () -> assertTrue(html.contains("scrollToSection('section-menu')"), "menu anchor scroll handler still targets the room menu")
        );
    }

    @Test
    void timerThresholdsMatchThePhaseRequirements() throws IOException {
        String html = chairHtml();

        assertAll(
            () -> assertTrue(html.contains("const warnThreshold = c.limitSec * 1000 * 0.75;"), "warning threshold starts at 25% remaining"),
            () -> assertTrue(html.contains("const dangerThreshold = c.limitSec * 1000 * 0.9;"), "danger threshold starts at 10% remaining"),
            () -> assertTrue(html.contains("elRemaining.textContent = '(remaining: 00:00)';"), "remaining display stays pinned at 00:00 once time expires")
        );
    }

    @Test
    void chairSurfaceStillPreservesTheLiveStateWiring() throws IOException {
        String html = chairHtml();

        assertAll(
            () -> assertTrue(html.contains("/topic/room/${roomCode}/state"), "state subscription remains intact"),
            () -> assertTrue(html.contains("/topic/room/${roomCode}/destroyed"), "destroyed topic remains intact"),
            () -> assertTrue(html.contains("/user/queue/error"), "chair error queue remains intact"),
            () -> assertTrue(html.contains("elConferenceTableTopic.textContent = DOMPurify.sanitize((state.roomConfig?.topic || '').trim() || '[No Agendum]');"), "conference table topic still renders from room state")
        );
    }

    @Test
    void avatarLabelTruncatesToThreeCharacterPrefixForLongNames() throws IOException {
        String html = chairHtml();

        // names up to 5 chars show fully; longer names (e.g. 'annemarie') truncate to 'ann…'
        assertAll(
            () -> assertTrue(html.contains("cleaned.length <= 5"), "avatar label helper shows names up to 5 characters fully"),
            () -> assertTrue(html.contains("cleaned.slice(0, 3)"), "avatar label helper uses three-character prefix plus ellipsis"),
            () -> assertFalse(html.contains("cleaned.slice(0, 7)"), "old seven-character truncation is removed")
        );
    }

    @Test
    void chairBootstrapUsesPlainChairLabel() throws IOException {
        String html = chairHtml();

        assertAll(
            () -> assertTrue(html.contains("name:'chair'"), "join payload uses plain 'chair' label"),
            () -> assertTrue(html.contains("participantName: 'chair'"), "assume-chair payload uses plain 'chair' label"),
            () -> assertFalse(html.contains("Chair-Candidate"), "old Chair-Candidate label is removed")
        );
    }
}