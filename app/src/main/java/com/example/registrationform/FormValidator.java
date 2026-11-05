package com.example.registrationform;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.regex.Pattern;

/**
 * Pure Java validation logic for the registration form.
 * No Android dependencies - can be unit tested on the JVM.
 */
public final class FormValidator {

    private FormValidator() {}

    /** Minimum age in years for registration. */
    public static final int MIN_AGE_YEARS = 18;

    /** Validation error types. */
    public enum Error {
        NONE,
        NAME_REQUIRED,
        NAME_TOO_SHORT,
        NAME_INVALID_CHARS,
        DOB_REQUIRED,
        DOB_FUTURE,
        DOB_UNDER_AGE,
        EMAIL_REQUIRED,
        EMAIL_INVALID
    }

    /** Result of a validation check. */
    public static final class ValidationResult {
        public final Error error;

        private ValidationResult(Error error) {
            this.error = error;
        }

        public static ValidationResult valid() {
            return new ValidationResult(Error.NONE);
        }

        public static ValidationResult invalid(Error error) {
            return new ValidationResult(error);
        }

        public boolean isValid() {
            return error == Error.NONE;
        }
    }

    // Email regex pattern (RFC 5322 simplified)
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)+$"
    );

    /**
     * Validates the full name field.
     * Rules: required, trimmed, must contain only Unicode letters, spaces, hyphens, apostrophes and dots,
     * and must contain at least 2 letters.
     */
    public static ValidationResult validateName(String name) {
        if (name == null) {
            return ValidationResult.invalid(Error.NAME_REQUIRED);
        }
        String trimmed = name.trim();
        if (trimmed.isEmpty()) {
            return ValidationResult.invalid(Error.NAME_REQUIRED);
        }
        // Check for invalid characters: only Unicode letters, spaces, hyphens, apostrophes, dots allowed
        if (!trimmed.matches("[\\p{L}\\s\\-'.]+")) {
            return ValidationResult.invalid(Error.NAME_INVALID_CHARS);
        }
        // Count letters (must have at least 2)
        long letterCount = trimmed.codePoints().filter(Character::isLetter).count();
        if (letterCount < 2) {
            return ValidationResult.invalid(Error.NAME_TOO_SHORT);
        }
        return ValidationResult.valid();
    }

    /**
     * Validates the date of birth field.
     * Rules: required, not in the future (uses UTC for comparison), minimum age (18 years).
     * @param dobUtcMillis Date of birth in UTC milliseconds, or null if not selected.
     */
    public static ValidationResult validateDob(Long dobUtcMillis) {
        return validateDobForTesting(dobUtcMillis, LocalDate.now(ZoneOffset.UTC));
    }

    /**
     * Validates the date of birth field with a specific "today" date (for testing).
     * @param dobUtcMillis Date of birth in UTC milliseconds, or null if not selected.
     * @param today The reference date to compare against (in UTC).
     */
    static ValidationResult validateDobForTesting(Long dobUtcMillis, LocalDate today) {
        if (dobUtcMillis == null) {
            return ValidationResult.invalid(Error.DOB_REQUIRED);
        }
        // Convert UTC millis to LocalDate in UTC
        LocalDate dob = Instant.ofEpochMilli(dobUtcMillis).atOffset(ZoneOffset.UTC).toLocalDate();
        if (dob.isAfter(today)) {
            return ValidationResult.invalid(Error.DOB_FUTURE);
        }
        // Check minimum age: person must have had their 18th birthday on or before today
        // This correctly handles leap day birthdays (Feb 29 -> Feb 28 in non-leap years per Java's plusYears).
        // This is a deliberate lenient choice: a person born on Feb 29 legally turns 18 on Feb 28 in non-leap years.
        LocalDate eighteenthBirthday = dob.plusYears(MIN_AGE_YEARS);
        if (eighteenthBirthday.isAfter(today)) {
            return ValidationResult.invalid(Error.DOB_UNDER_AGE);
        }
        return ValidationResult.valid();
    }

    /**
     * Validates the email field.
     * Rules: required, valid format using regex (not android.util.Patterns).
     */
    public static ValidationResult validateEmail(String email) {
        if (email == null) {
            return ValidationResult.invalid(Error.EMAIL_REQUIRED);
        }
        String trimmed = email.trim();
        if (trimmed.isEmpty()) {
            return ValidationResult.invalid(Error.EMAIL_REQUIRED);
        }
        if (!EMAIL_PATTERN.matcher(trimmed).matches()) {
            return ValidationResult.invalid(Error.EMAIL_INVALID);
        }
        return ValidationResult.valid();
    }

    /**
     * Validates all fields at once. Returns the first error encountered,
     * or valid if all pass.
     */
    public static ValidationResult validateAll(String name, Long dobUtcMillis, String email) {
        ValidationResult nameResult = validateName(name);
        if (!nameResult.isValid()) {
            return nameResult;
        }
        ValidationResult dobResult = validateDob(dobUtcMillis);
        if (!dobResult.isValid()) {
            return dobResult;
        }
        ValidationResult emailResult = validateEmail(email);
        if (!emailResult.isValid()) {
            return emailResult;
        }
        return ValidationResult.valid();
    }
}