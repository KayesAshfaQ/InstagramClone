package com.impervious.instademo.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.impervious.instademo.AddStoryActivity;
import com.impervious.instademo.Model.Story;
import com.impervious.instademo.Model.User;
import com.impervious.instademo.R;
import com.impervious.instademo.StoryActivity;
import com.impervious.instademo.ViewHolder.StoryViewHolder;

import java.util.List;

public class StoryAdapter extends RecyclerView.Adapter<StoryViewHolder> {

    Context context;
    List<Story> storyList;

    public StoryAdapter(Context context, List<Story> storyList) {
        this.context = context;
        this.storyList = storyList;
    }

    @NonNull
    @Override
    public StoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view;
        if (viewType == 0) {
            view = LayoutInflater.from(context).inflate(R.layout.add_story_item, parent, false);
        } else {
            view = LayoutInflater.from(context).inflate(R.layout.story_item, parent, false);
        }
        return new StoryViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull StoryViewHolder holder, int position) {

        Story story = storyList.get(position);

        userInfo(holder, story.getUserId(), position);

        if (holder.getAdapterPosition() != 0){
            seenStory(holder, story.getUserId());
        }

        if (holder.getAdapterPosition() == 0){
            myStory(holder.add_story_txt, holder.story_plus, false);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.getAdapterPosition() == 0){
                    myStory(holder.add_story_txt, holder.story_plus, true);
                }else {
                   Intent intent = new Intent(context, StoryActivity.class);
                   intent.putExtra("userId", story.getUserId());
                   context.startActivity(intent);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return storyList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return 0;
        }
        return 1;
    }

    private void userInfo(final StoryViewHolder viewHolder, final String userId, final int pos) {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(userId);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);

                Glide.with(context).load(user.getImgUrl()).into(viewHolder.story_photo);
                if (pos != 0) {
                    Glide.with(context).load(user.getImgUrl()).into(viewHolder.story_photo_seen);
                    viewHolder.story_username.setText(user.getUsername());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void myStory(TextView textView, ImageView imageView, boolean click) {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Story")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int count = 0;
                long currentTime = System.currentTimeMillis();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Story story = dataSnapshot.getValue(Story.class);

                    if (currentTime > story.getTimeStart() && currentTime < story.getTimeEnd()) {
                        count++;
                    }
                }

                if (click) {
                    if (count > 0){
                        AlertDialog alertDialog = new AlertDialog.Builder(context).create();
                        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "view story",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent intent = new Intent(context, StoryActivity.class);
                                        intent.putExtra("userId", FirebaseAuth.getInstance().getCurrentUser().getUid());
                                        context.startActivity(intent);
                                        dialog.dismiss();
                                    }
                                });

                        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "add story",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent intent = new Intent(context, AddStoryActivity.class);
                                        context.startActivity(intent);
                                        dialog.dismiss();
                                    }
                                });
                        alertDialog.show();
                    }else {
                        Intent intent = new Intent(context, AddStoryActivity.class);
                        context.startActivity(intent);
                    }

                } else {
                    if (count > 0) {
                        textView.setText("My story");
                        imageView.setVisibility(View.GONE);
                    } else {
                        textView.setText("Add story");
                        imageView.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void seenStory(final StoryViewHolder viewHolder, String userId) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Story").child(userId);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                int count = 0;
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {

                    if (!dataSnapshot.child("views")
                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).exists() &&
                            System.currentTimeMillis() < dataSnapshot.getValue(Story.class).getTimeEnd()
                    ) {
                        count++;
                    }

                }

                if (count > 0){
                    viewHolder.story_photo.setVisibility(View.VISIBLE);
                    viewHolder.story_photo_seen.setVisibility(View.GONE);
                }else {
                    viewHolder.story_photo.setVisibility(View.GONE);
                    viewHolder.story_photo_seen.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}
