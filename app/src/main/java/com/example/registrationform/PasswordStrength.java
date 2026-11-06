package com.example.registrationform;

/**
 * Pure Java class for evaluating password strength.
 * No Android dependencies - can be unit tested on the JVM.
 */
public final class PasswordStrength {

    private PasswordStrength() {}

    public enum Level {
        WEAK(0, 30),
        FAIR(31, 60),
        STRONG(61, 100);

        public final int minScore;
        public final int maxScore;

        Level(int minScore, int maxScore) {
            this.minScore = minScore;
            this.maxScore = maxScore;
        }

        public static Level fromScore(int score) {
            if (score <= WEAK.maxScore) return WEAK;
            if (score <= FAIR.maxScore) return FAIR;
            return STRONG;
        }
    }

    /**
     * Evaluates the strength of a password.
     * @param password The password to evaluate.
     * @return A Level indicating the strength.
     */
    public static Level evaluate(String password) {
        if (password == null || password.isEmpty()) {
            return Level.WEAK;
        }
        int score = 0;

        // Length: up to 25 points (8 chars = 8, 16 chars = 16, max 25)
        score += Math.min(password.length(), 25);

        // Character variety
        boolean hasLower = false;
        boolean hasUpper = false;
        boolean hasDigit = false;
        boolean hasSpecial = false;

        for (char c : password.toCharArray()) {
            if (c >= 'a' && c <= 'z') hasLower = true;
            else if (c >= 'A' && c <= 'Z') hasUpper = true;
            else if (c >= '0' && c <= '9') hasDigit = true;
            else hasSpecial = true;
        }

        if (hasLower) score += 10;
        if (hasUpper) score += 10;
        if (hasDigit) score += 15;
        if (hasSpecial) score += 15;

        // Bonus for mixed case
        if (hasLower && hasUpper) score += 5;
        // Bonus for length > 12
        if (password.length() > 12) score += 5;
        // Bonus for all four types
        if (hasLower && hasUpper && hasDigit && hasSpecial) score += 10;

        // Cap at 100
        score = Math.min(score, 100);

        return Level.fromScore(score);
    }

    /**
     * Returns a progress value (0-100) for the strength indicator.
     */
    public static int progress(String password) {
        if (password == null || password.isEmpty()) return 0;
        Level level = evaluate(password);
        // Map to progress: WEAK=25, FAIR=60, STRONG=100
        switch (level) {
            case WEAK: return 25;
            case FAIR: return 60;
            case STRONG: return 100;
            default: return 0;
        }
    }

    }