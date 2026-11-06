package com.example.registrationform;

import java.text.Normalizer;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Set;
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
        EMAIL_INVALID,
        PASSWORD_REQUIRED,
        PASSWORD_TOO_SHORT,
        PASSWORD_MISSING_LETTER,
        PASSWORD_MISSING_DIGIT,
        PASSWORD_HAS_EDGE_SPACES,
        PASSWORD_TOO_COMMON,
        PASSWORD_MISMATCH,
        CONFIRM_REQUIRED
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

    // Common passwords to reject (case-insensitive)
    private static final Set<String> COMMON_PASSWORDS = Set.of(
            "password1", "12345678a", "qwerty123", "abc12345", "iloveyou1",
            "password", "12345678", "qwerty", "abc123", "letmein",
            "welcome", "admin", "password123", "123456789", "sunshine",
            "princess", "football", "monkey", "dragon", "master",
            "hello", "superman", "batman", "trustno1", "whatever"
    );

    /**
     * Validates the full name field.
     * Rules: required, trimmed, normalized to NFC, must contain only Unicode letters, combining marks,
     * spaces, hyphens, ASCII apostrophe, typographic apostrophe (U+2019), and dots,
     * and must contain at least 2 letters (combining marks don't count).
     */
    public static ValidationResult validateName(String name) {
        if (name == null) {
            return ValidationResult.invalid(Error.NAME_REQUIRED);
        }
        String trimmed = name.trim();
        if (trimmed.isEmpty()) {
            return ValidationResult.invalid(Error.NAME_REQUIRED);
        }
        // Normalize to NFC to handle decomposed characters
        String normalized = Normalizer.normalize(trimmed, Normalizer.Form.NFC);
        // Check for invalid characters: Unicode letters, combining marks, space, hyphen, apostrophes, dot
        if (!normalized.matches("[\\p{L}\\p{M} \\-'\\u2019.]+")) {
            return ValidationResult.invalid(Error.NAME_INVALID_CHARS);
        }
        // Count letters only (not combining marks) - must have at least 2
        long letterCount = normalized.codePoints().filter(c -> Character.isLetter(c) && !Character.isSpaceChar(c)).count();
        // Better: count only \p{L} category (letters, not marks)
        long letterCount2 = 0;
        for (int i = 0; i < normalized.length(); ) {
            int cp = normalized.codePointAt(i);
            if (Character.getType(cp) == Character.UPPERCASE_LETTER ||
                Character.getType(cp) == Character.LOWERCASE_LETTER ||
                Character.getType(cp) == Character.TITLECASE_LETTER ||
                Character.getType(cp) == Character.MODIFIER_LETTER ||
                Character.getType(cp) == Character.OTHER_LETTER) {
                letterCount2++;
            }
            i += Character.charCount(cp);
        }
        if (letterCount2 < 2) {
            return ValidationResult.invalid(Error.NAME_TOO_SHORT);
        }
        return ValidationResult.valid();
    }

    /**
     * Validates the date of birth field.
     * Rules: required, not in the future (uses device local date for comparison), minimum age (18 years).
     * @param dobUtcMillis Date of birth in UTC milliseconds, or null if not selected.
     */
    public static ValidationResult validateDob(Long dobUtcMillis) {
        // Use device's local date for "today" comparison
        return validateDobForTesting(dobUtcMillis, LocalDate.now(ZoneId.systemDefault()));
    }

    /**
     * Validates the date of birth field with a specific "today" date (for testing).
     * @param dobUtcMillis Date of birth in UTC milliseconds, or null if not selected.
     * @param today The reference date to compare against (in the local timezone of the test scenario).
     */
    static ValidationResult validateDobForTesting(Long dobUtcMillis, LocalDate today) {
        if (dobUtcMillis == null) {
            return ValidationResult.invalid(Error.DOB_REQUIRED);
        }
        // Convert UTC millis to LocalDate in UTC (the picker stores UTC midnight)
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
     * Validates the password field.
     * Rules: required, minimum 8 characters, at least one letter and one digit,
     * no leading or trailing spaces, not a common password.
     */
    public static ValidationResult validatePassword(String password) {
        if (password == null) {
            return ValidationResult.invalid(Error.PASSWORD_REQUIRED);
        }
        if (password.length() != password.trim().length()) {
            return ValidationResult.invalid(Error.PASSWORD_HAS_EDGE_SPACES);
        }
        if (password.isEmpty()) {
            return ValidationResult.invalid(Error.PASSWORD_REQUIRED);
        }
        if (password.length() < 8) {
            return ValidationResult.invalid(Error.PASSWORD_TOO_SHORT);
        }
        boolean hasLetter = false;
        boolean hasDigit = false;
        for (char c : password.toCharArray()) {
            if (Character.isLetter(c)) hasLetter = true;
            if (Character.isDigit(c)) hasDigit = true;
        }
        if (!hasLetter) {
            return ValidationResult.invalid(Error.PASSWORD_MISSING_LETTER);
        }
        if (!hasDigit) {
            return ValidationResult.invalid(Error.PASSWORD_MISSING_DIGIT);
        }
        // Check against common passwords (case-insensitive)
        if (COMMON_PASSWORDS.contains(password.toLowerCase())) {
            return ValidationResult.invalid(Error.PASSWORD_TOO_COMMON);
        }
        return ValidationResult.valid();
    }

    /**
     * Validates the confirm password field.
     * Rules: required, must match password.
     */
    public static ValidationResult validateConfirmPassword(String confirmPassword, String password) {
        if (confirmPassword == null) {
            return ValidationResult.invalid(Error.CONFIRM_REQUIRED);
        }
        if (confirmPassword.isEmpty()) {
            return ValidationResult.invalid(Error.CONFIRM_REQUIRED);
        }
        if (!confirmPassword.equals(password)) {
            return ValidationResult.invalid(Error.PASSWORD_MISMATCH);
        }
        return ValidationResult.valid();
    }

    /**
     * Validates all fields at once. Returns the first error encountered,
     * or valid if all pass.
     */
    public static ValidationResult validateAll(String name, Long dobUtcMillis, String email,
                                               String password, String confirmPassword) {
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
        ValidationResult passwordResult = validatePassword(password);
        if (!passwordResult.isValid()) {
            return passwordResult;
        }
        ValidationResult confirmResult = validateConfirmPassword(confirmPassword, password);
        if (!confirmResult.isValid()) {
            return confirmResult;
        }
        return ValidationResult.valid();
    }
}