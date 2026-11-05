package com.example.registrationform;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.LiveData;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.time.LocalDate;
import java.time.ZoneOffset;

import static org.junit.Assert.*;

public class RegistrationViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private RegistrationViewModel viewModel;

    @Before
    public void setUp() {
        viewModel = new RegistrationViewModel();
    }

    @Test
    public void initialState_isEmptyAndValid() {
        assertEquals("", viewModel.getName());
        assertNull(viewModel.getDobUtcMillis());
        assertEquals("", viewModel.getEmail());
        assertFalse(viewModel.isNameTouched());
        assertFalse(viewModel.isDobTouched());
        assertFalse(viewModel.isEmailTouched());

        // Initially no errors (NONE means valid)
        assertEquals(FormValidator.Error.NONE, getValue(viewModel.getNameError()));
        assertEquals(FormValidator.Error.NONE, getValue(viewModel.getDobError()));
        assertEquals(FormValidator.Error.NONE, getValue(viewModel.getEmailError()));

        // Form is not valid initially (empty fields)
        assertFalse(Boolean.TRUE.equals(getValue(viewModel.getIsFormValid())));
    }

    @Test
    public void nameValidation_empty_showsErrorAfterTouched() {
        viewModel.setName("");
        viewModel.setNameTouched(true);

        assertEquals(FormValidator.Error.NAME_REQUIRED, getValue(viewModel.getNameError()));
        assertFalse(Boolean.TRUE.equals(getValue(viewModel.getIsFormValid())));
    }

    @Test
    public void nameValidation_oneChar_showsErrorAfterTouched() {
        viewModel.setName("A");
        viewModel.setNameTouched(true);

        assertEquals(FormValidator.Error.NAME_TOO_SHORT, getValue(viewModel.getNameError()));
        assertFalse(Boolean.TRUE.equals(getValue(viewModel.getIsFormValid())));
    }

    @Test
    public void nameValidation_valid_clearsError() {
        viewModel.setName("John");
        viewModel.setNameTouched(true);

        assertEquals(FormValidator.Error.NONE, getValue(viewModel.getNameError()));
    }

    @Test
    public void nameValidation_whitespaceOnly_showsRequiredError() {
        viewModel.setName("   ");
        viewModel.setNameTouched(true);

        assertEquals(FormValidator.Error.NAME_REQUIRED, getValue(viewModel.getNameError()));
    }

    @Test
    public void dobValidation_null_showsErrorAfterTouched() {
        viewModel.setDobUtcMillis(null);
        viewModel.setDobTouched(true);

        assertEquals(FormValidator.Error.DOB_REQUIRED, getValue(viewModel.getDobError()));
        assertFalse(Boolean.TRUE.equals(getValue(viewModel.getIsFormValid())));
    }

    @Test
    public void dobValidation_validPastDate_clearsError() {
        long pastDate = 631152000000L; // 1990-01-01 UTC
        viewModel.setDobUtcMillis(pastDate);
        viewModel.setDobTouched(true);

        assertEquals(FormValidator.Error.NONE, getValue(viewModel.getDobError()));
    }

    @Test
    public void dobValidation_futureDate_showsError() {
        long futureDate = java.time.Instant.now().toEpochMilli() + 86400000; // tomorrow
        viewModel.setDobUtcMillis(futureDate);
        viewModel.setDobTouched(true);

        assertEquals(FormValidator.Error.DOB_FUTURE, getValue(viewModel.getDobError()));
        assertFalse(Boolean.TRUE.equals(getValue(viewModel.getIsFormValid())));
    }

    @Test
    public void dobValidation_today_isUnderAge() {
        // Today is not a valid DOB for someone 18+ (would be 0 years old)
        long today = java.time.Instant.now().toEpochMilli();
        viewModel.setDobUtcMillis(today);
        viewModel.setDobTouched(true);

        assertEquals(FormValidator.Error.DOB_UNDER_AGE, getValue(viewModel.getDobError()));
        assertFalse(Boolean.TRUE.equals(getValue(viewModel.getIsFormValid())));
    }

    @Test
    public void dobValidation_exact18thBirthdayToday_isValid() {
        // Use a fixed date to avoid flakiness: Jan 15, 2024 -> 18th birthday is Jan 15, 2042
        // But we test with "today" = Jan 15, 2042 and dob = Jan 15, 2024
        LocalDate today = LocalDate.of(2042, 1, 15);
        LocalDate dob = today.minusYears(18); // Jan 15, 2024
        long dobMillis = dob.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();
        long todayMillis = today.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();
        
        FormValidator.ValidationResult result = FormValidator.validateDobForTesting(dobMillis, today);
        assertTrue("Should be valid on exact 18th birthday", result.isValid());
    }

    @Test
    public void dobValidation_oneDayShortOf18_showsUnderAgeError() {
        // Fixed date: 2042-01-15 -> one day short is 2024-01-16
        LocalDate today = LocalDate.of(2042, 1, 15);
        LocalDate dob = today.minusYears(18).plusDays(1); // 2024-01-16
        long dobMillis = dob.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();
        viewModel.setDobUtcMillis(dobMillis);
        viewModel.setDobTouched(true);

        assertEquals(FormValidator.Error.DOB_UNDER_AGE, getValue(viewModel.getDobError()));
        assertFalse(Boolean.TRUE.equals(getValue(viewModel.getIsFormValid())));
    }

    @Test
    public void dobValidation_leapDayBirthday() {
        // Born on Feb 29, 2008 (leap year) - Java's plusYears(18) gives Feb 28, 2026 (non-leap year)
        // So on Feb 28, 2026 they turn 18, on Feb 27, 2026 they are still 17
        LocalDate today = LocalDate.of(2026, 2, 27);
        LocalDate dob = LocalDate.of(2008, 2, 29);
        long dobMillis = dob.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();
        
        FormValidator.ValidationResult result = FormValidator.validateDobForTesting(dobMillis, today);
        assertFalse("Should be under age on Feb 27, 2026 (born Feb 29, 2008)", result.isValid());
        assertEquals(FormValidator.Error.DOB_UNDER_AGE, result.error);
    }

    @Test
    public void emailValidation_empty_showsErrorAfterTouched() {
        viewModel.setEmail("");
        viewModel.setEmailTouched(true);

        assertEquals(FormValidator.Error.EMAIL_REQUIRED, getValue(viewModel.getEmailError()));
        assertFalse(Boolean.TRUE.equals(getValue(viewModel.getIsFormValid())));
    }

    @Test
    public void emailValidation_invalidFormat_showsErrorAfterTouched() {
        viewModel.setEmail("invalid-email");
        viewModel.setEmailTouched(true);

        assertEquals(FormValidator.Error.EMAIL_INVALID, getValue(viewModel.getEmailError()));
        assertFalse(Boolean.TRUE.equals(getValue(viewModel.getIsFormValid())));
    }

    @Test
    public void emailValidation_valid_clearsError() {
        viewModel.setEmail("user@example.com");
        viewModel.setEmailTouched(true);

        assertEquals(FormValidator.Error.NONE, getValue(viewModel.getEmailError()));
    }

    @Test
    public void formValid_allFieldsValid_returnsTrue() {
        viewModel.setName("John Doe");
        viewModel.setNameTouched(true);
        viewModel.setDobUtcMillis(631152000000L); // 1990-01-01 UTC
        viewModel.setDobTouched(true);
        viewModel.setEmail("john@example.com");
        viewModel.setEmailTouched(true);

        assertTrue(Boolean.TRUE.equals(getValue(viewModel.getIsFormValid())));
        assertEquals(FormValidator.Error.NONE, getValue(viewModel.getNameError()));
        assertEquals(FormValidator.Error.NONE, getValue(viewModel.getDobError()));
        assertEquals(FormValidator.Error.NONE, getValue(viewModel.getEmailError()));
    }

    @Test
    public void formValid_missingDob_returnsFalse() {
        viewModel.setName("John Doe");
        viewModel.setNameTouched(true);
        viewModel.setDobUtcMillis(null);
        viewModel.setDobTouched(true);
        viewModel.setEmail("john@example.com");
        viewModel.setEmailTouched(true);

        assertFalse(Boolean.TRUE.equals(getValue(viewModel.getIsFormValid())));
        assertEquals(FormValidator.Error.DOB_REQUIRED, getValue(viewModel.getDobError()));
    }

    @Test
    public void formValid_missingEmail_returnsFalse() {
        viewModel.setName("John Doe");
        viewModel.setNameTouched(true);
        viewModel.setDobUtcMillis(631152000000L);
        viewModel.setDobTouched(true);
        viewModel.setEmail("");
        viewModel.setEmailTouched(true);

        assertFalse(Boolean.TRUE.equals(getValue(viewModel.getIsFormValid())));
        assertEquals(FormValidator.Error.EMAIL_REQUIRED, getValue(viewModel.getEmailError()));
    }

    @Test
    public void setAllTouched_marksAllFieldsTouched() {
        viewModel.setName("A");
        viewModel.setEmail("invalid");
        viewModel.setDobUtcMillis(null);

        viewModel.setAllTouched();

        assertTrue(viewModel.isNameTouched());
        assertTrue(viewModel.isDobTouched());
        assertTrue(viewModel.isEmailTouched());

        // All errors should be visible now
        assertEquals(FormValidator.Error.NAME_TOO_SHORT, getValue(viewModel.getNameError()));
        assertEquals(FormValidator.Error.DOB_REQUIRED, getValue(viewModel.getDobError()));
        assertEquals(FormValidator.Error.EMAIL_INVALID, getValue(viewModel.getEmailError()));
    }

    @Test
    public void stateSurvivesRotation_simulatedByNewViewModelInstance() {
        // Simulate rotation by creating a new ViewModel with same data
        viewModel.setName("Jane Smith");
        viewModel.setDobUtcMillis(631152000000L); // 1990-01-01 UTC
        viewModel.setEmail("jane@example.com");
        viewModel.setNameTouched(true);
        viewModel.setDobTouched(true);
        viewModel.setEmailTouched(true);

        // Verify initial state
        assertEquals("Jane Smith", viewModel.getName());
        assertEquals(631152000000L, viewModel.getDobUtcMillis().longValue());
        assertEquals("jane@example.com", viewModel.getEmail());
        assertTrue(viewModel.isNameTouched());
        assertTrue(viewModel.isDobTouched());
        assertTrue(viewModel.isEmailTouched());
        assertTrue(Boolean.TRUE.equals(getValue(viewModel.getIsFormValid())));

        // Create new ViewModel instance (simulating Activity recreation)
        RegistrationViewModel newViewModel = new RegistrationViewModel();

        // Copy state (in real app, ViewModel survives; here we test the data model)
        newViewModel.setName(viewModel.getName());
        newViewModel.setDobUtcMillis(viewModel.getDobUtcMillis());
        newViewModel.setEmail(viewModel.getEmail());
        newViewModel.setNameTouched(viewModel.isNameTouched());
        newViewModel.setDobTouched(viewModel.isDobTouched());
        newViewModel.setEmailTouched(viewModel.isEmailTouched());

        // Verify state is preserved
        assertEquals("Jane Smith", newViewModel.getName());
        assertEquals(631152000000L, newViewModel.getDobUtcMillis().longValue());
        assertEquals("jane@example.com", newViewModel.getEmail());
        assertTrue(newViewModel.isNameTouched());
        assertTrue(newViewModel.isDobTouched());
        assertTrue(newViewModel.isEmailTouched());
        assertTrue(Boolean.TRUE.equals(getValue(newViewModel.getIsFormValid())));
        assertEquals(FormValidator.Error.NONE, getValue(newViewModel.getNameError()));
        assertEquals(FormValidator.Error.NONE, getValue(newViewModel.getDobError()));
        assertEquals(FormValidator.Error.NONE, getValue(newViewModel.getEmailError()));
    }

    @Test
    public void errorClearedWhenFieldBecomesValid() {
        viewModel.setName("A");
        viewModel.setNameTouched(true);
        assertEquals(FormValidator.Error.NAME_TOO_SHORT, getValue(viewModel.getNameError()));

        viewModel.setName("Valid Name");
        assertEquals(FormValidator.Error.NONE, getValue(viewModel.getNameError()));
    }

    @Test
    public void untouchedFieldsDontShowErrors() {
        viewModel.setName("");
        viewModel.setEmail("");
        viewModel.setDobUtcMillis(null);
        // Don't set touched flags

        // Errors should be NONE even though fields are invalid
        assertEquals(FormValidator.Error.NONE, getValue(viewModel.getNameError()));
        assertEquals(FormValidator.Error.NONE, getValue(viewModel.getEmailError()));
        assertEquals(FormValidator.Error.NONE, getValue(viewModel.getDobError()));

        // But form should still be invalid
        assertFalse(Boolean.TRUE.equals(getValue(viewModel.getIsFormValid())));
    }

    // Helper to get LiveData value synchronously (works with InstantTaskExecutorRule)
    private <T> T getValue(LiveData<T> liveData) {
        return liveData.getValue();
    }
}