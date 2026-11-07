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
     * Formats a date of birth (UTC millis) as "dd MMM yyyy" in UTC using the given locale.
     * @param dobUtcMillis Date of birth in UTC milliseconds, or null.
     * @param locale The locale to use for month names.
     * @return Formatted date string, or empty string if null.
     */
    public static String formatDob(Long dobUtcMillis, Locale locale) {
        if (dobUtcMillis == null) {
            return "";
        }
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", locale);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(dobUtcMillis);
    }
}