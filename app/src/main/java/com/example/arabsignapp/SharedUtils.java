package com.example.arabsignapp;

import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class SharedUtils {

    public static void setUserGreeting(TextView tv){
        FirebaseFirestore fsdb = FirebaseFirestore.getInstance();
        fsdb.collection("users")
                .document(""+ FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addSnapshotListener((value, error) -> {
                    if (error!=null){
                        return;
                    }

                    if (value!=null && value.exists()){
                        CharSequence greeting = value.getString("username");
                        tv.append(greeting);
                    }
                });
    }

    public static void setUserProfileName(TextView tv){
        FirebaseFirestore fsdb = FirebaseFirestore.getInstance();
        fsdb.collection("users")
                .document(""+ FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addSnapshotListener((value, error) -> {
                    if (error!=null){
                        return;
                    }

                    if (value!=null && value.exists()){
                        String greeting = value.getString("username");
                        tv.setText(greeting);
                    }
                });
    }
}
