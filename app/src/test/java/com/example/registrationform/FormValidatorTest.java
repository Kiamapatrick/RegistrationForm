package com.example.registrationform;

import org.junit.Test;

import java.time.LocalDate;
import java.time.ZoneOffset;

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
    public void validateName_validWithHyphen() {
        FormValidator.ValidationResult result = FormValidator.validateName("Jean-Luc");
        assertTrue(result.isValid());
    }

    @Test
    public void validateName_validWithApostrophe() {
        FormValidator.ValidationResult result = FormValidator.validateName("O'Brien");
        assertTrue(result.isValid());
    }

    @Test
    public void validateName_validWithMultipleSpaces() {
        FormValidator.ValidationResult result = FormValidator.validateName("Wanjiru Kamau");
        assertTrue(result.isValid());
    }

    @Test
    public void validateName_validWithUnicodeLetter() {
        FormValidator.ValidationResult result = FormValidator.validateName("Müller");
        assertTrue(result.isValid());
    }

    @Test
    public void validateName_emoji_invalidChars() {
        FormValidator.ValidationResult result = FormValidator.validateName("John 😀");
        assertFalse(result.isValid());
        assertEquals(FormValidator.Error.NAME_INVALID_CHARS, result.error);
    }

    @Test
    public void validateName_digits_invalidChars() {
        FormValidator.ValidationResult result = FormValidator.validateName("John123");
        assertFalse(result.isValid());
        assertEquals(FormValidator.Error.NAME_INVALID_CHARS, result.error);
    }

    @Test
    public void validateName_singleLetter_tooShort() {
        FormValidator.ValidationResult result = FormValidator.validateName("A");
        assertFalse(result.isValid());
        assertEquals(FormValidator.Error.NAME_TOO_SHORT, result.error);
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
        // Today is not a valid DOB for someone 18+ (would be 0 years old)
        long today = java.time.Instant.now().toEpochMilli();
        FormValidator.ValidationResult result = FormValidator.validateDob(today);
        assertFalse(result.isValid());
        assertEquals(FormValidator.Error.DOB_UNDER_AGE, result.error);
    }

    @Test
    public void validateDob_exact18thBirthday_isValid() {
        // Fixed date: 2024-01-15 -> 18th birthday is 2042-01-15
        LocalDate today = LocalDate.of(2042, 1, 15);
        LocalDate dob = today.minusYears(18); // 2024-01-15
        long dobMillis = dob.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();
        FormValidator.ValidationResult result = FormValidator.validateDobForTesting(dobMillis, today);
        assertTrue("Should be valid on exact 18th birthday", result.isValid());
    }

    @Test
    public void validateDob_oneDayShortOf18_isUnderAge() {
        // Fixed date: 2024-01-15 -> one day short is 2024-01-16
        LocalDate today = LocalDate.of(2042, 1, 15);
        LocalDate dob = today.minusYears(18).plusDays(1); // 2024-01-16
        long dobMillis = dob.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();
        FormValidator.ValidationResult result = FormValidator.validateDobForTesting(dobMillis, today);
        assertFalse("Should be invalid - one day short of 18", result.isValid());
        assertEquals(FormValidator.Error.DOB_UNDER_AGE, result.error);
    }

    @Test
    public void validateDob_leapDayBirthday_nonLeapYear() {
        // Born on Feb 29, 2004 (leap year) - Java's plusYears(18) gives Feb 28, 2022 (non-leap year)
        // So on Feb 28, 2022 they turn 18, on Feb 27, 2022 they are still 17
        LocalDate dob = LocalDate.of(2004, 2, 29);
        long dobMillis = dob.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();
        
        // Test on Feb 27, 2022 - still 17 (18th birthday is Feb 28, 2022)
        LocalDate feb27_2022 = LocalDate.of(2022, 2, 27);
        FormValidator.ValidationResult result1 = FormValidator.validateDobForTesting(dobMillis, feb27_2022);
        assertFalse("Should be under age on Feb 27, 2022 (born Feb 29, 2004)", result1.isValid());
        assertEquals(FormValidator.Error.DOB_UNDER_AGE, result1.error);
        
        // Test on Feb 28, 2022 - turns 18
        LocalDate feb28_2022 = LocalDate.of(2022, 2, 28);
        FormValidator.ValidationResult result2 = FormValidator.validateDobForTesting(dobMillis, feb28_2022);
        assertTrue("Should be valid on Feb 28, 2022 (born Feb 29, 2004)", result2.isValid());
    }

    @Test
    public void validateDob_leapDayBirthday_leapYear() {
        // Born on Feb 29, 2008 (leap year) - Java's plusYears(18) gives Feb 28, 2026 (non-leap year)
        // So on Feb 28, 2026 they turn 18, on Feb 27, 2026 they are still 17
        LocalDate dob = LocalDate.of(2008, 2, 29);
        long dobMillis = dob.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();
        
        // Test on Feb 27, 2026 - still 17
        LocalDate feb27_2026 = LocalDate.of(2026, 2, 27);
        FormValidator.ValidationResult result1 = FormValidator.validateDobForTesting(dobMillis, feb27_2026);
        assertFalse("Should be under age on Feb 27, 2026 (born Feb 29, 2008)", result1.isValid());
        assertEquals(FormValidator.Error.DOB_UNDER_AGE, result1.error);
        
        // Test on Feb 28, 2026 - turns 18
        LocalDate feb28_2026 = LocalDate.of(2026, 2, 28);
        FormValidator.ValidationResult result2 = FormValidator.validateDobForTesting(dobMillis, feb28_2026);
        assertTrue("Should be valid on Feb 28, 2026 (born Feb 29, 2008)", result2.isValid());
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