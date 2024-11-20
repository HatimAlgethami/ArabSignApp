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
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import android.widget.Toast;
import com.google.android.material.navigation.NavigationBarView;

public class HomeFragment extends Fragment {

    private AlertDialog dialog;
    private String selectedLanguage = "ARSL";
    private String sessionInput;

    public HomeFragment() {

    }

    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.home_fragment, container, false);
    }

    private void toActivityHm(String chatName) {
        Intent intent = new Intent(getActivity(), TranslationActivity.class);
        intent.putExtra("selectedLanguage", selectedLanguage);
        intent.putExtra("chatName", chatName);
        startActivity(intent);
        dialog.dismiss();
    }

    private void returnFromPopupHm() {
        dialog.dismiss();
    }

    public void popupHm() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.getContext().setTheme(R.style.dialogradius);

        LayoutInflater inflater = this.getLayoutInflater();
        View eventView = inflater.inflate(R.layout.dialog_input, null);

        // Initialize views
        TextInputLayout chatNameInputLayout = eventView.findViewById(R.id.chat_name_input_layout);
        TextInputEditText chatNameInput = eventView.findViewById(R.id.chat_name_input);
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

        eventView.findViewById(R.id.confirm_button).setOnClickListener(v -> {
            String chatName = chatNameInput.getText().toString().trim();

            if (chatName.isEmpty()) {
                chatNameInputLayout.setError("اسم المحادثة مطلوب");
                chatNameInput.requestFocus();
            } else {
                chatNameInputLayout.setError(null);
                toActivityHm(chatName);
            }
        });

        eventView.findViewById(R.id.cancel_confirm_button).setOnClickListener(v -> returnFromPopupHm());

        builder.setView(eventView);
        dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        NavigationBarView navigationBarView = getActivity().findViewById(R.id.nav_view);
        navigationBarView.getMenu().findItem(R.id.navigation_home).setChecked(true);

        Button startButton = view.findViewById(R.id.start);
        startButton.setOnClickListener(v -> popupHm());

        SharedUtils.setUserGreeting(view.findViewById(R.id.greetingText));
    }
}
