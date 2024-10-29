package com.example.arabsignapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment {

    //the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    boolean isNightMode;
    SharedPreferences sharedPref;
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public ProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ProfileFragment.
     */
    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.profile_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        try {
            NavigationBarView navigationBar_v =getActivity().findViewById(R.id.nav_view);
            navigationBar_v.getMenu().findItem(R.id.navigation_profile).setChecked(true);
            sharedPref = getActivity().getSharedPreferences("appPref",0);
            //go to feedback activity

//            Button button = getActivity().findViewById(R.id.btn_feedback);
//            button.setOnClickListener(v -> {
//                Intent intent = new Intent(getActivity(), Activity.class);
//                getActivity().startActivity(intent);
//            });
            Button button = getView().findViewById(R.id.btn_logout);
            button.setOnClickListener(v -> {
                sharedPref.edit().putInt("selectedFragment",2).apply();

                Intent intent = new Intent(getActivity(), LoginCreatAccActivity.class);
                FirebaseAuth.getInstance().signOut();
                getActivity().startActivity(intent);
                getActivity().finish();
            });

            MaterialSwitch ms = getView().findViewById(R.id.modeswitch);
            setIsNightMode();
            ms.setChecked(isNightMode);
            ms.setOnClickListener((v) -> {
                int newMode;
                if (ms.isChecked()){
                    newMode=AppCompatDelegate.MODE_NIGHT_YES;
                }
                else {
                    newMode=AppCompatDelegate.MODE_NIGHT_NO;
                }
                sharedPref.edit().putBoolean("selectedDark",ms.isChecked()).apply();
//                sharedPref.edit().putBoolean("isRecreated",true).apply();
                AppCompatDelegate.setDefaultNightMode(newMode);

            });
            SharedUtils.setUserProfileName(getView().findViewById(R.id.usrnm));
        }

        catch (NullPointerException ignored){

        }
        super.onViewCreated(view, savedInstanceState);
    }

    public void setIsNightMode(){
        int modeMask = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;

        isNightMode=modeMask==Configuration.UI_MODE_NIGHT_YES;
    }
}