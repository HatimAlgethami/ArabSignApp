package com.example.arabsignapp;


import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

public class FirstActivity extends AppCompatActivity {

    private Button navigateButton;
    SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_firstactivity);

        sharedPref = getSharedPreferences("appPref",0);

        if (sharedPref.contains("selectedDark")){

            boolean isNightMode = sharedPref.getBoolean("selectedDark",false);
            if (isNightMode){
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            }
            else{
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }

        }

        if (!sharedPref.getBoolean("selectedDark",true)){
            Intent intent = new Intent(FirstActivity.this, LoginCreatAccActivity.class);
            startActivity(intent);
        }



        navigateButton = findViewById(R.id.createAccountBtn);

        navigateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean("firstLaunch",false).apply();

                Intent intent = new Intent(FirstActivity.this, LoginCreatAccActivity.class);
                startActivity(intent);
            }
        });
    }
}
