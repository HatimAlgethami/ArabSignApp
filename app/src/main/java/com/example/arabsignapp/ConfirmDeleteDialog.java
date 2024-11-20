package com.example.arabsignapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.DialogFragment;

public class ConfirmDeleteDialog extends DialogFragment {

    private final String message;
    private final DeleteDialog adapter;
    private final int position;

    public ConfirmDeleteDialog(String message, DeleteDialog adapter,int position){
        this.message = message;
        this.adapter = adapter;
        this.position = position;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());


        View view = requireActivity().getLayoutInflater().inflate(R.layout.dialog_delete,null);
        dialogBuilder.setView(view);
        TextView tv_title = view.findViewById(R.id.delete_title);
        AppCompatButton yes_button = view.findViewById(R.id.delete_yes_button);
        AppCompatButton no_button = view.findViewById(R.id.delete_no_button);

        tv_title.setText(message);

        yes_button.setOnClickListener(v -> {
            adapter.deleteItem(position);
            dismiss();
        });

        no_button.setOnClickListener(v -> {
            dismiss();
        });

        return dialogBuilder.create();
    }
}
