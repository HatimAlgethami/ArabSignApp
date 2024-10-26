package com.example.arabsignapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class ResetPasswordActivity extends AppCompatActivity {
    EditText ed_password;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reset_password_activity);
    }

    public void toActivity(View view) {
        Intent intent = new Intent(this, UserMainActivity.class);
        startActivity(intent);
    }
}

