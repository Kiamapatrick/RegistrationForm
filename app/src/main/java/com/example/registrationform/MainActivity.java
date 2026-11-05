package com.example.registrationform;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

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
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
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
        viewModel.setDobTouched(true);

        // Create calendar constraints: only past dates up to today
        CalendarConstraints.Builder constraintsBuilder = new CalendarConstraints.Builder();
        constraintsBuilder.setValidator(DateValidatorPointBackward.now());

        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(getString(R.string.hint_date_of_birth))
                .setSelection(viewModel.getDobUtcMillis() != null ? viewModel.getDobUtcMillis() : MaterialDatePicker.todayInUtcMilliseconds())
                .setCalendarConstraints(constraintsBuilder.build())
                .build();

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

        datePicker.show(getSupportFragmentManager(), "DATE_PICKER");

        // Reset guard after a short delay to allow the dialog to fully show
        // Using a simple flag reset after the dialog is shown
        // MaterialDatePicker handles its own lifecycle, so we reset when it's dismissed
        datePicker.addOnDismissListener(dialog -> {
            isDatePickerShowing = false;
        });
    }

    private void updateDobField() {
        if (viewModel.getDobUtcMillis() != null) {
            // MaterialDatePicker returns UTC millis, so format with UTC timezone to avoid off-by-one-day bug
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
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

        // DOB field doesn't need a text watcher since it's not editable
        // But we track touch for validation via the date picker
    }

    private void setupNextButton() {
        btnNext.setOnClickListener(v -> {
            // Validate all fields on submit
            viewModel.setAllTouched();

            if (Boolean.TRUE.equals(viewModel.getIsFormValid().getValue())) {
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
    }

    private void observeViewModel() {
        // Observe name error
        viewModel.getNameError().observe(this, error -> {
            if (error != null) {
                tilName.setError(getString(getResources().getIdentifier(error, "string", getPackageName())));
            } else {
                tilName.setError(null);
            }
        });

        // Observe DOB error
        viewModel.getDobError().observe(this, error -> {
            if (error != null) {
                tilDob.setError(getString(getResources().getIdentifier(error, "string", getPackageName())));
            } else {
                tilDob.setError(null);
            }
        });

        // Observe email error
        viewModel.getEmailError().observe(this, error -> {
            if (error != null) {
                tilEmail.setError(getString(getResources().getIdentifier(error, "string", getPackageName())));
            } else {
                tilEmail.setError(null);
            }
        });

        // Observe form validity for button state
        viewModel.getIsFormValid().observe(this, isValid -> {
            btnNext.setEnabled(Boolean.TRUE.equals(isValid));
        });
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
    }
}