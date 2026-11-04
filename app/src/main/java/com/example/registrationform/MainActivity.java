package com.example.registrationform;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointBackward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity {

    private TextInputEditText etName;
    private TextInputEditText etDob;
    private TextInputEditText etEmail;
    private MaterialButton btnNext;
    private TextInputLayout tilName;
    private TextInputLayout tilDob;
    private TextInputLayout tilEmail;

    // Track user interaction to show errors only after interaction
    private boolean nameTouched = false;
    private boolean dobTouched = false;
    private boolean emailTouched = false;

    // Guard against double date picker opening
    private boolean isDatePickerShowing = false;

    // Selected date in UTC millis (from MaterialDatePicker)
    private Long selectedDobUtcMillis = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setupDatePicker();
        setupTextWatchers();
        setupNextButton();
    }

    private void initViews() {
        etName = findViewById(R.id.etName);
        etDob = findViewById(R.id.etDob);
        etEmail = findViewById(R.id.etEmail);
        btnNext = findViewById(R.id.btnNext);
        tilName = findViewById(R.id.tilName);
        tilDob = findViewById(R.id.tilDob);
        tilEmail = findViewById(R.id.tilEmail);
    }

    private void setupDatePicker() {
        // Set click listeners for both the edit text and the end icon
        View.OnClickListener dobClickListener = v -> {
            if (!isDatePickerShowing) {
                showDatePickerDialog();
            }
        };

        etDob.setOnClickListener(dobClickListener);
        etDob.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus && !isDatePickerShowing) {
                showDatePickerDialog();
            }
        });

        // Also handle end icon click (calendar icon)
        tilDob.setEndIconOnClickListener(v -> {
            if (!isDatePickerShowing) {
                showDatePickerDialog();
            }
        });
    }

    private void showDatePickerDialog() {
        isDatePickerShowing = true;
        dobTouched = true;

        // Create calendar constraints: only past dates up to today
        CalendarConstraints.Builder constraintsBuilder = new CalendarConstraints.Builder();
        constraintsBuilder.setValidator(DateValidatorPointBackward.now());

        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(getString(R.string.hint_date_of_birth))
                .setSelection(selectedDobUtcMillis != null ? selectedDobUtcMillis : MaterialDatePicker.todayInUtcMilliseconds())
                .setCalendarConstraints(constraintsBuilder.build())
                .build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            selectedDobUtcMillis = selection;
            updateDobField();
        });

        datePicker.addOnNegativeButtonClickListener(dialog -> {
            // User cancelled
        });

        datePicker.addOnCancelListener(dialog -> {
            // Dialog dismissed
        });

        datePicker.show(getSupportFragmentManager(), "DATE_PICKER");

        // Reset guard after a short delay to allow the dialog to fully show
        // Using a simple flag reset after the dialog is shown
        // MaterialDatePicker handles its own lifecycle, so we reset when it's dismissed
        datePicker.addOnDismissListener(dialog -> {
            isDatePickerShowing = false;
        });
    }

    private void updateDobField() {
        if (selectedDobUtcMillis != null) {
            // MaterialDatePicker returns UTC millis, so format with UTC timezone to avoid off-by-one-day bug
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            etDob.setText(sdf.format(selectedDobUtcMillis));
            tilDob.setError(null);
        }
    }

    private void setupTextWatchers() {
        // Name field watcher
        etName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (nameTouched) {
                    validateName();
                }
                updateButtonState();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        etName.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                nameTouched = true;
                validateName();
            }
        });

        // Email field watcher
        etEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (emailTouched) {
                    validateEmail();
                }
                updateButtonState();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        etEmail.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                emailTouched = true;
                validateEmail();
            }
        });

        // DOB field doesn't need a text watcher since it's not editable
        // But we track touch for validation
    }

    private void setupNextButton() {
        btnNext.setOnClickListener(v -> {
            // Validate all fields on submit
            nameTouched = true;
            dobTouched = true;
            emailTouched = true;

            boolean isNameValid = validateName();
            boolean isDobValid = validateDob();
            boolean isEmailValid = validateEmail();

            if (isNameValid && isDobValid && isEmailValid) {
                // Form is valid - proceed to next step
                String name = etName.getText().toString().trim();
                String dob = etDob.getText().toString().trim();
                String email = etEmail.getText().toString().trim();

                Toast.makeText(this,
                        "Form submitted!\nName: " + name + "\nDOB: " + dob + "\nEmail: " + email,
                        Toast.LENGTH_LONG).show();

                // TODO: Navigate to next screen or process registration
            }
        });

        // Initial button state
        updateButtonState();
    }

    private boolean validateName() {
        String name = etName.getText() != null ? etName.getText().toString().trim() : "";
        if (TextUtils.isEmpty(name)) {
            tilName.setError(getString(R.string.error_name_required));
            return false;
        } else if (name.length() < 2) {
            tilName.setError(getString(R.string.error_name_min_length));
            return false;
        } else {
            tilName.setError(null);
            return true;
        }
    }

    private boolean validateDob() {
        String dob = etDob.getText() != null ? etDob.getText().toString().trim() : "";
        if (TextUtils.isEmpty(dob)) {
            tilDob.setError(getString(R.string.error_dob_required));
            return false;
        } else {
            tilDob.setError(null);
            return true;
        }
    }

    private boolean validateEmail() {
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        if (TextUtils.isEmpty(email)) {
            tilEmail.setError(getString(R.string.error_email_required));
            return false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError(getString(R.string.error_email_invalid));
            return false;
        } else {
            tilEmail.setError(null);
            return true;
        }
    }

    private void updateButtonState() {
        boolean isNameValid = !TextUtils.isEmpty(etName.getText() != null ? etName.getText().toString().trim() : "") &&
                etName.getText().toString().trim().length() >= 2;
        boolean isDobValid = selectedDobUtcMillis != null;
        boolean isEmailValid = !TextUtils.isEmpty(etEmail.getText() != null ? etEmail.getText().toString().trim() : "") &&
                android.util.Patterns.EMAIL_ADDRESS.matcher(etEmail.getText().toString().trim()).matches();

        boolean allValid = isNameValid && isDobValid && isEmailValid;
        btnNext.setEnabled(allValid);
    }
}