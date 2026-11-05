package com.example.registrationform;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * ViewModel for the registration form.
 * Holds form state (name, DOB, email, password, confirm password, touched flags)
 * and exposes LiveData for UI observation. Survives configuration changes (rotation).
 */
public class RegistrationViewModel extends ViewModel {

    // Form field values
    private String name = "";
    private Long dobUtcMillis = null;
    private String email = "";
    private String password = "";
    private String confirmPassword = "";

    // Touched flags - track user interaction to show errors only after interaction
    private boolean nameTouched = false;
    private boolean dobTouched = false;
    private boolean emailTouched = false;
    private boolean passwordTouched = false;
    private boolean confirmPasswordTouched = false;

    // LiveData for field errors (NONE = valid, specific error = invalid after touched)
    private final MutableLiveData<FormValidator.Error> nameError = new MutableLiveData<>(FormValidator.Error.NONE);
    private final MutableLiveData<FormValidator.Error> dobError = new MutableLiveData<>(FormValidator.Error.NONE);
    private final MutableLiveData<FormValidator.Error> emailError = new MutableLiveData<>(FormValidator.Error.NONE);
    private final MutableLiveData<FormValidator.Error> passwordError = new MutableLiveData<>(FormValidator.Error.NONE);
    private final MutableLiveData<FormValidator.Error> confirmPasswordError = new MutableLiveData<>(FormValidator.Error.NONE);

    // LiveData for password strength
    private final MutableLiveData<Integer> passwordStrengthProgress = new MutableLiveData<>(0);
    private final MutableLiveData<String> passwordStrengthLabel = new MutableLiveData<>("");

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

    public String getPassword() {
        return password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
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

    public boolean isPasswordTouched() {
        return passwordTouched;
    }

    public boolean isConfirmPasswordTouched() {
        return confirmPasswordTouched;
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

    public LiveData<FormValidator.Error> getPasswordError() {
        return passwordError;
    }

    public LiveData<FormValidator.Error> getConfirmPasswordError() {
        return confirmPasswordError;
    }

    public LiveData<Integer> getPasswordStrengthProgress() {
        return passwordStrengthProgress;
    }

    public LiveData<String> getPasswordStrengthLabel() {
        return passwordStrengthLabel;
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

    public void setPassword(String password) {
        this.password = password != null ? password : "";
        validateAndUpdate();
        updatePasswordStrength();
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword != null ? confirmPassword : "";
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

    public void setPasswordTouched(boolean touched) {
        this.passwordTouched = touched;
        if (touched) {
            validateAndUpdate();
        }
    }

    public void setConfirmPasswordTouched(boolean touched) {
        this.confirmPasswordTouched = touched;
        if (touched) {
            validateAndUpdate();
        }
    }

    // Called when all fields are touched (e.g., on submit)
    public void setAllTouched() {
        this.nameTouched = true;
        this.dobTouched = true;
        this.emailTouched = true;
        this.passwordTouched = true;
        this.confirmPasswordTouched = true;
        validateAndUpdate();
    }

    private void updatePasswordStrength() {
        PasswordStrength.Level level = PasswordStrength.evaluate(password);
        passwordStrengthProgress.setValue(PasswordStrength.progress(password));
        passwordStrengthLabel.setValue(PasswordStrength.label(level));
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

        // Validate password
        FormValidator.ValidationResult passwordResult = FormValidator.validatePassword(password);
        if (passwordTouched && !passwordResult.isValid()) {
            passwordError.setValue(passwordResult.error);
        } else {
            passwordError.setValue(FormValidator.Error.NONE);
        }

        // Validate confirm password
        FormValidator.ValidationResult confirmResult = FormValidator.validateConfirmPassword(confirmPassword, password);
        if (confirmPasswordTouched && !confirmResult.isValid()) {
            confirmPasswordError.setValue(confirmResult.error);
        } else {
            confirmPasswordError.setValue(FormValidator.Error.NONE);
        }

        // Update form validity (all fields must be valid, regardless of touched state)
        boolean allValid = nameResult.isValid() && dobResult.isValid() && emailResult.isValid()
                && passwordResult.isValid() && confirmResult.isValid();
        isFormValid.setValue(allValid);
    }
}