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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import com.google.android.material.navigation.NavigationBarView;

public class HomeFragment extends Fragment {

    private AlertDialog dialog;
    private String selectedLanguage;

    public HomeFragment() {
        // Required empty public constructor
    }

    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.home_fragment, container, false);
    }

    public void toActivityHm() {
        if (selectedLanguage == null) {
            Toast.makeText(getContext(), "Please select a language", Toast.LENGTH_SHORT).show();
        } else {
            dialog.dismiss();
            Intent intent = new Intent(getActivity(), TranslationActivity.class);
            intent.putExtra("selectedLanguage", selectedLanguage); // Pass the selected language to the activity
            startActivity(intent);
        }
    }

    public void returnFromPopupHm() {
        dialog.dismiss();
    }

    public void popupHm() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.getContext().setTheme(R.style.dialogradius);

        LayoutInflater inflater = this.getLayoutInflater();
        View eventView = inflater.inflate(R.layout.dialog_input, null);

        RadioGroup languageRadioGroup = eventView.findViewById(R.id.language_radio_group);
        RadioButton aslRadioButton = eventView.findViewById(R.id.asl_radio_button);
        RadioButton arslRadioButton = eventView.findViewById(R.id.arsl_radio_button);
        arslRadioButton.setChecked(true);
        selectedLanguage = "ARSL";

        languageRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.asl_radio_button) {
                selectedLanguage = "ASL";
            } else if (checkedId == R.id.arsl_radio_button) {
                selectedLanguage = "ARSL";
            }
        });

        eventView.findViewById(R.id.confirm_button).setOnClickListener(v -> toActivityHm());
        eventView.findViewById(R.id.cancel_confirm_button).setOnClickListener(v -> returnFromPopupHm());

        builder.setView(eventView);
        dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        NavigationBarView navigationBar_v = getActivity().findViewById(R.id.nav_view);
        navigationBar_v.getMenu().findItem(R.id.navigation_home).setChecked(true);

        Button btn = getView().findViewById(R.id.start);
        btn.setOnClickListener(v -> popupHm());

        SharedUtils.setUserGreeting(getView().findViewById(R.id.greetingText));
    }
}
