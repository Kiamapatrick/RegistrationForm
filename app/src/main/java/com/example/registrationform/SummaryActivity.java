package com.example.registrationform;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.IntentCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.card.MaterialCardView;

import java.util.Locale;

import androidx.dynamicanimation.animation.DynamicAnimation;
import androidx.dynamicanimation.animation.SpringAnimation;
import androidx.dynamicanimation.animation.SpringForce;

public class SummaryActivity extends AppCompatActivity {

    private com.google.android.material.card.MaterialCardView summaryCard;
    private com.google.android.material.card.MaterialCardView successCard;
    private LinearLayout buttonRow;

    private TextView tvNameValue;
    private TextView tvDobValue;
    private TextView tvEmailValue;

    private com.google.android.material.button.MaterialButton btnEditDetails;
    private com.google.android.material.button.MaterialButton btnConfirm;
    private com.google.android.material.button.MaterialButton btnDone;

    private RegistrationData registrationData;

    private boolean isSuccessState = false;
    private static final String KEY_SUCCESS_STATE = "isSuccessState";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_summary);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            androidx.core.graphics.Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            androidx.core.graphics.Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());
            v.setPadding(
                    systemBars.left,
                    systemBars.top,
                    systemBars.right,
                    systemBars.bottom + ime.bottom
            );
            return insets;
        });

        // Restore success state from saved instance state
        if (savedInstanceState != null) {
            isSuccessState = savedInstanceState.getBoolean(KEY_SUCCESS_STATE, false);
        }

        // Extract registration data from Intent
        Intent intent = getIntent();
        registrationData = IntentCompat.getParcelableExtra(intent, RegistrationData.EXTRA_KEY, RegistrationData.class);

        // Handle missing data
        if (registrationData == null) {
            finish();
            return;
        }

        initViews();
        populateSummary();
        setupButtons();

        // Restore UI state
        if (isSuccessState) {
            showSuccessState();
        }

        // Entrance animations
        runEntranceAnimations();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_SUCCESS_STATE, isSuccessState);
    }

    private void initViews() {
        summaryCard = findViewById(R.id.summaryCard);
        successCard = findViewById(R.id.successCard);
        buttonRow = findViewById(R.id.buttonRow);

        tvNameValue = findViewById(R.id.tvNameValue);
        tvDobValue = findViewById(R.id.tvDobValue);
        tvEmailValue = findViewById(R.id.tvEmailValue);

        btnEditDetails = findViewById(R.id.btnEditDetails);
        btnConfirm = findViewById(R.id.btnConfirm);
        btnDone = findViewById(R.id.btnDone);
    }

    private void populateSummary() {
        tvNameValue.setText(registrationData.name);
        // Use device locale for month names (e.g., Swahili month names when device is in Swahili)
        Locale locale = getResources().getConfiguration().getLocales().get(0);
        tvDobValue.setText(RegistrationFormatter.formatDob(registrationData.dobUtcMillis, locale));
        tvEmailValue.setText(registrationData.email);
    }

    private void setupButtons() {
        btnEditDetails.setOnClickListener(v -> {
            // Simply finish this activity to return to MainActivity
            // MainActivity's ViewModel still has all the data
            finish();
        });

        btnConfirm.setOnClickListener(v -> {
            // Show success state
            showSuccessState();
        });

        btnDone.setOnClickListener(v -> {
            // Set RESULT_OK and finish to trigger form reset in MainActivity
            setResult(RESULT_OK);
            finish();
        });
    }

    private void showSuccessState() {
        isSuccessState = true;
        setResult(RESULT_OK);
        summaryCard.setVisibility(View.GONE);
        successCard.setVisibility(View.VISIBLE);
        buttonRow.setVisibility(View.GONE);

        // Animate success icon
        animateSuccessIcon();
    }

    // Motion and Animation Helpers

    private void runEntranceAnimations() {
        if (isMotionReduced(this)) {
            // Show immediately
            findViewById(R.id.tvTitle).setAlpha(1f);
            findViewById(R.id.tvSubtitle).setAlpha(1f);
            findViewById(R.id.summaryCard).setAlpha(1f);
            findViewById(R.id.buttonRow).setAlpha(1f);
            return;
        }

        View[] views = {
                findViewById(R.id.tvTitle),
                findViewById(R.id.tvSubtitle),
                findViewById(R.id.summaryCard),
                findViewById(R.id.buttonRow)
        };
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
                    .setStartDelay(i * getResources().getInteger(R.integer.motion_duration_entrance_stagger))
                    .setInterpolator(new android.view.animation.OvershootInterpolator(1.2f))
                    .start();
        }
    }

    /**
     * Animates the success check icon with a spring scale animation.
     */
    private static final float SPRING_DAMPING_RATIO = 0.7f;

    private void animateSuccessIcon() {
        View ivSuccessIcon = findViewById(R.id.ivSuccessIcon);
        if (ivSuccessIcon == null) return;

        if (isMotionReduced(this)) {
            ivSuccessIcon.setScaleX(1f);
            ivSuccessIcon.setScaleY(1f);
            return;
        }

        ivSuccessIcon.setScaleX(0f);
        ivSuccessIcon.setScaleY(0f);

        SpringForce springForce = new SpringForce(1f);
        springForce.setStiffness(getResources().getInteger(R.integer.spring_stiffness));
        springForce.setDampingRatio(SPRING_DAMPING_RATIO);

        SpringAnimation scaleX = new SpringAnimation(ivSuccessIcon, androidx.dynamicanimation.animation.DynamicAnimation.SCALE_X);
        SpringAnimation scaleY = new SpringAnimation(ivSuccessIcon, androidx.dynamicanimation.animation.DynamicAnimation.SCALE_Y);

        scaleX.setSpring(springForce);
        scaleY.setSpring(springForce);

        scaleX.start();
        scaleY.start();
    }

    /**
     * Checks if motion is reduced via system settings or power save mode.
     */
    private boolean isMotionReduced(Context context) {
        float scale = android.provider.Settings.Global.getFloat(context.getContentResolver(),
                android.provider.Settings.Global.ANIMATOR_DURATION_SCALE, 1f);
        if (scale == 0f) {
            return true;
        }

        android.os.PowerManager pm = (android.os.PowerManager) context.getSystemService(android.content.Context.POWER_SERVICE);
        if (pm != null && pm.isPowerSaveMode()) {
            return true;
        }

        return false;
    }
}