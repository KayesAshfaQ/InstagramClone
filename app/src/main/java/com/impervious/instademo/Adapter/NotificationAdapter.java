package com.impervious.instademo.Adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.impervious.instademo.Fragment.PostDetailsFragment;
import com.impervious.instademo.Fragment.ProfileFragment;
import com.impervious.instademo.Model.Notification;
import com.impervious.instademo.Model.Post;
import com.impervious.instademo.Model.User;
import com.impervious.instademo.R;
import com.impervious.instademo.ViewHolder.NotificationViewHolder;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationViewHolder> {

    Context context;
    List<Notification> notificationList;

    public NotificationAdapter(Context context, List<Notification> notificationList) {
        this.context = context;
        this.notificationList = notificationList;
    }


    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.notification_item, parent, false);
        return new NotificationViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {

        Notification notification = notificationList.get(position);

        holder.text.setText(notification.getText());
        getUserInfo(holder.image_profile, holder.username, notification.getUserId());

        if (notification.getIsPost()){
            holder.post_image.setVisibility(View.VISIBLE);
            getPostImage(holder.post_image, notification.getPostId());
        }else {
            holder.post_image.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (notification.getIsPost()){

                    SharedPreferences.Editor editor = context.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
                    editor.putString("postId", notification.getPostId());
                    editor.apply();

                    ((FragmentActivity)context).getSupportFragmentManager().beginTransaction()
                                                            .replace(R.id.fragment_container, new PostDetailsFragment()).commit();

                }else {

                    SharedPreferences.Editor editor = context.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
                    editor.putString("profileId", notification.getUserId());
                    editor.apply();

                    ((FragmentActivity)context).getSupportFragmentManager().beginTransaction()
                                                                .replace(R.id.fragment_container, new ProfileFragment()).commit();

                }

            }
        });



    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    private void getUserInfo(ImageView image_profile, TextView username, String publisherId) {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(publisherId);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                User user = snapshot.getValue(User.class);

                Glide.with(context).load(user.getImgUrl()).into(image_profile);
                username.setText(user.getUsername());

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void getPostImage(ImageView post_image, final String postId) {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts").child(postId);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                Post post = snapshot.getValue(Post.class);

                Glide.with(context).load(post.getPostImage()).into(post_image);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

}
