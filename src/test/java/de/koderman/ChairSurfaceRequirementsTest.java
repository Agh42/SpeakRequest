package de.koderman;

import static org.junit.jupiter.api.Assertions.assertAll;
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
}