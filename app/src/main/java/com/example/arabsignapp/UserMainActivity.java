package com.example.arabsignapp;

import android.os.Bundle;
import android.content.SharedPreferences;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.navigation.NavigationBarView;

public class UserMainActivity extends AppCompatActivity implements NavigationBarView.OnItemSelectedListener {

    private NavigationBarView navigationBar_v;
    private SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_main_activity);

        sharedPref = this.getSharedPreferences("appPref", 0);

        if (savedInstanceState == null) {
            replaceFragment(new HomeFragment());
        }

        // Initialize BottomNavigationView
        navigationBar_v = findViewById(R.id.nav_view);
        navigationBar_v.setOnItemSelectedListener(this);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment selectedFragment = null;

        if (item.getItemId() == R.id.navigation_home) {
            selectedFragment = new HomeFragment();
        } else if (item.getItemId() == R.id.navigation_chat) {
            selectedFragment = new HistoryFragment();
        } else if (item.getItemId() == R.id.navigation_profile) {
            selectedFragment = new ProfileFragment();
        }

        if (selectedFragment != null) {
            replaceFragment(selectedFragment);
        }
        return true;
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(
                android.R.anim.fade_in,  // Enter animation
                android.R.anim.fade_out // Exit animation
        );
        fragmentTransaction.replace(R.id.main_view, fragment);
        fragmentTransaction.commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.remove("selectedFragment");
        editor.apply();
    }
}
