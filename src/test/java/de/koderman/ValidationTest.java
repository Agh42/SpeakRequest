package de.koderman;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import de.koderman.domain.*;
import de.koderman.infrastructure.*;


class ValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testRequestSpeak_validName() {
        RequestSpeak request = new RequestSpeak("John Doe");
        Set<ConstraintViolation<RequestSpeak>> violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "Valid name should not have violations");
    }

    @Test
    void testRequestSpeak_validNameWithHyphen() {
        RequestSpeak request = new RequestSpeak("Mary-Jane");
        Set<ConstraintViolation<RequestSpeak>> violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "Name with hyphen should be valid");
    }

    @Test
    void testRequestSpeak_validNameWithApostrophe() {
        RequestSpeak request = new RequestSpeak("O'Brien");
        Set<ConstraintViolation<RequestSpeak>> violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "Name with apostrophe should be valid");
    }

    @Test
    void testRequestSpeak_validNameWithNumbers() {
        RequestSpeak request = new RequestSpeak("User123");
        Set<ConstraintViolation<RequestSpeak>> violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "Name with numbers should be valid");
    }

    @Test
    void testRequestSpeak_nameTooLong() {
        RequestSpeak request = new RequestSpeak("ThisIsAVeryLongNameThatExceedsTheThirtyCharacterLimit");
        Set<ConstraintViolation<RequestSpeak>> violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Name longer than 30 characters should be invalid");
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("30 characters")));
    }

    @Test
    void testRequestSpeak_nameWithInvalidCharacters() {
        RequestSpeak request = new RequestSpeak("John@Doe");
        Set<ConstraintViolation<RequestSpeak>> violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Name with @ should be invalid");
    }

    @Test
    void testRequestSpeak_blankName() {
        RequestSpeak request = new RequestSpeak("   ");
        Set<ConstraintViolation<RequestSpeak>> violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Blank name should be invalid");
    }

    @Test
    void testRequestSpeak_nullName() {
        RequestSpeak request = new RequestSpeak(null);
        Set<ConstraintViolation<RequestSpeak>> violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Null name should be invalid");
    }

    @Test
    void testWithdraw_validName() {
        Withdraw withdraw = new Withdraw("Alice");
        Set<ConstraintViolation<Withdraw>> violations = validator.validate(withdraw);
        assertTrue(violations.isEmpty(), "Valid name should not have violations");
    }

    @Test
    void testJoin_validName() {
        Join join = new Join("Bob-123");
        Set<ConstraintViolation<Join>> violations = validator.validate(join);
        assertTrue(violations.isEmpty(), "Valid name should not have violations");
    }

    @Test
    void testAssumeChair_validParticipantName() {
        AssumeChair assumeChair = new AssumeChair("Charlie O'Neill", "req123");
        Set<ConstraintViolation<AssumeChair>> violations = validator.validate(assumeChair);
        assertTrue(violations.isEmpty(), "Valid participant name should not have violations");
    }

    @Test
    void testAssumeChair_invalidParticipantName() {
        AssumeChair assumeChair = new AssumeChair("Charlie@Test", "req123");
        Set<ConstraintViolation<AssumeChair>> violations = validator.validate(assumeChair);
        assertFalse(violations.isEmpty(), "Participant name with @ should be invalid");
    }

    @Test
    void testRequestSpeak_exactly30Characters() {
        RequestSpeak request = new RequestSpeak("123456789012345678901234567890");
        Set<ConstraintViolation<RequestSpeak>> violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "Name with exactly 30 characters should be valid");
    }

    @Test
    void testRequestSpeak_31Characters() {
        RequestSpeak request = new RequestSpeak("1234567890123456789012345678901");
        Set<ConstraintViolation<RequestSpeak>> violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Name with 31 characters should be invalid");
    }
}
