package com.example.arabsignapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.navigation.NavigationBarView;

public class UserMainActivity extends AppCompatActivity implements NavigationBarView.OnItemSelectedListener {

    NavigationBarView navigationBar_v;
    int selectedFragment;
    SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_main_activity);

        navigationBar_v = findViewById(R.id.nav_view);
        navigationBar_v.setOnItemSelectedListener(this);

        try {
            sharedPref = this.getSharedPreferences("appPref", 0);

            // Check if a specific fragment is passed in the intent
            String startFragment = getIntent().getStringExtra("startFragment");
            if (startFragment != null && startFragment.equals("home")) {
                selectedFragment = 2; // Force HomeFragment
            } else if (sharedPref.contains("selectedFragment")) {
                selectedFragment = sharedPref.getInt("selectedFragment", 2); // Use saved state
            } else {
                selectedFragment = 2; // Default to HomeFragment
            }

            addFirstFragment();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        findSelectedFragment(navigationBar_v.getSelectedItemId());
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("selectedFragment", selectedFragment).apply();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.navigation_home) {
            selectedFragment = 2;
            replaceFragment(new HomeFragment());
        } else if (item.getItemId() == R.id.navigation_chat) {
            selectedFragment = 1;
            replaceFragment(new HistoryFragment());
        } else if (item.getItemId() == R.id.navigation_profile) {
            selectedFragment = 0;
            replaceFragment(new ProfileFragment());
        }
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("selectedFragment", selectedFragment).apply();
        return true;
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        // Add custom fade animations
        ft.setCustomAnimations(
                R.anim.fade_in,  // Custom fade-in animation
                R.anim.fade_out  // Custom fade-out animation
        );

        ft.replace(R.id.main_view, fragment).addToBackStack(null).setReorderingAllowed(true);
        ft.commit();
    }

    private void addFirstFragment() {
        Fragment fragment;

        // Default to HomeFragment
        if (selectedFragment == 2) {
            fragment = new HomeFragment();
        } else if (selectedFragment == 1) {
            fragment = new HistoryFragment();
        } else {
            fragment = new ProfileFragment();
        }

        // Update selected navigation item
        navigationBar_v.getMenu().findItem(R.id.navigation_home).setChecked(selectedFragment == 2);
        navigationBar_v.getMenu().findItem(R.id.navigation_chat).setChecked(selectedFragment == 1);
        navigationBar_v.getMenu().findItem(R.id.navigation_profile).setChecked(selectedFragment == 0);

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        // Add custom fade animations
        ft.setCustomAnimations(
                R.anim.fade_in,
                R.anim.fade_out
        );

        ft.replace(R.id.main_view, fragment).setReorderingAllowed(true);
        ft.commit();
    }

    private void findSelectedFragment(int fragId) {
        if (fragId == R.id.navigation_home) {
            selectedFragment = 2;
        } else if (fragId == R.id.navigation_chat) {
            selectedFragment = 1;
        } else if (fragId == R.id.navigation_profile) {
            selectedFragment = 0;
        }
    }
}
