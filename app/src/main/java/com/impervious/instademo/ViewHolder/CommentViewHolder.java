package com.impervious.instademo.ViewHolder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.impervious.instademo.R;

public class CommentViewHolder extends RecyclerView.ViewHolder {

    public ImageView image_profile;
    public TextView username, comment;

    public CommentViewHolder(@NonNull View itemView) {
        super(itemView);

        image_profile = itemView.findViewById(R.id.image_profile);
        username = itemView.findViewById(R.id.username);
        comment = itemView.findViewById(R.id.comment);

    }
}
