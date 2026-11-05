package com.example.registrationform;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.Objects;

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

    // LiveData for field errors (null = no error)
    private final MutableLiveData<String> nameError = new MutableLiveData<>();
    private final MutableLiveData<String> dobError = new MutableLiveData<>();
    private final MutableLiveData<String> emailError = new MutableLiveData<>();

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
    public LiveData<String> getNameError() {
        return nameError;
    }

    public LiveData<String> getDobError() {
        return dobError;
    }

    public LiveData<String> getEmailError() {
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
            nameError.setValue(mapErrorToString(nameResult.error));
        } else {
            nameError.setValue(null);
        }

        // Validate DOB
        FormValidator.ValidationResult dobResult = FormValidator.validateDob(dobUtcMillis);
        if (dobTouched && !dobResult.isValid()) {
            dobError.setValue(mapErrorToString(dobResult.error));
        } else {
            dobError.setValue(null);
        }

        // Validate email
        FormValidator.ValidationResult emailResult = FormValidator.validateEmail(email);
        if (emailTouched && !emailResult.isValid()) {
            emailError.setValue(mapErrorToString(emailResult.error));
        } else {
            emailError.setValue(null);
        }

        // Update form validity (all fields must be valid, regardless of touched state)
        boolean allValid = nameResult.isValid() && dobResult.isValid() && emailResult.isValid();
        isFormValid.setValue(allValid);
    }

    // Map FormValidator.Error to string resource key (Activity will resolve to actual string)
    private String mapErrorToString(FormValidator.Error error) {
        switch (error) {
            case NAME_REQUIRED:
                return "error_name_required";
            case NAME_TOO_SHORT:
                return "error_name_min_length";
            case DOB_REQUIRED:
                return "error_dob_required";
            case DOB_FUTURE:
                return "error_dob_required"; // Reuse existing string for future date
            case EMAIL_REQUIRED:
                return "error_email_required";
            case EMAIL_INVALID:
                return "error_email_invalid";
            default:
                return "";
        }
    }

    // Helper to check if form data equals another ViewModel's data (for testing)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RegistrationViewModel that = (RegistrationViewModel) o;
        return nameTouched == that.nameTouched &&
                dobTouched == that.dobTouched &&
                emailTouched == that.emailTouched &&
                Objects.equals(name, that.name) &&
                Objects.equals(dobUtcMillis, that.dobUtcMillis) &&
                Objects.equals(email, that.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, dobUtcMillis, email, nameTouched, dobTouched, emailTouched);
    }
}