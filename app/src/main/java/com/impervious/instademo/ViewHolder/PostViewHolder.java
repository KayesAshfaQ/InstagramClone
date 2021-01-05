package com.impervious.instademo.ViewHolder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.impervious.instademo.R;

public class PostViewHolder extends RecyclerView.ViewHolder {

    public ImageView image_profile, post_image, like, comment, save, more;
    public TextView username, likes, publisher, description, comments;

    public PostViewHolder(@NonNull View itemView) {
        super(itemView);

        image_profile = itemView.findViewById(R.id.image_profile);
        post_image = itemView.findViewById(R.id.post_image);
        like = itemView.findViewById(R.id.like);
        comment = itemView.findViewById(R.id.comment);
        save = itemView.findViewById(R.id.save);
        more = itemView.findViewById(R.id.more);

        username = itemView.findViewById(R.id.username);
        likes = itemView.findViewById(R.id.likes);
        publisher = itemView.findViewById(R.id.publisher);
        description = itemView.findViewById(R.id.description);
        comments = itemView.findViewById(R.id.comments);


    }
}
