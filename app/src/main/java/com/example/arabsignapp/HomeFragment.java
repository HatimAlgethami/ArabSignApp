package com.example.arabsignapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.material.navigation.NavigationBarView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */

public class HomeFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    AlertDialog dialog;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
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
        return inflater.inflate(R.layout.home_fragment, container, false);
    }

    public void toActivityHm() {
        dialog.dismiss();
        Intent intent = new Intent(getActivity(), TranslationActivity.class);
        startActivity(intent);
    }

    public void returnFromPopupHm() {
        dialog.dismiss();
    }

    public void popupHm() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.getContext().setTheme(R.style.dialogradius);

        LayoutInflater inflater = this.getLayoutInflater();
        View eventView = inflater.inflate(R.layout.dialog_input,null);

        eventView.findViewById(R.id.confirm_button).setOnClickListener(v -> toActivityHm());
        eventView.findViewById(R.id.cancel_confirm_button).setOnClickListener(v -> returnFromPopupHm());

        builder.setView(eventView);
        dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        NavigationBarView navigationBar_v =getActivity().findViewById(R.id.nav_view);
        navigationBar_v.getMenu().findItem(R.id.navigation_home).setChecked(true);
        Button btn = getView().findViewById(R.id.start);
        btn.setOnClickListener(v-> popupHm());
        String arabicGreeting = "\u202B"+"مرحبا, "+"\u202C";
        SharedUtils.setUserGreeting(getView().findViewById(R.id.greetingText),arabicGreeting);
    }

}