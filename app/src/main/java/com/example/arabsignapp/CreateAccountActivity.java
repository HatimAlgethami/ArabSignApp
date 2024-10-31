package com.example.arabsignapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class CreateAccountActivity extends AppCompatActivity {
    private EditText username, email, password, confirmPassword;
    private Button createAccount;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_createaccount);

        // Link activity with layout elements
        username = findViewById(R.id.username);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        confirmPassword = findViewById(R.id.confirm_password);
        createAccount = findViewById(R.id.createAccountBtn);

        createAccount.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String usernameText = username.getText().toString().trim();
        String emailText = email.getText().toString().trim();
        String passwordText = password.getText().toString();
        String confirmPasswordText = confirmPassword.getText().toString();

        if (!validateInputs(usernameText, emailText, passwordText, confirmPasswordText)) {
            return; // Stop if validation fails
        }

        HashMap<String, Object> newUserData = new HashMap<>();
        newUserData.put("username", usernameText);
        newUserData.put("email", emailText);
        newUserData.put("user_role", "user");
        newUserData.put("created_at", FieldValue.serverTimestamp());

        mAuth = FirebaseAuth.getInstance();
        mAuth.createUserWithEmailAndPassword(emailText, passwordText).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Intent intent = new Intent(this, LoginActivity.class);
                saveToFirestore(newUserData, intent);
            } else {
                Toast.makeText(this, "Registration failed. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean validateInputs(String usernameText, String emailText, String passwordText, String confirmPasswordText) {
        if (usernameText.isEmpty()) {
            username.setError("Username is required");
            username.requestFocus();
            return false;
        }
        if (emailText.isEmpty()) {
            email.setError("Email is required");
            email.requestFocus();
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(emailText).matches()) {
            email.setError("Enter a valid email");
            email.requestFocus();
            return false;
        }
        if (passwordText.isEmpty()) {
            password.setError("Password is required");
            password.requestFocus();
            return false;
        }
        if (passwordText.length() < 6) {
            password.setError("Password must be at least 6 characters");
            password.requestFocus();
            return false;
        }
        if (!passwordText.equals(confirmPasswordText)) {
            confirmPassword.setError("Passwords do not match");
            confirmPassword.requestFocus();
            return false;
        }
        return true;
    }

    private void saveToFirestore(HashMap<String, Object> newUserData, Intent intent) {
        FirebaseFirestore fsdb = FirebaseFirestore.getInstance();
        fsdb.document("users/" + FirebaseAuth.getInstance().getCurrentUser().getUid())
                .set(newUserData).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        startActivity(intent);
                        Toast.makeText(this, "Account created successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Failed to save data. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
