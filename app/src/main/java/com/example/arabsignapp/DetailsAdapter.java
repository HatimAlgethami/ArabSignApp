package com.example.arabsignapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;

public class DetailsAdapter extends RecyclerView.Adapter<DetailsAdapter.DetailsViewHolder>
        implements DeleteDialog{

    private final String sessionId;
    private ArrayList<Word> sentence;
    private DetailsFragment detailsFragment;

    public DetailsAdapter(String sessionId,DetailsFragment detailsFragment) {
        this.sessionId = sessionId;
        this.sentence = new ArrayList<>();
        this.detailsFragment = detailsFragment;
    }

    public void setSentence(ArrayList<Word> sentence) {
        this.sentence = sentence;
    }

    @Override
    public int getItemCount() {
        return sentence.size();
    }

    @NonNull
    @Override
    public DetailsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.details_listview,parent,false);
        return new DetailsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DetailsViewHolder holder, int position) {

        Word currentSentence = sentence.get(position);

        String accuracyText = "نسبة الدقة: "+currentSentence.getAccuracy()+"٪";

        holder.getTv_sentence().setText(currentSentence.getWord());
        holder.getTv_accuracy().setText(accuracyText);

        holder.getDeleteButton().setOnClickListener(v -> {
            new ConfirmDeleteDialog("هل انت متأكد من مسح هذه الكلمة؟",
                    this,position).show(detailsFragment.getParentFragmentManager(),"");
        });

    }

    public void deleteItem(int position){
        FirebaseFirestore fsdb = FirebaseFirestore.getInstance();
        fsdb.document("history/" + sessionId)
                .update("sentence",FieldValue.arrayRemove
                        (sentence.get(position)))
                .addOnSuccessListener(unused -> {
                    return;
                }).addOnFailureListener(e -> {
                    return;
                });
    }

    public static class DetailsViewHolder extends RecyclerView.ViewHolder{

        private TextView tv_sentence,tv_accuracy;
        private AppCompatButton deleteButton;

        public DetailsViewHolder(@NonNull View itemView) {
            super(itemView);

            tv_sentence = itemView.findViewById(R.id.tv_word);
            tv_accuracy = itemView.findViewById(R.id.tv_accuracy);
            deleteButton = itemView.findViewById(R.id.btn_delete);
        }

        public TextView getTv_sentence() {
            return tv_sentence;
        }

        public TextView getTv_accuracy() {
            return tv_accuracy;
        }

        public AppCompatButton getDeleteButton() {
            return deleteButton;
        }
    }
}
