package com.example.arabsignapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class CreateAccountActivity extends AppCompatActivity {

    private TextInputEditText username, email, password, confirmPassword;
    private TextInputLayout usernameInputLayout, emailInputLayout, passwordInputLayout, confirmPasswordInputLayout;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_createaccount);

        // Link activity with layout elements
        usernameInputLayout = findViewById(R.id.usernameInputLayout);
        emailInputLayout = findViewById(R.id.emailInputLayout);
        passwordInputLayout = findViewById(R.id.passwordInputLayout);
        confirmPasswordInputLayout = findViewById(R.id.confirmPasswordInputLayout);

        username = findViewById(R.id.username);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        confirmPassword = findViewById(R.id.confirm_password);

        findViewById(R.id.createAccountBtn).setOnClickListener(v -> registerUser());
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
                Toast.makeText(this, "فشل في إنشاء الحساب. حاول مرة أخرى.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean validateInputs(String usernameText, String emailText, String passwordText, String confirmPasswordText) {
        boolean isValid = true;

        // Username validation
        if (usernameText.isEmpty()) {
            usernameInputLayout.setError("اسم المستخدم مطلوب");
            isValid = false;
        } else if (!usernameText.matches("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{6,}$")) {
            usernameInputLayout.setError("يجب أن يحتوي اسم المستخدم على أرقام وأحرف ويكون طوله 6 أحرف على الأقل");
            isValid = false;
        } else {
            usernameInputLayout.setError(null);
        }

        // Email validation
        if (emailText.isEmpty()) {
            emailInputLayout.setError("البريد الإلكتروني مطلوب");
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(emailText).matches()) {
            emailInputLayout.setError("البريد الإلكتروني غير صالح");
            isValid = false;
        } else {
            emailInputLayout.setError(null);
        }

        // Password validation
        if (passwordText.isEmpty()) {
            passwordInputLayout.setError("كلمة المرور مطلوبة");
            isValid = false;
        } else if (!passwordText.matches("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{6,}$")) {
            passwordInputLayout.setError("يجب أن تحتوي كلمة المرور على أرقام وأحرف ويكون طولها 6 أحرف على الأقل");
            isValid = false;
        } else {
            passwordInputLayout.setError(null);
        }

        // Confirm Password validation
        if (!passwordText.equals(confirmPasswordText)) {
            confirmPasswordInputLayout.setError("كلمات المرور غير متطابقة");
            isValid = false;
        } else {
            confirmPasswordInputLayout.setError(null);
        }

        return isValid;
    }

    private void saveToFirestore(HashMap<String, Object> newUserData, Intent intent) {
        FirebaseFirestore fsdb = FirebaseFirestore.getInstance();
        fsdb.document("users/" + FirebaseAuth.getInstance().getCurrentUser().getUid())
                .set(newUserData).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Clear back stack and navigate to LoginActivity
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intent.putExtra("email", newUserData.get("email").toString()); // Pass email to LoginActivity
                        startActivity(intent);
                        Toast.makeText(this, "تم إنشاء الحساب بنجاح", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "فشل في حفظ البيانات. حاول مرة أخرى.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
