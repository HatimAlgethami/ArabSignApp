package com.example.arabsignapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.navigation.NavigationBarView;

public class UserMainActivity extends AppCompatActivity implements NavigationBarView.OnItemSelectedListener {

    NavigationBarView navigationBar_v;
    UserMainActivityBinding binding;
    int selectedFragment;
    SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        binding = UserMainActivityBinding.inflate(getLayoutInflater());
//        setContentView(binding.getRoot());
        setContentView(R.layout.user_main_activity);
        navigationBar_v = findViewById(R.id.nav_view);
        navigationBar_v.setOnItemSelectedListener(this);
        try{
            sharedPref = this.getSharedPreferences("appPref",0);
            if (sharedPref.contains("selectedFragment")){
                selectedFragment = sharedPref.getInt("selectedFragment",selectedFragment);
            }
            else{
                selectedFragment = navigationBar_v.getMenu().size()-1;
            }
            onNavigationItemSelected(navigationBar_v.getMenu().getItem(selectedFragment));
       }
        catch (Exception e){
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("selectedFragment",selectedFragment).apply();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.navigation_home){
            selectedFragment = 2;
            replaceFragment(new HomeFragment());
        }
        else if(item.getItemId() == R.id.navigation_chat) {
            selectedFragment = 1;
            replaceFragment(new HistoryFragment());
        }
        else if(item.getItemId() == R.id.navigation_profile){
            selectedFragment = 0;
            replaceFragment(new ProfileFragment());
        }
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("selectedFragment",selectedFragment).apply();
        return true;
    }
    private void replaceFragment(Fragment fragment){
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.main_view, fragment).addToBackStack("custom").setReorderingAllowed(true);
        ft.commit();
    }
}
