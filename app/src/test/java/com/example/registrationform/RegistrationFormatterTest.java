package com.example.registrationform;

import org.junit.Test;

import static org.junit.Assert.*;

public class RegistrationFormatterTest {

    @Test
    public void formatDob_null_returnsEmpty() {
        assertEquals("", RegistrationFormatter.formatDob(null));
    }

    @Test
    public void formatDob_validDate() {
        // 1990-01-15 00:00:00 UTC
        long dobMillis = 632361600000L;
        String formatted = RegistrationFormatter.formatDob(dobMillis);
        assertEquals("15 Jan 1990", formatted);
    }

    @Test
    public void formatDob_leapDay() {
        // 2000-02-29 00:00:00 UTC
        long dobMillis = 951782400000L;
        String formatted = RegistrationFormatter.formatDob(dobMillis);
        assertEquals("29 Feb 2000", formatted);
    }

    @Test
    public void formatDob_year2024() {
        // 2024-12-25 00:00:00 UTC
        long dobMillis = 1735084800000L;
        String formatted = RegistrationFormatter.formatDob(dobMillis);
        assertEquals("25 Dec 2024", formatted);
    }
}