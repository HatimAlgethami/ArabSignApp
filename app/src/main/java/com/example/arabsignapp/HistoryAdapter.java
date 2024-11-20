package com.example.arabsignapp;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>
        implements DeleteDialog{

    private ArrayList<String> names;
    private ArrayList<String> sessionIds;
    HistoryFragment historyFragment;

    public HistoryAdapter(HistoryFragment historyFragment){
        this.historyFragment = historyFragment;
    }

    public void setNames(ArrayList<String> names) {
        this.names = names;
    }

    public void setSessionIds(ArrayList<String> sessionIds) {
        this.sessionIds = sessionIds;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.history_listview,parent,false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        holder.getTv_session_name().setText(names.get(position));

        holder.getDelete_button().setOnClickListener(v -> {
            new ConfirmDeleteDialog("هل انت متأكد من مسح هذه المحادثة؟",
                    this,position).show(historyFragment.getParentFragmentManager(),"");
        });
        holder.itemView.setOnClickListener(v -> {

            FragmentManager fm = historyFragment.getParentFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.main_view, new DetailsFragment()).addToBackStack("custom");
            ft.commit();
            Intent intent = historyFragment.getActivity().getIntent();
            intent.putExtra("itemName", names.get(position));
            intent.putExtra("itemId", sessionIds.get(position));
        });
    }

    @Override
    public int getItemCount() {
        return names.size();
    }

    @Override
    public void deleteItem(int position) {
        FirebaseFirestore.getInstance().document("history/"+sessionIds.get(position))
                .delete().addOnSuccessListener(unused -> {
                    return;
                })
                .addOnFailureListener(e -> {
                    return;
                });
    }

    public static class HistoryViewHolder extends RecyclerView.ViewHolder{

        private final TextView tv_session_name;
        private final AppCompatButton delete_button;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_session_name = itemView.findViewById(R.id.sessionname);
            delete_button = itemView.findViewById(R.id.history_btn_delete);
        }

        public TextView getTv_session_name() {
            return tv_session_name;
        }

        public AppCompatButton getDelete_button() {
            return delete_button;
        }
    }
}

