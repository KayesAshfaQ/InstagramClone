package com.impervious.instademo.ViewHolder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.impervious.instademo.R;

public class StoryViewHolder extends RecyclerView.ViewHolder {

    public ImageView story_photo, story_photo_seen, story_plus;
    public TextView story_username, add_story_txt;

    public StoryViewHolder(@NonNull View itemView) {
        super(itemView);

        story_photo = itemView.findViewById(R.id.story_photo);
        story_photo_seen = itemView.findViewById(R.id.story_photo_seen);
        story_plus = itemView.findViewById(R.id.story_plus);
        story_username = itemView.findViewById(R.id.story_username);
        add_story_txt = itemView.findViewById(R.id.add_story_txt);

    }

}
