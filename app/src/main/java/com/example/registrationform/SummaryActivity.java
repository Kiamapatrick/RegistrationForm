package com.example.registrationform;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

/**
 * Summary screen showing the registration details for confirmation.
 * Does not store or log any password data.
 */
public class SummaryActivity extends AppCompatActivity {

    private MaterialCardView summaryCard;
    private MaterialCardView successCard;
    private LinearLayout buttonRow;

    private TextView tvNameValue;
    private TextView tvDobValue;
    private TextView tvEmailValue;

    private MaterialButton btnEditDetails;
    private MaterialButton btnConfirm;

    private RegistrationData registrationData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_summary);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Extract registration data from Intent
        Intent intent = getIntent();
        registrationData = intent.getParcelableExtra(RegistrationData.EXTRA_KEY);

        // Handle missing data
        if (registrationData == null) {
            finish();
            return;
        }

        initViews();
        populateSummary();
        setupButtons();
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
    }

    private void populateSummary() {
        tvNameValue.setText(registrationData.name);
        tvDobValue.setText(RegistrationFormatter.formatDob(registrationData.dobUtcMillis));
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
    }

    private void showSuccessState() {
        summaryCard.setVisibility(View.GONE);
        successCard.setVisibility(View.VISIBLE);
        buttonRow.setVisibility(View.GONE);
    }
}