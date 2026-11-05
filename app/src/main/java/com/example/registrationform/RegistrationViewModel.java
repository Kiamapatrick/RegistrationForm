package com.example.registrationform;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * ViewModel for the registration form.
 * Holds form state (name, DOB, email, touched flags) and exposes LiveData for UI observation.
 * Survives configuration changes (rotation).
 */
public class RegistrationViewModel extends ViewModel {

    // Form field values
    private String name = "";
    private Long dobUtcMillis = null;
    private String email = "";

    // Touched flags - track user interaction to show errors only after interaction
    private boolean nameTouched = false;
    private boolean dobTouched = false;
    private boolean emailTouched = false;

    // LiveData for field errors (NONE = valid, specific error = invalid after touched)
    private final MutableLiveData<FormValidator.Error> nameError = new MutableLiveData<>(FormValidator.Error.NONE);
    private final MutableLiveData<FormValidator.Error> dobError = new MutableLiveData<>(FormValidator.Error.NONE);
    private final MutableLiveData<FormValidator.Error> emailError = new MutableLiveData<>(FormValidator.Error.NONE);

    // LiveData for form validity
    private final MutableLiveData<Boolean> isFormValid = new MutableLiveData<>(false);

    // Getters for current values
    public String getName() {
        return name;
    }

    public Long getDobUtcMillis() {
        return dobUtcMillis;
    }

    public String getEmail() {
        return email;
    }

    public boolean isNameTouched() {
        return nameTouched;
    }

    public boolean isDobTouched() {
        return dobTouched;
    }

    public boolean isEmailTouched() {
        return emailTouched;
    }

    // LiveData getters for observation
    public LiveData<FormValidator.Error> getNameError() {
        return nameError;
    }

    public LiveData<FormValidator.Error> getDobError() {
        return dobError;
    }

    public LiveData<FormValidator.Error> getEmailError() {
        return emailError;
    }

    public LiveData<Boolean> getIsFormValid() {
        return isFormValid;
    }

    // Setters - called from Activity
    public void setName(String name) {
        this.name = name != null ? name : "";
        validateAndUpdate();
    }

    public void setDobUtcMillis(Long dobUtcMillis) {
        this.dobUtcMillis = dobUtcMillis;
        validateAndUpdate();
    }

    public void setEmail(String email) {
        this.email = email != null ? email : "";
        validateAndUpdate();
    }

    public void setNameTouched(boolean touched) {
        this.nameTouched = touched;
        if (touched) {
            validateAndUpdate();
        }
    }

    public void setDobTouched(boolean touched) {
        this.dobTouched = touched;
        if (touched) {
            validateAndUpdate();
        }
    }

    public void setEmailTouched(boolean touched) {
        this.emailTouched = touched;
        if (touched) {
            validateAndUpdate();
        }
    }

    // Called when all fields are touched (e.g., on submit)
    public void setAllTouched() {
        this.nameTouched = true;
        this.dobTouched = true;
        this.emailTouched = true;
        validateAndUpdate();
    }

    // Validate all fields and update LiveData
    private void validateAndUpdate() {
        // Validate name
        FormValidator.ValidationResult nameResult = FormValidator.validateName(name);
        if (nameTouched && !nameResult.isValid()) {
            nameError.setValue(nameResult.error);
        } else {
            nameError.setValue(FormValidator.Error.NONE);
        }

        // Validate DOB
        FormValidator.ValidationResult dobResult = FormValidator.validateDob(dobUtcMillis);
        if (dobTouched && !dobResult.isValid()) {
            dobError.setValue(dobResult.error);
        } else {
            dobError.setValue(FormValidator.Error.NONE);
        }

        // Validate email
        FormValidator.ValidationResult emailResult = FormValidator.validateEmail(email);
        if (emailTouched && !emailResult.isValid()) {
            emailError.setValue(emailResult.error);
        } else {
            emailError.setValue(FormValidator.Error.NONE);
        }

        // Update form validity (all fields must be valid, regardless of touched state)
        boolean allValid = nameResult.isValid() && dobResult.isValid() && emailResult.isValid();
        isFormValid.setValue(allValid);
    }
}