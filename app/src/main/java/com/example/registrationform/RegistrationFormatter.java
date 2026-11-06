package com.example.registrationform;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Pure Java formatting utilities for the registration summary.
 * No Android dependencies - can be unit tested on the JVM.
 */
public final class RegistrationFormatter {

    private RegistrationFormatter() {}

    /**
     * Formats a date of birth (UTC millis) as "dd MMM yyyy" in UTC.
     * @param dobUtcMillis Date of birth in UTC milliseconds, or null.
     * @return Formatted date string, or empty string if null.
     */
    public static String formatDob(Long dobUtcMillis) {
        if (dobUtcMillis == null) {
            return "";
        }
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(dobUtcMillis);
    }
}