package com.example.registrationform;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointBackward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textview.MaterialTextView;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Locale;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity {

    private TextInputEditText etName;
    private TextInputEditText etDob;
    private TextInputEditText etEmail;
    private TextInputEditText etPassword;
    private TextInputEditText etConfirmPassword;
    private MaterialButton btnNext;
    private TextInputLayout tilName;
    private TextInputLayout tilDob;
    private TextInputLayout tilEmail;
    private TextInputLayout tilPassword;
    private TextInputLayout tilConfirmPassword;
    private LinearProgressIndicator passwordStrengthBar;
    private MaterialTextView passwordStrengthLabel;

    // Guard against double date picker opening
    private boolean isDatePickerShowing = false;

    private RegistrationViewModel viewModel;

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

        viewModel = new ViewModelProvider(this).get(RegistrationViewModel.class);

        initViews();
        setupDatePicker();
        setupTextWatchers();
        setupNextButton();
        observeViewModel();
        syncViewsFromViewModel();

        // Restore date picker listeners if picker is already showing (e.g., after rotation)
        restoreDatePickerIfShowing();
    }

    private void initViews() {
        etName = findViewById(R.id.etName);
        etDob = findViewById(R.id.etDob);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnNext = findViewById(R.id.btnNext);
        tilName = findViewById(R.id.tilName);
        tilDob = findViewById(R.id.tilDob);
        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);
        passwordStrengthBar = findViewById(R.id.passwordStrengthBar);
        passwordStrengthLabel = findViewById(R.id.passwordStrengthLabel);
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
        viewModel.setDobTouched(true);

        // Calculate max date: 18 years ago from today (in UTC millis)
        long todayUtcMillis = MaterialDatePicker.todayInUtcMilliseconds();
        LocalDate today = Instant.ofEpochMilli(todayUtcMillis).atOffset(ZoneOffset.UTC).toLocalDate();
        LocalDate eighteenYearsAgo = today.minusYears(FormValidator.MIN_AGE_YEARS);
        long maxDateUtcMillis = eighteenYearsAgo.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();

        // Create calendar constraints: past dates up to 18 years ago
        CalendarConstraints.Builder constraintsBuilder = new CalendarConstraints.Builder();
        constraintsBuilder.setValidator(DateValidatorPointBackward.before(maxDateUtcMillis));
        constraintsBuilder.setEnd(maxDateUtcMillis);

        // Default selection: if no date selected yet, open on the max date (18 years ago)
        long defaultSelection = viewModel.getDobUtcMillis() != null
                ? viewModel.getDobUtcMillis()
                : maxDateUtcMillis;

        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(getString(R.string.hint_date_of_birth))
                .setSelection(defaultSelection)
                .setCalendarConstraints(constraintsBuilder.build())
                .build();

        attachDatePickerListeners(datePicker);

        datePicker.show(getSupportFragmentManager(), "DATE_PICKER");
    }

    /**
     * Attach positive button and dismiss listeners to a date picker.
     * Shared between creating a new picker and restoring an existing one after rotation.
     */
    @SuppressWarnings("unchecked")
    private void attachDatePickerListeners(MaterialDatePicker<Long> datePicker) {
        datePicker.addOnPositiveButtonClickListener(selection -> {
            viewModel.setDobUtcMillis(selection);
            updateDobField();
        });

        datePicker.addOnNegativeButtonClickListener(dialog -> {
            // User cancelled
        });

        datePicker.addOnCancelListener(dialog -> {
            // Dialog dismissed
        });

        datePicker.addOnDismissListener(dialog -> {
            isDatePickerShowing = false;
        });
    }

    /**
     * If a date picker is already showing (e.g., after rotation), find it and re-attach listeners.
     */
    @SuppressWarnings("unchecked")
    private void restoreDatePickerIfShowing() {
        MaterialDatePicker<Long> existingPicker = (MaterialDatePicker<Long>) getSupportFragmentManager()
                .findFragmentByTag("DATE_PICKER");
        if (existingPicker != null && existingPicker.isAdded()) {
            isDatePickerShowing = true;
            attachDatePickerListeners(existingPicker);
        }
    }

    private void updateDobField() {
        if (viewModel.getDobUtcMillis() != null) {
            // MaterialDatePicker returns UTC millis, so format with UTC timezone to avoid off-by-one-day bug
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH);
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            etDob.setText(sdf.format(viewModel.getDobUtcMillis()));
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
                viewModel.setName(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        etName.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                viewModel.setNameTouched(true);
            }
        });

        // Email field watcher
        etEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.setEmail(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        etEmail.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                viewModel.setEmailTouched(true);
            }
        });

        // Password field watcher
        etPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.setPassword(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        etPassword.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                viewModel.setPasswordTouched(true);
            }
        });

        // Confirm Password field watcher
        etConfirmPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.setConfirmPassword(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        etConfirmPassword.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                viewModel.setConfirmPasswordTouched(true);
            }
        });

        // DOB field doesn't need a text watcher since it's not editable
        // But we track touch for validation via the date picker
    }

    private void setupNextButton() {
        btnNext.setOnClickListener(v -> {
            // Validate all fields on submit
            viewModel.setAllTouched();

            if (Boolean.TRUE.equals(viewModel.getIsFormValid().getValue())) {
                // Form is valid - proceed to summary screen
                String name = etName.getText().toString().trim();
                String email = etEmail.getText().toString().trim();
                Long dobUtcMillis = viewModel.getDobUtcMillis();

                RegistrationData data = new RegistrationData(name, email, dobUtcMillis);
                Intent intent = new Intent(this, SummaryActivity.class);
                intent.putExtra(RegistrationData.EXTRA_KEY, data);
                startActivity(intent);

                // Do not call finish() - keep MainActivity alive so ViewModel survives rotation
            }
        });
    }

    private void observeViewModel() {
        // Observe name error
        viewModel.getNameError().observe(this, error -> {
            if (error != null && error != FormValidator.Error.NONE) {
                tilName.setError(getString(errorToStringRes(error)));
            } else {
                tilName.setError(null);
            }
        });

        // Observe DOB error
        viewModel.getDobError().observe(this, error -> {
            if (error != null && error != FormValidator.Error.NONE) {
                tilDob.setError(getString(errorToStringRes(error)));
            } else {
                tilDob.setError(null);
            }
        });

        // Observe email error
        viewModel.getEmailError().observe(this, error -> {
            if (error != null && error != FormValidator.Error.NONE) {
                tilEmail.setError(getString(errorToStringRes(error)));
            } else {
                tilEmail.setError(null);
            }
        });

        // Observe password error
        viewModel.getPasswordError().observe(this, error -> {
            if (error != null && error != FormValidator.Error.NONE) {
                tilPassword.setError(getString(errorToStringRes(error)));
            } else {
                tilPassword.setError(null);
            }
        });

        // Observe confirm password error
        viewModel.getConfirmPasswordError().observe(this, error -> {
            if (error != null && error != FormValidator.Error.NONE) {
                tilConfirmPassword.setError(getString(errorToStringRes(error)));
            } else {
                tilConfirmPassword.setError(null);
            }
        });

        // Observe password strength
        viewModel.getPasswordStrengthProgress().observe(this, progress -> {
            if (progress != null) {
                passwordStrengthBar.setProgress(progress, true);
            }
        });

        viewModel.getPasswordStrengthLevel().observe(this, level -> {
            if (level != null) {
                int stringRes;
                switch (level) {
                    case WEAK:
                        stringRes = R.string.password_strength_weak;
                        break;
                    case FAIR:
                        stringRes = R.string.password_strength_fair;
                        break;
                    case STRONG:
                        stringRes = R.string.password_strength_strong;
                        break;
                    default:
                        stringRes = R.string.password_strength_weak;
                }
                passwordStrengthLabel.setText(stringRes);
                passwordStrengthLabel.setVisibility(View.VISIBLE);
            } else {
                passwordStrengthLabel.setVisibility(View.GONE);
            }
        });

        // Observe form validity for button state
        viewModel.getIsFormValid().observe(this, isValid -> {
            btnNext.setEnabled(Boolean.TRUE.equals(isValid));
        });
    }

    /**
     * Map a FormValidator.Error to its corresponding string resource ID.
     */
    private int errorToStringRes(FormValidator.Error error) {
        switch (error) {
            case NAME_REQUIRED:
                return R.string.error_name_required;
            case NAME_TOO_SHORT:
                return R.string.error_name_min_length;
            case NAME_INVALID_CHARS:
                return R.string.error_name_invalid;
            case DOB_REQUIRED:
                return R.string.error_dob_required;
            case DOB_FUTURE:
                return R.string.error_dob_future;
            case DOB_UNDER_AGE:
                return R.string.error_dob_under_age;
            case EMAIL_REQUIRED:
                return R.string.error_email_required;
            case EMAIL_INVALID:
                return R.string.error_email_invalid;
            case PASSWORD_REQUIRED:
                return R.string.error_password_required;
            case PASSWORD_TOO_SHORT:
                return R.string.error_password_too_short;
            case PASSWORD_MISSING_LETTER:
                return R.string.error_password_missing_letter;
            case PASSWORD_MISSING_DIGIT:
                return R.string.error_password_missing_digit;
            case PASSWORD_HAS_EDGE_SPACES:
                return R.string.error_password_edge_spaces;
            case PASSWORD_TOO_COMMON:
                return R.string.error_password_common;
            case PASSWORD_MISMATCH:
                return R.string.error_password_mismatch;
            case CONFIRM_REQUIRED:
                return R.string.error_confirm_required;
            default:
                return 0;
        }
    }

    private void syncViewsFromViewModel() {
        // Sync name
        if (viewModel.getName() != null && !viewModel.getName().isEmpty()) {
            etName.setText(viewModel.getName());
            etName.setSelection(viewModel.getName().length());
        }

        // Sync DOB
        if (viewModel.getDobUtcMillis() != null) {
            updateDobField();
        }

        // Sync email
        if (viewModel.getEmail() != null && !viewModel.getEmail().isEmpty()) {
            etEmail.setText(viewModel.getEmail());
            etEmail.setSelection(viewModel.getEmail().length());
        }

        // Sync password
        if (viewModel.getPassword() != null && !viewModel.getPassword().isEmpty()) {
            etPassword.setText(viewModel.getPassword());
            etPassword.setSelection(viewModel.getPassword().length());
        }

        // Sync confirm password
        if (viewModel.getConfirmPassword() != null && !viewModel.getConfirmPassword().isEmpty()) {
            etConfirmPassword.setText(viewModel.getConfirmPassword());
            etConfirmPassword.setSelection(viewModel.getConfirmPassword().length());
        }
    }
}