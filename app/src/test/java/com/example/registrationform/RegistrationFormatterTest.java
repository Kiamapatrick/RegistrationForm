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
        // 1990-01-15 00:00:00 UTC
        long dobMillis = 632361600000L;
        String formatted = RegistrationFormatter.formatDob(dobMillis, new Locale("sw"));
        // Swahili month for January is "Jan" (or "Januari")
        // Just verify it formats without crashing and contains the day and year
        assertTrue(formatted.contains("15"));
        assertTrue(formatted.contains("1990"));
    }
}