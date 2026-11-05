package com.example.registrationform;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.LiveData;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

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

        // Initially no errors
        assertNull(getValue(viewModel.getNameError()));
        assertNull(getValue(viewModel.getDobError()));
        assertNull(getValue(viewModel.getEmailError()));

        // Form is not valid initially (empty fields)
        assertFalse(Boolean.TRUE.equals(getValue(viewModel.getIsFormValid())));
    }

    @Test
    public void nameValidation_empty_showsErrorAfterTouched() {
        viewModel.setName("");
        viewModel.setNameTouched(true);

        assertEquals("error_name_required", getValue(viewModel.getNameError()));
        assertFalse(Boolean.TRUE.equals(getValue(viewModel.getIsFormValid())));
    }

    @Test
    public void nameValidation_oneChar_showsErrorAfterTouched() {
        viewModel.setName("A");
        viewModel.setNameTouched(true);

        assertEquals("error_name_min_length", getValue(viewModel.getNameError()));
        assertFalse(Boolean.TRUE.equals(getValue(viewModel.getIsFormValid())));
    }

    @Test
    public void nameValidation_valid_clearsError() {
        viewModel.setName("John");
        viewModel.setNameTouched(true);

        assertNull(getValue(viewModel.getNameError()));
    }

    @Test
    public void nameValidation_whitespaceOnly_showsRequiredError() {
        viewModel.setName("   ");
        viewModel.setNameTouched(true);

        assertEquals("error_name_required", getValue(viewModel.getNameError()));
    }

    @Test
    public void dobValidation_null_showsErrorAfterTouched() {
        viewModel.setDobUtcMillis(null);
        viewModel.setDobTouched(true);

        assertEquals("error_dob_required", getValue(viewModel.getDobError()));
        assertFalse(Boolean.TRUE.equals(getValue(viewModel.getIsFormValid())));
    }

    @Test
    public void dobValidation_validPastDate_clearsError() {
        long pastDate = 631152000000L; // 1990-01-01 UTC
        viewModel.setDobUtcMillis(pastDate);
        viewModel.setDobTouched(true);

        assertNull(getValue(viewModel.getDobError()));
    }

    @Test
    public void dobValidation_futureDate_showsError() {
        long futureDate = java.time.Instant.now().toEpochMilli() + 86400000; // tomorrow
        viewModel.setDobUtcMillis(futureDate);
        viewModel.setDobTouched(true);

        assertEquals("error_dob_required", getValue(viewModel.getDobError()));
        assertFalse(Boolean.TRUE.equals(getValue(viewModel.getIsFormValid())));
    }

    @Test
    public void emailValidation_empty_showsErrorAfterTouched() {
        viewModel.setEmail("");
        viewModel.setEmailTouched(true);

        assertEquals("error_email_required", getValue(viewModel.getEmailError()));
        assertFalse(Boolean.TRUE.equals(getValue(viewModel.getIsFormValid())));
    }

    @Test
    public void emailValidation_invalidFormat_showsErrorAfterTouched() {
        viewModel.setEmail("invalid-email");
        viewModel.setEmailTouched(true);

        assertEquals("error_email_invalid", getValue(viewModel.getEmailError()));
        assertFalse(Boolean.TRUE.equals(getValue(viewModel.getIsFormValid())));
    }

    @Test
    public void emailValidation_valid_clearsError() {
        viewModel.setEmail("user@example.com");
        viewModel.setEmailTouched(true);

        assertNull(getValue(viewModel.getEmailError()));
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
        assertNull(getValue(viewModel.getNameError()));
        assertNull(getValue(viewModel.getDobError()));
        assertNull(getValue(viewModel.getEmailError()));
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
        assertEquals("error_dob_required", getValue(viewModel.getDobError()));
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
        assertEquals("error_email_required", getValue(viewModel.getEmailError()));
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
        assertEquals("error_name_min_length", getValue(viewModel.getNameError()));
        assertEquals("error_dob_required", getValue(viewModel.getDobError()));
        assertEquals("error_email_invalid", getValue(viewModel.getEmailError()));
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
    }

    @Test
    public void errorClearedWhenFieldBecomesValid() {
        viewModel.setName("A");
        viewModel.setNameTouched(true);
        assertEquals("error_name_min_length", getValue(viewModel.getNameError()));

        viewModel.setName("Valid Name");
        assertNull(getValue(viewModel.getNameError()));
    }

    @Test
    public void untouchedFieldsDontShowErrors() {
        viewModel.setName("");
        viewModel.setEmail("");
        viewModel.setDobUtcMillis(null);
        // Don't set touched flags

        // Errors should be null even though fields are invalid
        assertNull(getValue(viewModel.getNameError()));
        assertNull(getValue(viewModel.getEmailError()));
        assertNull(getValue(viewModel.getDobError()));

        // But form should still be invalid
        assertFalse(Boolean.TRUE.equals(getValue(viewModel.getIsFormValid())));
    }

    // Helper to get LiveData value synchronously (works with InstantTaskExecutorRule)
    private <T> T getValue(LiveData<T> liveData) {
        return liveData.getValue();
    }
}