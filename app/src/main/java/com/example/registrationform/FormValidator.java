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

    /** Validation error types. */
    public enum Error {
        NONE,
        NAME_REQUIRED,
        NAME_TOO_SHORT,
        DOB_REQUIRED,
        DOB_FUTURE,
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
     * Rules: required, trimmed, minimum 2 characters.
     */
    public static ValidationResult validateName(String name) {
        if (name == null) {
            return ValidationResult.invalid(Error.NAME_REQUIRED);
        }
        String trimmed = name.trim();
        if (trimmed.isEmpty()) {
            return ValidationResult.invalid(Error.NAME_REQUIRED);
        }
        if (trimmed.length() < 2) {
            return ValidationResult.invalid(Error.NAME_TOO_SHORT);
        }
        return ValidationResult.valid();
    }

    /**
     * Validates the date of birth field.
     * Rules: required, not in the future (uses UTC for comparison).
     * @param dobUtcMillis Date of birth in UTC milliseconds, or null if not selected.
     */
    public static ValidationResult validateDob(Long dobUtcMillis) {
        if (dobUtcMillis == null) {
            return ValidationResult.invalid(Error.DOB_REQUIRED);
        }
        // Convert UTC millis to LocalDate in UTC
        LocalDate dob = Instant.ofEpochMilli(dobUtcMillis).atOffset(ZoneOffset.UTC).toLocalDate();
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        if (dob.isAfter(today)) {
            return ValidationResult.invalid(Error.DOB_FUTURE);
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