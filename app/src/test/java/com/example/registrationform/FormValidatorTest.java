package com.example.registrationform;

import org.junit.Test;

import static org.junit.Assert.*;

public class FormValidatorTest {

    @Test
    public void validateName_validInput() {
        FormValidator.ValidationResult result = FormValidator.validateName("John Doe");
        assertTrue(result.isValid());
        assertEquals(FormValidator.Error.NONE, result.error);
    }

    @Test
    public void validateName_validInput_withSpaces() {
        FormValidator.ValidationResult result = FormValidator.validateName("  Jane Smith  ");
        assertTrue(result.isValid());
    }

    @Test
    public void validateName_empty() {
        FormValidator.ValidationResult result = FormValidator.validateName("");
        assertFalse(result.isValid());
        assertEquals(FormValidator.Error.NAME_REQUIRED, result.error);
    }

    @Test
    public void validateName_null() {
        FormValidator.ValidationResult result = FormValidator.validateName(null);
        assertFalse(result.isValid());
        assertEquals(FormValidator.Error.NAME_REQUIRED, result.error);
    }

    @Test
    public void validateName_whitespaceOnly() {
        FormValidator.ValidationResult result = FormValidator.validateName("   ");
        assertFalse(result.isValid());
        assertEquals(FormValidator.Error.NAME_REQUIRED, result.error);
    }

    @Test
    public void validateName_oneCharacter() {
        FormValidator.ValidationResult result = FormValidator.validateName("A");
        assertFalse(result.isValid());
        assertEquals(FormValidator.Error.NAME_TOO_SHORT, result.error);
    }

    @Test
    public void validateName_twoCharacters() {
        FormValidator.ValidationResult result = FormValidator.validateName("Ab");
        assertTrue(result.isValid());
    }

    @Test
    public void validateDob_validPastDate() {
        long pastDate = 631152000000L; // 1990-01-01 UTC
        FormValidator.ValidationResult result = FormValidator.validateDob(pastDate);
        assertTrue(result.isValid());
    }

    @Test
    public void validateDob_today() {
        long today = java.time.Instant.now().toEpochMilli();
        FormValidator.ValidationResult result = FormValidator.validateDob(today);
        assertTrue(result.isValid());
    }

    @Test
    public void validateDob_futureDate() {
        long futureDate = java.time.Instant.now().toEpochMilli() + 86400000; // tomorrow
        FormValidator.ValidationResult result = FormValidator.validateDob(futureDate);
        assertFalse(result.isValid());
        assertEquals(FormValidator.Error.DOB_FUTURE, result.error);
    }

    @Test
    public void validateDob_null() {
        FormValidator.ValidationResult result = FormValidator.validateDob(null);
        assertFalse(result.isValid());
        assertEquals(FormValidator.Error.DOB_REQUIRED, result.error);
    }

    @Test
    public void validateEmail_validSimple() {
        FormValidator.ValidationResult result = FormValidator.validateEmail("user@example.com");
        assertTrue(result.isValid());
    }

    @Test
    public void validateEmail_validWithSubdomain() {
        FormValidator.ValidationResult result = FormValidator.validateEmail("user@mail.example.com");
        assertTrue(result.isValid());
    }

    @Test
    public void validateEmail_validWithPlus() {
        FormValidator.ValidationResult result = FormValidator.validateEmail("user+tag@example.com");
        assertTrue(result.isValid());
    }

    @Test
    public void validateEmail_validWithDots() {
        FormValidator.ValidationResult result = FormValidator.validateEmail("first.last@example.com");
        assertTrue(result.isValid());
    }

    @Test
    public void validateEmail_empty() {
        FormValidator.ValidationResult result = FormValidator.validateEmail("");
        assertFalse(result.isValid());
        assertEquals(FormValidator.Error.EMAIL_REQUIRED, result.error);
    }

    @Test
    public void validateEmail_null() {
        FormValidator.ValidationResult result = FormValidator.validateEmail(null);
        assertFalse(result.isValid());
        assertEquals(FormValidator.Error.EMAIL_REQUIRED, result.error);
    }

    @Test
    public void validateEmail_whitespaceOnly() {
        FormValidator.ValidationResult result = FormValidator.validateEmail("   ");
        assertFalse(result.isValid());
        assertEquals(FormValidator.Error.EMAIL_REQUIRED, result.error);
    }

    @Test
    public void validateEmail_missingAt() {
        FormValidator.ValidationResult result = FormValidator.validateEmail("userexample.com");
        assertFalse(result.isValid());
        assertEquals(FormValidator.Error.EMAIL_INVALID, result.error);
    }

    @Test
    public void validateEmail_missingDomain() {
        FormValidator.ValidationResult result = FormValidator.validateEmail("user@");
        assertFalse(result.isValid());
        assertEquals(FormValidator.Error.EMAIL_INVALID, result.error);
    }

    @Test
    public void validateEmail_missingDomainPart() {
        FormValidator.ValidationResult result = FormValidator.validateEmail("user@.com");
        assertFalse(result.isValid());
        assertEquals(FormValidator.Error.EMAIL_INVALID, result.error);
    }

    @Test
    public void validateEmail_spacesInLocalPart() {
        FormValidator.ValidationResult result = FormValidator.validateEmail("user name@example.com");
        assertFalse(result.isValid());
        assertEquals(FormValidator.Error.EMAIL_INVALID, result.error);
    }

    @Test
    public void validateEmail_spacesInDomain() {
        FormValidator.ValidationResult result = FormValidator.validateEmail("user@exam ple.com");
        assertFalse(result.isValid());
        assertEquals(FormValidator.Error.EMAIL_INVALID, result.error);
    }

    @Test
    public void validateEmail_leadingTrailingSpaces() {
        FormValidator.ValidationResult result = FormValidator.validateEmail("  user@example.com  ");
        assertTrue(result.isValid());
    }

    @Test
    public void validateAll_valid() {
        long pastDate = 631152000000L; // 1990-01-01 UTC
        FormValidator.ValidationResult result = FormValidator.validateAll("John Doe", pastDate, "john@example.com");
        assertTrue(result.isValid());
    }

    @Test
    public void validateAll_nameFailsFirst() {
        long pastDate = 631152000000L;
        FormValidator.ValidationResult result = FormValidator.validateAll("A", pastDate, "john@example.com");
        assertFalse(result.isValid());
        assertEquals(FormValidator.Error.NAME_TOO_SHORT, result.error);
    }

    @Test
    public void validateAll_dobFailsSecond() {
        long futureDate = java.time.Instant.now().toEpochMilli() + 86400000;
        FormValidator.ValidationResult result = FormValidator.validateAll("John Doe", futureDate, "john@example.com");
        assertFalse(result.isValid());
        assertEquals(FormValidator.Error.DOB_FUTURE, result.error);
    }

    @Test
    public void validateAll_emailFailsThird() {
        long pastDate = 631152000000L;
        FormValidator.ValidationResult result = FormValidator.validateAll("John Doe", pastDate, "invalid-email");
        assertFalse(result.isValid());
        assertEquals(FormValidator.Error.EMAIL_INVALID, result.error);
    }
}