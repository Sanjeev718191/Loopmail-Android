package com.example.loopmail;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class TaskViewHolder extends RecyclerView.ViewHolder {

    TextView subject;
    TextView email;

    public TaskViewHolder(@NonNull View itemView) {
        super(itemView);
        subject = itemView.findViewById(R.id.item_subtext);
        email = itemView.findViewById(R.id.item_subject);
    }
}
