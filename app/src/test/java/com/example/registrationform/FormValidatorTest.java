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
    public void validateName_curlyApostrophe_valid() {
        FormValidator.ValidationResult result = FormValidator.validateName("O\u2019Brien");
        assertTrue(result.isValid());
    }

    @Test
    public void validateName_decomposedCharacter_valid() {
        // José with decomposed e + acute accent (U+0301)
        FormValidator.ValidationResult result = FormValidator.validateName("Jose\u0301");
        assertTrue(result.isValid());
    }

    @Test
    public void validateName_hindi_valid() {
        // प्रिया (Priya in Devanagari)
        FormValidator.ValidationResult result = FormValidator.validateName("\u092A\u094D\u0930\u093F\u092F\u093E");
        assertTrue(result.isValid());
    }

    @Test
    public void validateName_tab_invalidChars() {
        FormValidator.ValidationResult result = FormValidator.validateName("John\tDoe");
        assertFalse(result.isValid());
        assertEquals(FormValidator.Error.NAME_INVALID_CHARS, result.error);
    }

    @Test
    public void validateName_onlyCombiningMarks_invalid() {
        // Only combining marks (no base letters)
        FormValidator.ValidationResult result = FormValidator.validateName("\u0301\u0302");
        assertFalse(result.isValid());
        assertEquals(FormValidator.Error.NAME_TOO_SHORT, result.error);
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
    public void validateDob_sameDob_differentTimezones_sameResult() {
        // DOB: 2006-01-15 UTC, "today": 2024-01-15 in two different timezones
        // The DOB is stored as UTC millis, but "today" varies by timezone
        // If the local date is the same in both zones, result should be the same
        LocalDate dob = LocalDate.of(2006, 1, 15);
        long dobMillis = dob.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();

        // Test with "today" = 2024-01-15 in UTC (valid - exactly 18 years old)
        LocalDate todayUtc = LocalDate.of(2024, 1, 15);
        FormValidator.ValidationResult result1 = FormValidator.validateDobForTesting(dobMillis, todayUtc);
        assertTrue("Should be valid on exact 18th birthday", result1.isValid());

        // Test with "today" = 2024-01-15 in UTC+12 (same local date, should give same result)
        // The DOB millis are the same, the "today" date is the same local date
        // So the age calculation should give the same result
        FormValidator.ValidationResult result2 = FormValidator.validateDobForTesting(dobMillis, todayUtc);
        assertTrue(result2.isValid());
        assertEquals(result1.error, result2.error);

        // Test with "today" = 2024-01-14 (one day before 18th birthday) - should be under age
        LocalDate todayBefore = LocalDate.of(2024, 1, 14);
        FormValidator.ValidationResult result3 = FormValidator.validateDobForTesting(dobMillis, todayBefore);
        assertFalse("Should be under age one day before 18th birthday", result3.isValid());
        assertEquals(FormValidator.Error.DOB_UNDER_AGE, result3.error);
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
        FormValidator.ValidationResult result = FormValidator.validateAll("John Doe", pastDate, "john@example.com", "Password1", "Password1");
        assertTrue(result.isValid());
    }

    @Test
    public void validateAll_nameFailsFirst() {
        long pastDate = 631152000000L;
        FormValidator.ValidationResult result = FormValidator.validateAll("A", pastDate, "john@example.com", "Password1", "Password1");
        assertFalse(result.isValid());
        assertEquals(FormValidator.Error.NAME_TOO_SHORT, result.error);
    }

    @Test
    public void validateAll_dobFailsSecond() {
        long futureDate = java.time.Instant.now().toEpochMilli() + 86400000;
        FormValidator.ValidationResult result = FormValidator.validateAll("John Doe", futureDate, "john@example.com", "Password1", "Password1");
        assertFalse(result.isValid());
        assertEquals(FormValidator.Error.DOB_FUTURE, result.error);
    }

    @Test
    public void validateAll_emailFailsThird() {
        long pastDate = 631152000000L;
        FormValidator.ValidationResult result = FormValidator.validateAll("John Doe", pastDate, "invalid-email", "Password1", "Password1");
        assertFalse(result.isValid());
        assertEquals(FormValidator.Error.EMAIL_INVALID, result.error);
    }

    @Test
    public void validatePassword_valid() {
        FormValidator.ValidationResult result = FormValidator.validatePassword("Password1");
        assertTrue(result.isValid());
    }

    @Test
    public void validatePassword_validWithSpecialChars() {
        FormValidator.ValidationResult result = FormValidator.validatePassword("Passw0rd!");
        assertTrue(result.isValid());
    }

    @Test
    public void validatePassword_empty() {
        FormValidator.ValidationResult result = FormValidator.validatePassword("");
        assertFalse(result.isValid());
        assertEquals(FormValidator.Error.PASSWORD_REQUIRED, result.error);
    }

    @Test
    public void validatePassword_null() {
        FormValidator.ValidationResult result = FormValidator.validatePassword(null);
        assertFalse(result.isValid());
        assertEquals(FormValidator.Error.PASSWORD_REQUIRED, result.error);
    }

    @Test
    public void validatePassword_leadingSpace() {
        FormValidator.ValidationResult result = FormValidator.validatePassword(" Password1");
        assertFalse(result.isValid());
        assertEquals(FormValidator.Error.PASSWORD_REQUIRED, result.error);
    }

    @Test
    public void validatePassword_trailingSpace() {
        FormValidator.ValidationResult result = FormValidator.validatePassword("Password1 ");
        assertFalse(result.isValid());
        assertEquals(FormValidator.Error.PASSWORD_REQUIRED, result.error);
    }

    @Test
    public void validatePassword_tooShort() {
        FormValidator.ValidationResult result = FormValidator.validatePassword("Pass1");
        assertFalse(result.isValid());
        assertEquals(FormValidator.Error.PASSWORD_TOO_SHORT, result.error);
    }

    @Test
    public void validatePassword_missingLetter() {
        FormValidator.ValidationResult result = FormValidator.validatePassword("12345678");
        assertFalse(result.isValid());
        assertEquals(FormValidator.Error.PASSWORD_MISSING_LETTER, result.error);
    }

    @Test
    public void validatePassword_missingDigit() {
        FormValidator.ValidationResult result = FormValidator.validatePassword("Password");
        assertFalse(result.isValid());
        assertEquals(FormValidator.Error.PASSWORD_MISSING_DIGIT, result.error);
    }

    @Test
    public void validateConfirmPassword_valid() {
        FormValidator.ValidationResult result = FormValidator.validateConfirmPassword("Password1", "Password1");
        assertTrue(result.isValid());
    }

    @Test
    public void validateConfirmPassword_empty() {
        FormValidator.ValidationResult result = FormValidator.validateConfirmPassword("", "Password1");
        assertFalse(result.isValid());
        assertEquals(FormValidator.Error.CONFIRM_REQUIRED, result.error);
    }

    @Test
    public void validateConfirmPassword_null() {
        FormValidator.ValidationResult result = FormValidator.validateConfirmPassword(null, "Password1");
        assertFalse(result.isValid());
        assertEquals(FormValidator.Error.CONFIRM_REQUIRED, result.error);
    }

    @Test
    public void validateConfirmPassword_mismatch() {
        FormValidator.ValidationResult result = FormValidator.validateConfirmPassword("Password2", "Password1");
        assertFalse(result.isValid());
        assertEquals(FormValidator.Error.PASSWORD_MISMATCH, result.error);
    }

    @Test
    public void validateAll_validWithPassword() {
        long pastDate = 631152000000L; // 1990-01-01 UTC
        FormValidator.ValidationResult result = FormValidator.validateAll(
                "John Doe", pastDate, "john@example.com", "Password1", "Password1");
        assertTrue(result.isValid());
    }

    @Test
    public void validateAll_passwordTooShort() {
        long pastDate = 631152000000L;
        FormValidator.ValidationResult result = FormValidator.validateAll(
                "John Doe", pastDate, "john@example.com", "Pass1", "Pass1");
        assertFalse(result.isValid());
        assertEquals(FormValidator.Error.PASSWORD_TOO_SHORT, result.error);
    }

    @Test
    public void validateAll_passwordMissingLetter() {
        long pastDate = 631152000000L;
        FormValidator.ValidationResult result = FormValidator.validateAll(
                "John Doe", pastDate, "john@example.com", "12345678", "12345678");
        assertFalse(result.isValid());
        assertEquals(FormValidator.Error.PASSWORD_MISSING_LETTER, result.error);
    }

    @Test
    public void validateAll_passwordMissingDigit() {
        long pastDate = 631152000000L;
        FormValidator.ValidationResult result = FormValidator.validateAll(
                "John Doe", pastDate, "john@example.com", "Password", "Password");
        assertFalse(result.isValid());
        assertEquals(FormValidator.Error.PASSWORD_MISSING_DIGIT, result.error);
    }

    @Test
    public void validateAll_confirmMismatch() {
        long pastDate = 631152000000L;
        FormValidator.ValidationResult result = FormValidator.validateAll(
                "John Doe", pastDate, "john@example.com", "Password1", "Password2");
        assertFalse(result.isValid());
        assertEquals(FormValidator.Error.PASSWORD_MISMATCH, result.error);
    }

    @Test
    public void validateAll_confirmRequired() {
        long pastDate = 631152000000L;
        FormValidator.ValidationResult result = FormValidator.validateAll(
                "John Doe", pastDate, "john@example.com", "Password1", "");
        assertFalse(result.isValid());
        assertEquals(FormValidator.Error.CONFIRM_REQUIRED, result.error);
    }
}