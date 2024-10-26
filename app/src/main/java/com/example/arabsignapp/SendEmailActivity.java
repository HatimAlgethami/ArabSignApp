package com.example.arabsignapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SendEmailActivity extends AppCompatActivity {

    private EditText email;
    private Button resetButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sendemail);

        Intent intent= getIntent();

        email = findViewById(R.id.email);





    }
}
