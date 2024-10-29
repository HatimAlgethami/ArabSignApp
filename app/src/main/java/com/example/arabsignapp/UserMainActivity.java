package com.example.arabsignapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

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
            addFirstFragment();
       }
        catch (Exception e){
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        findSelectedFragment(navigationBar_v.getSelectedItemId());
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

    private void addFirstFragment(){
        findSelectedFragment(navigationBar_v.getSelectedItemId());
        Fragment fragment;
        switch (selectedFragment){
            case 2:
                fragment = new HomeFragment();
                break;
            case 1:
                fragment = new HistoryFragment();
                break;
            case 0:
                fragment = new ProfileFragment();
                break;
            default:
                return;
        }

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.main_view, fragment).setReorderingAllowed(true);
        ft.commit();
    }

    private void findSelectedFragment(int fragId){
        if(fragId == R.id.navigation_home){
            selectedFragment = 2;
        }
        else if(fragId == R.id.navigation_chat) {
            selectedFragment = 1;
        }
        else if(fragId == R.id.navigation_profile){
            selectedFragment = 0;
        }
    }
}
