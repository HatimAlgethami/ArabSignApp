package com.example.arabsignapp;

import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

public class ResetPasswordActivity extends AppCompatActivity {

    private TextInputEditText resetEmail;
    private TextInputLayout resetEmailLayout;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reset_password_activity);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize Views
        resetEmailLayout = findViewById(R.id.resetEmailLayout);
        resetEmail = findViewById(R.id.resetEmail);

        // Handle dynamic text alignment
        setupDynamicTextAlignment(resetEmail);

        findViewById(R.id.confirmResetButton).setOnClickListener(v -> sendPasswordResetEmail());
    }

    private void setupDynamicTextAlignment(TextInputEditText editText) {
        editText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                String currentLanguage = imm.getCurrentInputMethodSubtype().getLanguageTag();

                if (currentLanguage != null && currentLanguage.startsWith("ar")) {
                    // Arabic alignment (right)
                    editText.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
                } else {
                    // English or other alignment (left)
                    editText.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
                }
            }
        });
    }

    private void sendPasswordResetEmail() {
        String email = resetEmail.getText().toString().trim();

        // Clear previous error
        resetEmailLayout.setError(null);

        // Validate Email
        if (email.isEmpty()) {
            resetEmailLayout.setError("يرجى إدخال البريد الإلكتروني");
            return;
        }

        // Send password reset email
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "تم إرسال البريد لإعادة تعيين كلمة المرور", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        resetEmailLayout.setError("حدث خطأ أثناء إرسال البريد");
                    }
                });
    }
}
