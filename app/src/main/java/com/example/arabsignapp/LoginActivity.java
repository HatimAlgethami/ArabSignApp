package com.example.arabsignapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText email, password;
    private TextInputLayout emailInputLayout, passwordInputLayout;
    private Button createAccount, resetPasswordButton;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Views
        emailInputLayout = findViewById(R.id.emailInputLayout);
        passwordInputLayout = findViewById(R.id.passwordInputLayout);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        createAccount = findViewById(R.id.createAccountBtn);
        resetPasswordButton = findViewById(R.id.reset_password_button);

        // Auto-fill email if provided through Intent
        String preFilledEmail = getIntent().getStringExtra("email");
        if (preFilledEmail != null) {
            email.setText(preFilledEmail);
        }

        // Set Reset Password Button Click Listener
        resetPasswordButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, ResetPasswordActivity.class);
            startActivity(intent);
        });

        // Set Create Account Button Click Listener
        createAccount.setOnClickListener(v -> {
            String emailText = email.getText().toString().trim();
            String passwordText = password.getText().toString().trim();

            if (!validateEmail(emailText) || !validatePassword(passwordText)) {
                return; // Stop execution if validation fails
            }

            // Firebase Authentication
            mAuth = FirebaseAuth.getInstance();
            mAuth.signInWithEmailAndPassword(emailText, passwordText).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Intent intent = new Intent(this, UserMainActivity.class);
                    intent.putExtra("startFragment", "home");
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(this, "خطأ في تسجيل الدخول", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private boolean validateEmail(String emailText) {
        if (emailText.isEmpty()) {
            emailInputLayout.setError("البريد الإلكتروني مطلوب");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(emailText).matches()) {
            emailInputLayout.setError("البريد الإلكتروني غير صالح");
            return false;
        } else {
            emailInputLayout.setError(null);
            return true;
        }
    }

    private boolean validatePassword(String passwordText) {
        if (passwordText.isEmpty()) {
            passwordInputLayout.setError("كلمة المرور مطلوبة");
            return false;
        } else if (passwordText.length() < 6) {
            passwordInputLayout.setError("كلمة المرور يجب أن تكون أطول من 6 أحرف");
            return false;
        } else {
            passwordInputLayout.setError(null);
            return true;
        }
    }
}
