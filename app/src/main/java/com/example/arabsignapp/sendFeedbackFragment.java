package com.example.arabsignapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link sendFeedbackFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class sendFeedbackFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public sendFeedbackFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment sendFeedbackFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static sendFeedbackFragment newInstance(String param1, String param2) {
        sendFeedbackFragment fragment = new sendFeedbackFragment();
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
        return inflater.inflate(R.layout.fragment_send_feedback, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        try {
            Button sendFeedbackButton = getView().findViewById(R.id.sendFeedbackButton);
            sendFeedbackButton.setOnClickListener(v -> {
                EditText titleET =getView().findViewById(R.id.feedbackTitle);
                EditText messageET =getView().findViewById(R.id.feedbackMessage);
                String title = String.valueOf(titleET.getText());
                String message = String.valueOf(messageET.getText());
                if (title.isBlank()){
                    titleET.setError("Title is required");
                    titleET.requestFocus();
                    return;
                }
                if (message.isBlank()){
                    messageET.setError("Message is required");
                    messageET.requestFocus();
                    return;
                }
                storeFeedbackInDatabase(title,message);
            });
        }
        catch (Exception e){

        }

    }

    private void storeFeedbackInDatabase(String title,String message){
        FirebaseFirestore fsdb = FirebaseFirestore.getInstance();
        String userUid;
        try {
            userUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }
        catch (NullPointerException npEx){
            return;
        }
        DocumentReference dr = fsdb.collection("users").document(userUid);

        HashMap<String,Object> feedbackData = new HashMap<>();
        feedbackData.put("title",title);
        feedbackData.put("message",message);
        feedbackData.put("created_at", FieldValue.serverTimestamp());
        feedbackData.put("user_id",dr);
        fsdb.collection("feedback")
                .add(feedbackData)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        getParentFragmentManager().popBackStack();
                    } else {
                        Toast.makeText(getContext(), "Something went wrong.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}