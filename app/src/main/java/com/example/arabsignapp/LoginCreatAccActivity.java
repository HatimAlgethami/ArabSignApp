package com.example.arabsignapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginCreatAccActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_createacctivity);

        Button createAccountBtn = findViewById(R.id.createAccountBtn);
        Button loginBtn = findViewById(R.id.loginBtn);

        createAccountBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, CreateAccountActivity.class);
            startActivity(intent);
        });

        loginBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser fbusr = FirebaseAuth.getInstance().getCurrentUser();
        if (fbusr!=null){
            Intent intent = new Intent(this, UserMainActivity.class);
            startActivity(intent);
            finish();
        }
    }
}

