package com.example.arabsignapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private EditText email, password;
    private Button createAccount, resetPasswordButton;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Link the activity with the layout
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        createAccount = findViewById(R.id.createAccountBtn);
        resetPasswordButton = findViewById(R.id.reset_password_button); // Updated ID

        // Set OnClickListener for resetPasswordButton to navigate to ResetPasswordActivity
        resetPasswordButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, ResetPasswordActivity.class);
            startActivity(intent);
        });

        createAccount.setOnClickListener(v -> {
            Intent intent = new Intent(this, UserMainActivity.class);
            String ST_email = email.getText().toString();
            String ST_password = password.getText().toString();

            if (ST_email.isEmpty() || ST_password.isEmpty()) {
                Toast.makeText(this, "Make sure all fields are filled", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth = FirebaseAuth.getInstance();
            Log.d("TAG", ST_email + " " + ST_password);
            mAuth.signInWithEmailAndPassword(ST_email, ST_password).addOnCompleteListener(Task -> {
                if (Task.isSuccessful()) {
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}
