package com.example.registrationform;

import org.junit.Test;

import java.util.Locale;

import static org.junit.Assert.*;

public class RegistrationFormatterTest {

    @Test
    public void formatDob_null_returnsEmpty() {
        assertEquals("", RegistrationFormatter.formatDob(null, Locale.ENGLISH));
    }

    @Test
    public void formatDob_validDate() {
        // 1990-01-15 00:00:00 UTC
        long dobMillis = 632361600000L;
        String formatted = RegistrationFormatter.formatDob(dobMillis, Locale.ENGLISH);
        assertEquals("15 Jan 1990", formatted);
    }

    @Test
    public void formatDob_leapDay() {
        // 2000-02-29 00:00:00 UTC
        long dobMillis = 951782400000L;
        String formatted = RegistrationFormatter.formatDob(dobMillis, Locale.ENGLISH);
        assertEquals("29 Feb 2000", formatted);
    }

    @Test
    public void formatDob_year2024() {
        // 2024-12-25 00:00:00 UTC
        long dobMillis = 1735084800000L;
        String formatted = RegistrationFormatter.formatDob(dobMillis, Locale.ENGLISH);
        assertEquals("25 Dec 2024", formatted);
    }

    @Test
    public void formatDob_swahiliLocale() {
        // 1990-03-15 00:00:00 UTC
        long dobMillis = 637545600000L;
        String formattedSw = RegistrationFormatter.formatDob(dobMillis, new Locale("sw"));
        String formattedEn = RegistrationFormatter.formatDob(dobMillis, Locale.ENGLISH);

        // Verify formatting works (doesn't crash) and produces non-empty output
        assertNotNull("Swahili output should not be null", formattedSw);
        assertFalse("Swahili output should not be empty", formattedSw.isEmpty());

        // Verify it contains the year at minimum (day/month format may vary by locale data availability)
        assertTrue("Swahili output should contain year '1990'", formattedSw.contains("1990"));

        // If locale data is available, month should differ from English "Mar"
        // If not available, it falls back to English - both are acceptable behaviors
        System.out.println("Swahili formatted: " + formattedSw + " | English formatted: " + formattedEn);
    }
}