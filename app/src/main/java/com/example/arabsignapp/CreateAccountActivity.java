package com.example.arabsignapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
// ubdate

public class CreateAccountActivity extends AppCompatActivity {
    private EditText username, email, password, confirmPassword;
    private Button createAccount;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_createaccount);
//link the actvity with the layout
        username = findViewById(R.id.username);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        confirmPassword = findViewById(R.id.confirm_password);
        createAccount = findViewById(R.id.createAccountBtn);


        createAccount.setOnClickListener(v -> {
               String usernameText = username.getText().toString();
                String emailText = email.getText().toString();
                String passwordText = password.getText().toString();
                String confirmPasswordText = confirmPassword.getText().toString();

                HashMap<String,Object> newUserData = new HashMap<>();
                newUserData.put("username", usernameText);
                newUserData.put("email", emailText);
//                newUserData.put("password", passwordText);
                newUserData.put("user_role", "user");
                newUserData.put("created_at", FieldValue.serverTimestamp());

                if (usernameText.isEmpty() || emailText.isEmpty() || passwordText.isEmpty() || confirmPasswordText.isEmpty()) {
                    Toast.makeText(this, "Make sure all fields are filled", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(!passwordText.equals(confirmPasswordText)){
                    Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                    return;
                }
                mAuth = FirebaseAuth.getInstance();
                mAuth.createUserWithEmailAndPassword(emailText, passwordText).addOnCompleteListener(Task ->{
                    if (Task.isSuccessful()) {
                        Intent intent = new Intent(this, LoginActivity.class);
//                        handleDatabase(newUserData);
                        handleFirestore(newUserData,intent);
//                        startActivity(intent);
                    }
                    else{
                        Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
                    }
                });
        });
    }

    private void handleDatabase(HashMap<String,String> newUserData) {
        DatabaseReference db = FirebaseDatabase.getInstance().getReference("users/"+ FirebaseAuth.getInstance().getCurrentUser().getUid());
        db.setValue(newUserData).addOnCompleteListener(Task->{
            if(Task.isSuccessful()){
                Toast.makeText(this, "Account created successfully", Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleFirestore(HashMap<String,Object> newUserData,Intent intent){
        FirebaseFirestore fsdb = FirebaseFirestore.getInstance();
        fsdb.document("users/"+FirebaseAuth.getInstance().getCurrentUser().getUid())
                .set(newUserData).addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        if (mAuth.getCurrentUser()!=null){
                            mAuth.signOut();
                        }
                        startActivity(intent);
                        finish();
                    }
                    else{
                        Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
