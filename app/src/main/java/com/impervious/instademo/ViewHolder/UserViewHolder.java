package com.impervious.instademo.ViewHolder;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.impervious.instademo.R;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserViewHolder extends RecyclerView.ViewHolder {

    public TextView username;
    public TextView fullname;
    public CircleImageView image_profile;
    public Button btn_follow;

    public UserViewHolder(@NonNull View itemView) {
        super(itemView);

        username = itemView.findViewById(R.id.username);
        fullname = itemView.findViewById(R.id.fullname);
        image_profile = itemView.findViewById(R.id.image_profile);
        btn_follow = itemView.findViewById(R.id.btn_follow);

    }

}
