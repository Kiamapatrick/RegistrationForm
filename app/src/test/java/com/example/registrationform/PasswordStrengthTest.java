package com.example.registrationform;

import org.junit.Test;

import static org.junit.Assert.*;

public class PasswordStrengthTest {

    @Test
    public void evaluate_empty_weak() {
        assertEquals(PasswordStrength.Level.WEAK, PasswordStrength.evaluate(""));
    }

    @Test
    public void evaluate_null_weak() {
        assertEquals(PasswordStrength.Level.WEAK, PasswordStrength.evaluate(null));
    }

    @Test
    public void evaluate_short_weak() {
        assertEquals(PasswordStrength.Level.WEAK, PasswordStrength.evaluate("abc"));
    }

    @Test
    public void evaluate_8charsOnlyLowercase_weak() {
        assertEquals(PasswordStrength.Level.WEAK, PasswordStrength.evaluate("password"));
    }

    @Test
    public void evaluate_8charsMixedCase_fair() {
        assertEquals(PasswordStrength.Level.FAIR, PasswordStrength.evaluate("Password"));
    }

    @Test
    public void evaluate_8charsWithDigit_fair() {
        assertEquals(PasswordStrength.Level.FAIR, PasswordStrength.evaluate("passwor1"));
    }

    @Test
    public void evaluate_8charsWithSpecial_fair() {
        assertEquals(PasswordStrength.Level.FAIR, PasswordStrength.evaluate("passwor!"));
    }

    @Test
    public void evaluate_8charsUpperLowerDigit_fair() {
        assertEquals(PasswordStrength.Level.FAIR, PasswordStrength.evaluate("Passwor1"));
    }

    @Test
    public void evaluate_12charsMixed_fair() {
        assertEquals(PasswordStrength.Level.FAIR, PasswordStrength.evaluate("Password1234"));
    }

    @Test
    public void evaluate_allFourTypes_strong() {
        assertEquals(PasswordStrength.Level.STRONG, PasswordStrength.evaluate("Passw0rd!"));
    }

    @Test
    public void evaluate_longPassword_strong() {
        assertEquals(PasswordStrength.Level.STRONG, PasswordStrength.evaluate("VeryLongPassword123!"));
    }

    @Test
    public void progress_empty_zero() {
        assertEquals(0, PasswordStrength.progress(""));
    }

    @Test
    public void progress_weak_25() {
        assertEquals(25, PasswordStrength.progress("password"));
    }

    @Test
    public void progress_fair_60() {
        assertEquals(60, PasswordStrength.progress("Password1"));
    }

    @Test
    public void progress_strong_100() {
        assertEquals(100, PasswordStrength.progress("Password123!"));
    }
}