package de.koderman;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testRequestSpeak_validName() {
        MeetingApp.RequestSpeak request = new MeetingApp.RequestSpeak("John Doe");
        Set<ConstraintViolation<MeetingApp.RequestSpeak>> violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "Valid name should not have violations");
    }

    @Test
    void testRequestSpeak_validNameWithHyphen() {
        MeetingApp.RequestSpeak request = new MeetingApp.RequestSpeak("Mary-Jane");
        Set<ConstraintViolation<MeetingApp.RequestSpeak>> violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "Name with hyphen should be valid");
    }

    @Test
    void testRequestSpeak_validNameWithApostrophe() {
        MeetingApp.RequestSpeak request = new MeetingApp.RequestSpeak("O'Brien");
        Set<ConstraintViolation<MeetingApp.RequestSpeak>> violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "Name with apostrophe should be valid");
    }

    @Test
    void testRequestSpeak_validNameWithNumbers() {
        MeetingApp.RequestSpeak request = new MeetingApp.RequestSpeak("User123");
        Set<ConstraintViolation<MeetingApp.RequestSpeak>> violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "Name with numbers should be valid");
    }

    @Test
    void testRequestSpeak_nameTooLong() {
        MeetingApp.RequestSpeak request = new MeetingApp.RequestSpeak("ThisIsAVeryLongNameThatExceedsTheThirtyCharacterLimit");
        Set<ConstraintViolation<MeetingApp.RequestSpeak>> violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Name longer than 30 characters should be invalid");
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("30 characters")));
    }

    @Test
    void testRequestSpeak_nameWithInvalidCharacters() {
        MeetingApp.RequestSpeak request = new MeetingApp.RequestSpeak("John@Doe");
        Set<ConstraintViolation<MeetingApp.RequestSpeak>> violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Name with @ should be invalid");
    }

    @Test
    void testRequestSpeak_blankName() {
        MeetingApp.RequestSpeak request = new MeetingApp.RequestSpeak("   ");
        Set<ConstraintViolation<MeetingApp.RequestSpeak>> violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Blank name should be invalid");
    }

    @Test
    void testRequestSpeak_nullName() {
        MeetingApp.RequestSpeak request = new MeetingApp.RequestSpeak(null);
        Set<ConstraintViolation<MeetingApp.RequestSpeak>> violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Null name should be invalid");
    }

    @Test
    void testWithdraw_validName() {
        MeetingApp.Withdraw withdraw = new MeetingApp.Withdraw("Alice");
        Set<ConstraintViolation<MeetingApp.Withdraw>> violations = validator.validate(withdraw);
        assertTrue(violations.isEmpty(), "Valid name should not have violations");
    }

    @Test
    void testJoin_validName() {
        MeetingApp.Join join = new MeetingApp.Join("Bob-123");
        Set<ConstraintViolation<MeetingApp.Join>> violations = validator.validate(join);
        assertTrue(violations.isEmpty(), "Valid name should not have violations");
    }

    @Test
    void testAssumeChair_validParticipantName() {
        MeetingApp.AssumeChair assumeChair = new MeetingApp.AssumeChair("Charlie O'Neill", "req123");
        Set<ConstraintViolation<MeetingApp.AssumeChair>> violations = validator.validate(assumeChair);
        assertTrue(violations.isEmpty(), "Valid participant name should not have violations");
    }

    @Test
    void testAssumeChair_invalidParticipantName() {
        MeetingApp.AssumeChair assumeChair = new MeetingApp.AssumeChair("Charlie@Test", "req123");
        Set<ConstraintViolation<MeetingApp.AssumeChair>> violations = validator.validate(assumeChair);
        assertFalse(violations.isEmpty(), "Participant name with @ should be invalid");
    }

    @Test
    void testRequestSpeak_exactly30Characters() {
        MeetingApp.RequestSpeak request = new MeetingApp.RequestSpeak("123456789012345678901234567890");
        Set<ConstraintViolation<MeetingApp.RequestSpeak>> violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "Name with exactly 30 characters should be valid");
    }

    @Test
    void testRequestSpeak_31Characters() {
        MeetingApp.RequestSpeak request = new MeetingApp.RequestSpeak("1234567890123456789012345678901");
        Set<ConstraintViolation<MeetingApp.RequestSpeak>> violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Name with 31 characters should be invalid");
    }
}
