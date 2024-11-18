package com.example.arabsignapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class sendFeedbackFragment extends Fragment {

    private TextInputLayout feedbackTitleLayout, feedbackMessageLayout;
    private TextInputEditText feedbackTitle, feedbackMessage;

    public sendFeedbackFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_send_feedback, container, false);

        // Initialize Views
        feedbackTitleLayout = view.findViewById(R.id.feedbackTitleLayout);
        feedbackMessageLayout = view.findViewById(R.id.feedbackMessageLayout);
        feedbackTitle = view.findViewById(R.id.feedbackTitle);
        feedbackMessage = view.findViewById(R.id.feedbackMessage);

        // Handle dynamic text alignment
        setupDynamicTextAlignment(feedbackTitle);
        setupDynamicTextAlignment(feedbackMessage);

        view.findViewById(R.id.sendFeedbackButton).setOnClickListener(v -> {
            if (validateInputs()) {
                storeFeedbackInDatabase(feedbackTitle.getText().toString(), feedbackMessage.getText().toString());
            }
        });

        return view;
    }

    private void setupDynamicTextAlignment(TextInputEditText editText) {
        editText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(requireActivity().INPUT_METHOD_SERVICE);
                String currentLanguage = imm.getCurrentInputMethodSubtype().getLanguageTag();

                if (currentLanguage != null && currentLanguage.startsWith("ar")) {
                    editText.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
                } else {
                    editText.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
                }
            }
        });
    }

    private boolean validateInputs() {
        boolean isValid = true;

        String title = feedbackTitle.getText().toString().trim();
        String message = feedbackMessage.getText().toString().trim();

        // Clear previous errors
        feedbackTitleLayout.setError(null);
        feedbackMessageLayout.setError(null);

        // Validate Title
        if (title.isEmpty()) {
            feedbackTitleLayout.setError("العنوان مطلوب");
            isValid = false;
        }

        // Validate Message
        if (message.isEmpty()) {
            feedbackMessageLayout.setError("الرسالة مطلوبة");
            isValid = false;
        }

        return isValid;
    }

    private void storeFeedbackInDatabase(String title, String message) {
        FirebaseFirestore fsdb = FirebaseFirestore.getInstance();
        String userUid;

        try {
            userUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } catch (NullPointerException e) {
            Toast.makeText(getContext(), "User not logged in.", Toast.LENGTH_SHORT).show();
            return;
        }

        DocumentReference userRef = fsdb.collection("users").document(userUid);

        HashMap<String, Object> feedbackData = new HashMap<>();
        feedbackData.put("title", title);
        feedbackData.put("message", message);
        feedbackData.put("created_at", FieldValue.serverTimestamp());
        feedbackData.put("user_id", userRef);

        fsdb.collection("feedback").add(feedbackData).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(getContext(), "تم إرسال رأيك بنجاح!", Toast.LENGTH_SHORT).show();
                getParentFragmentManager().popBackStack(); // Return to the previous fragment
            } else {
                Toast.makeText(getContext(), "حدث خطأ أثناء الإرسال.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
