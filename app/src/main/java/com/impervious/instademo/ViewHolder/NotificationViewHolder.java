package com.impervious.instademo.ViewHolder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.impervious.instademo.R;

public class NotificationViewHolder extends RecyclerView.ViewHolder {

    public ImageView image_profile, post_image;
    public TextView username, text;

    public NotificationViewHolder(@NonNull View itemView) {
        super(itemView);

        image_profile = itemView.findViewById(R.id.image_profile);
        post_image = itemView.findViewById(R.id.post_image);
        username = itemView.findViewById(R.id.username);
        text = itemView.findViewById(R.id.comment);

    }
}
