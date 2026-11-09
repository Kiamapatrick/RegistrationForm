package com.example.registrationform;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowInsets;
import android.view.animation.OvershootInterpolator;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.IntentCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
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

import androidx.dynamicanimation.animation.DynamicAnimation;
import androidx.dynamicanimation.animation.SpringAnimation;
import androidx.dynamicanimation.animation.SpringForce;

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

    // Track if form has been valid before (for pulse animation)
    private boolean hasBeenValid = false;

    // ActivityResultLauncher for SummaryActivity
    private final ActivityResultLauncher<Intent> summaryActivityLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    new ActivityResultCallback<ActivityResult>() {
                        @Override
                        public void onActivityResult(ActivityResult result) {
                            if (result.getResultCode() == RESULT_OK) {
                                // User confirmed in SummaryActivity - reset form
                                viewModel.reset();
                                clearFormFields();
                            }
                            // Enable btnNext based on current form validity.
                            // After RESULT_OK: form is reset (invalid) -> button stays disabled.
                            // After RESULT_CANCELED (Edit details/back): form unchanged -> enabled if valid.
                            btnNext.setEnabled(Boolean.TRUE.equals(viewModel.getIsFormValid().getValue()));
                        }
                    });

    private RegistrationViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Edge-to-edge insets handling
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());
            v.setPadding(
                    systemBars.left,
                    systemBars.top,
                    systemBars.right,
                    systemBars.bottom + ime.bottom
            );
            return insets;
        });

        viewModel = new ViewModelProvider(this).get(RegistrationViewModel.class);

        initViews();
        setupBlobLayer();
        setupDatePicker();
        setupTextWatchers();
        setupNextButton();
        observeViewModel();
        syncViewsFromViewModel();

        // Restore date picker listeners if picker is already showing (e.g., after rotation)
        restoreDatePickerIfShowing();

        // Entrance animations only on initial creation (not on rotation)
        if (savedInstanceState == null) {
            runEntranceAnimations();
        }
    }

    private void setupBlobLayer() {
        View blobLayer = findViewById(R.id.blobLayer);
        if (blobLayer == null) return;

        // Optional blur on API 31+
        if (Build.VERSION.SDK_INT >= 31) {
            boolean isPowerSave = false;
            android.os.PowerManager pm = (android.os.PowerManager) getSystemService(POWER_SERVICE);
            if (pm != null && pm.isPowerSaveMode()) {
                // Skip blur in power save mode
            } else if (isMotionReduced(this)) {
                // Skip blur if motion reduced
            } else {
                try {
                    var windowManager = (android.view.WindowManager) getSystemService(WINDOW_SERVICE);
                    if (windowManager != null && windowManager.isCrossWindowBlurEnabled()) {
                        blobLayer.setRenderEffect(android.graphics.RenderEffect.createBlurEffect(80f, 80f, android.graphics.Shader.TileMode.CLAMP));
                    }
                } catch (Exception e) {
                    // Blur not supported, skip
                }
            }
        }
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

        // Style the date picker dialog
        if (Build.VERSION.SDK_INT >= 31) {
            try {
                var windowManager = (android.view.WindowManager) getSystemService(WINDOW_SERVICE);
                if (windowManager != null && windowManager.isCrossWindowBlurEnabled()) {
                    // Note: MaterialDatePicker doesn't directly expose window blur setting
                    // This would require a custom dialog theme
                }
            } catch (Exception e) {
                // Ignore
            }
        }

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
            // Use device locale for month names (e.g., Swahili month names when device is in Swahili)
            Locale locale = getResources().getConfiguration().getLocales().get(0);
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", locale);
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

        // Handle "Done" action on confirm password field to submit form when valid
        etConfirmPassword.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE) {
                // Validate all fields on submit
                viewModel.setAllTouched();
                if (Boolean.TRUE.equals(viewModel.getIsFormValid().getValue())) {
                    btnNext.performClick();
                    return true;
                }
            }
            return false;
        });

        // DOB field doesn't need a text watcher since it's not editable
        // But we track touch for validation via the date picker
    }

    private void setupNextButton() {
        // Spring press animation
        btnNext.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                animateScaleSpring(v, 0.97f);
                v.performHapticFeedback(android.view.HapticFeedbackConstants.CONTEXT_CLICK);
            } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                animateScaleSpring(v, 1.0f);
            }
            return false;
        });

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
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

                // Disable btnNext to prevent double-click while launch is in progress
                btnNext.setEnabled(false);
                summaryActivityLauncher.launch(intent);
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
                android.animation.ObjectAnimator.ofInt(passwordStrengthBar, "progress", progress)
                        .setDuration(getResources().getInteger(R.integer.motion_duration_short))
                        .start();
            }
        });

        viewModel.getPasswordStrengthLevel().observe(this, level -> {
            if (level != null) {
                int stringRes;
                int colorRes;
                switch (level) {
                    case WEAK:
                        stringRes = R.string.password_strength_weak;
                        colorRes = R.color.strength_weak;
                        break;
                    case FAIR:
                        stringRes = R.string.password_strength_fair;
                        colorRes = R.color.strength_fair;
                        break;
                    case STRONG:
                        stringRes = R.string.password_strength_strong;
                        colorRes = R.color.strength_strong;
                        break;
                    default:
                        stringRes = R.string.password_strength_weak;
                        colorRes = R.color.strength_weak;
                }
                passwordStrengthLabel.setText(stringRes);
                passwordStrengthLabel.setVisibility(View.VISIBLE);

                // Animate color transition
                if (!isMotionReduced(this)) {
                    int startColor = androidx.core.content.ContextCompat.getColor(this, R.color.strength_weak);
                    int endColor = androidx.core.content.ContextCompat.getColor(this, colorRes);
                    android.animation.ValueAnimator colorAnim = android.animation.ValueAnimator.ofObject(
                            new android.animation.ArgbEvaluator(),
                            startColor,
                            endColor
                    );
                    colorAnim.setDuration(getResources().getInteger(R.integer.motion_duration_short));
                    colorAnim.addUpdateListener(anim -> {
                        passwordStrengthBar.setIndicatorColor((Integer) anim.getAnimatedValue());
                    });
                    colorAnim.start();
                } else {
                    passwordStrengthBar.setIndicatorColor(androidx.core.content.ContextCompat.getColor(this, colorRes));
                }
            } else {
                passwordStrengthLabel.setVisibility(View.GONE);
            }
        });

        // Observe form validity for button state
        viewModel.getIsFormValid().observe(this, isValid -> {
            boolean enabled = Boolean.TRUE.equals(isValid);
            animateButtonState(btnNext, enabled);

            // Pulse once when form first becomes valid
            if (enabled && !hasBeenValid) {
                hasBeenValid = true;
                pulseButton(btnNext);
            }
        });

        // Field validity animations (check icon fade-in, error shake)
        observeFieldValidity(tilName, viewModel.getNameError());
        observeFieldValidity(tilEmail, viewModel.getEmailError());
        observeFieldValidity(tilPassword, viewModel.getPasswordError());
        observeFieldValidity(tilConfirmPassword, viewModel.getConfirmPasswordError());
        observeFieldValidity(tilDob, viewModel.getDobError());
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

    /**
     * Clears all form fields in the UI after successful confirmation.
     */
    private void clearFormFields() {
        etName.setText("");
        etDob.setText("");
        etEmail.setText("");
        etPassword.setText("");
        etConfirmPassword.setText("");

        tilName.setError(null);
        tilDob.setError(null);
        tilEmail.setError(null);
        tilPassword.setError(null);
        tilConfirmPassword.setError(null);

        passwordStrengthBar.setProgress(0, false);
        passwordStrengthLabel.setVisibility(View.GONE);
    }

    // Motion and Animation Helpers

    private void runEntranceAnimations() {
        if (isMotionReduced(this)) {
            // Show immediately
            findViewById(R.id.tvTitle).setAlpha(1f);
            findViewById(R.id.tvSubtitle).setAlpha(1f);
            findViewById(R.id.cardPersonal).setAlpha(1f);
            findViewById(R.id.cardSecurity).setAlpha(1f);
            findViewById(R.id.btnNext).setAlpha(1f);
            return;
        }

        View header = findViewById(R.id.tvTitle);
        View subtitle = findViewById(R.id.tvSubtitle);
        View card1 = findViewById(R.id.cardPersonal);
        View card2 = findViewById(R.id.cardSecurity);

        View[] views = {header, subtitle, card1, card2};
        int stagger = getResources().getInteger(R.integer.motion_duration_entrance_stagger);

        for (int i = 0; i < views.length; i++) {
            View view = views[i];
            if (view == null) continue;
            view.setAlpha(0f);
            view.setTranslationY(24f);
            view.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(getResources().getInteger(R.integer.motion_duration_medium))
                    .setStartDelay(i * stagger)
                    .setInterpolator(new OvershootInterpolator(1.2f))
                    .withEndAction(() -> {
                        view.setAlpha(1f);
                        view.setTranslationY(0f);
                    })
                    .start();
        }
    }

    private void animateButtonState(MaterialButton button, boolean enabled) {
        if (isMotionReduced(this)) {
            button.setEnabled(enabled);
            button.setAlpha(enabled ? 1f : 0.38f);
            return;
        }

        button.setEnabled(enabled);
        button.animate()
                .alpha(enabled ? 1f : 0.38f)
                .setDuration(getResources().getInteger(R.integer.motion_duration_short))
                .setInterpolator(new OvershootInterpolator(1.2f))
                .start();

        // Pulse once when first becoming valid
        // Note: In a real implementation, you'd track if this is the first time becoming valid
    }

    /**
     * Pulses the button once (scale 1.0 -> 1.05 -> 1.0) with a spring.
     */
    private void pulseButton(MaterialButton button) {
        if (isMotionReduced(this)) return;

        SpringForce springForce = new SpringForce(1f);
        springForce.setStiffness(getResources().getInteger(R.integer.spring_stiffness));
        springForce.setDampingRatio(SPRING_DAMPING_RATIO);

        SpringAnimation scaleX = new SpringAnimation(button, DynamicAnimation.SCALE_X);
        SpringAnimation scaleY = new SpringAnimation(button, DynamicAnimation.SCALE_Y);

        scaleX.setSpring(springForce);
        scaleY.setSpring(springForce);

        // Start from 1.05
        button.setScaleX(1.05f);
        button.setScaleY(1.05f);

        scaleX.setSpring(springForce);
        scaleY.setSpring(springForce);

        scaleX.start();
        scaleY.start();
    }

    /**
     * Checks if motion is reduced via system settings or power save mode.
     */
    private boolean isMotionReduced(Context context) {
        float scale = Settings.Global.getFloat(context.getContentResolver(),
                Settings.Global.ANIMATOR_DURATION_SCALE, 1f);
        if (scale == 0f) {
            return true;
        }

        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        if (pm != null && pm.isPowerSaveMode()) {
            return true;
        }

        return false;
    }

    private static final float SPRING_DAMPING_RATIO = 0.7f;

    /**
     * Animates view scale with a spring.
     */
    private void animateScaleSpring(View view, float targetScale) {
        if (isMotionReduced(this)) {
            view.setScaleX(targetScale);
            view.setScaleY(targetScale);
            return;
        }

        SpringForce springForce = new SpringForce(targetScale);
        springForce.setStiffness(getResources().getInteger(R.integer.spring_stiffness));
        springForce.setDampingRatio(SPRING_DAMPING_RATIO);

        SpringAnimation scaleX = new SpringAnimation(view, DynamicAnimation.SCALE_X);
        SpringAnimation scaleY = new SpringAnimation(view, DynamicAnimation.SCALE_Y);

        scaleX.setSpring(springForce);
        scaleY.setSpring(springForce);

        scaleX.start();
        scaleY.start();
    }

    /**
     * Animates view scale with a spring (for button press).
     */
    private void animateScaleSpring(View view, float targetScale, float stiffness, float dampingRatio) {
        if (isMotionReduced(this)) {
            view.setScaleX(targetScale);
            view.setScaleY(targetScale);
            return;
        }

        SpringForce springForce = new SpringForce(targetScale);
        springForce.setStiffness(stiffness);
        springForce.setDampingRatio(dampingRatio);

        SpringAnimation scaleX = new SpringAnimation(view, DynamicAnimation.SCALE_X);
        SpringAnimation scaleY = new SpringAnimation(view, DynamicAnimation.SCALE_Y);

        scaleX.setSpring(springForce);
        scaleY.setSpring(springForce);

        scaleX.start();
        scaleY.start();
    }

    /**
     * Observes field validity and animates check icon / shake on error.
     */
    private void observeFieldValidity(TextInputLayout til, androidx.lifecycle.LiveData<FormValidator.Error> errorLiveData) {
        errorLiveData.observe(this, new androidx.lifecycle.Observer<FormValidator.Error>() {
            @Override
            public void onChanged(FormValidator.Error error) {
                boolean isError = error != null && error != FormValidator.Error.NONE;
                boolean wasError = til.isErrorEnabled() && til.getError() != null;

                // Animate check icon
                android.graphics.drawable.Drawable endIcon = til.getEndIconDrawable();

                if (!isError && !wasError) {
                    // Field became valid - animate check icon in
                    if (endIcon instanceof android.graphics.drawable.AnimatedVectorDrawable) {
                        if (!isMotionReduced(MainActivity.this)) {
                            ((android.graphics.drawable.AnimatedVectorDrawable) endIcon).start();
                        } else {
                            ((android.graphics.drawable.AnimatedVectorDrawable) endIcon).reset();
                        }
                    }
                } else if (isError && !wasError) {
                    // Field became invalid - shake the field
                    if (!isMotionReduced(MainActivity.this)) {
                        android.view.animation.Animation shake = android.view.animation.AnimationUtils.loadAnimation(MainActivity.this, R.anim.shake_horizontal);
                        til.startAnimation(shake);
                    }
                }
            }
        });
    }
}