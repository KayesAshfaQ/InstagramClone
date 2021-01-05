package com.impervious.instademo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.impervious.instademo.Model.Story;
import com.impervious.instademo.Model.User;

import java.util.ArrayList;
import java.util.List;

import jp.shts.android.storiesprogressview.StoriesProgressView;

public class StoryActivity extends AppCompatActivity implements StoriesProgressView.StoriesListener {

    int counter = 0;
    long pressTime = 0L;
    long limit = 500L;

    StoriesProgressView storiesProgressView;
    ImageView image, story_photo;
    TextView story_username;

    LinearLayout r_seen;
    TextView seen_number;
    ImageView story_delete;

    List<String> storyIds;
    List<String> images;
    String userId;

    private View.OnTouchListener touchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    pressTime = System.currentTimeMillis();
                    storiesProgressView.pause();
                    return false;

                case MotionEvent.ACTION_UP:
                    long now = System.currentTimeMillis();
                    storiesProgressView.resume();
                    return limit < (now - pressTime);
            }

            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story);

        r_seen = findViewById(R.id.r_seen);
        seen_number = findViewById(R.id.seen_number);
        story_delete = findViewById(R.id.story_delete);

        storiesProgressView = findViewById(R.id.stories);
        image = findViewById(R.id.image);
        story_photo = findViewById(R.id.story_photo);
        story_username = findViewById(R.id.story_username);

        userId = getIntent().getStringExtra("userId");

        if (userId.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
            r_seen.setVisibility(View.VISIBLE);
            story_delete.setVisibility(View.VISIBLE);
        }

        getStories(userId);
        userInfo(userId);

        View reverse = findViewById(R.id.reverse);
        reverse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                storiesProgressView.reverse();
            }
        });
        reverse.setOnTouchListener(touchListener);

        View skip = findViewById(R.id.skip);
        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                storiesProgressView.skip();
            }
        });
        skip.setOnTouchListener(touchListener);

        r_seen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StoryActivity.this, FollowersActivity.class);
                intent.putExtra("id", userId);
                intent.putExtra("storyId", storyIds.get(counter));
                intent.putExtra("title", "views");
                startActivity(intent);
            }
        });

        story_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                android.app.AlertDialog alertDialog = new android.app.AlertDialog.Builder(StoryActivity.this).create();

                alertDialog.setTitle("You want to delete story?");

                alertDialog.setButton(android.app.AlertDialog.BUTTON_NEGATIVE, "cancel",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                dialog.dismiss();

                            }
                        });

                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "delete",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Story")
                                        .child(userId)
                                        .child(storyIds.get(counter));

                                reference.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(StoryActivity.this, "Story deleted.", Toast.LENGTH_SHORT).show();
                                            finish();
                                        }
                                    }
                                });

                            }
                        });
                alertDialog.show();

            }
        });

    }

    @Override
    public void onNext() {
        if ((counter + 1) < images.size())
            Glide.with(getApplicationContext()).load(images.get(++counter)).into(image);

        addView(storyIds.get(counter));
        seenNumber(storyIds.get(counter));
    }

    @Override
    public void onPrev() {
        if ((counter - 1) >= 0)
            Glide.with(getApplicationContext()).load(images.get(--counter)).into(image);

        seenNumber(storyIds.get(counter));

    }

    @Override
    protected void onDestroy() {
        storiesProgressView.destroy();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        storiesProgressView.pause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        storiesProgressView.resume();
        super.onResume();
    }

    @Override
    public void onComplete() {
        finish();
    }

    private void getStories(String userId) {

        images = new ArrayList<>();
        storyIds = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Story").child(userId);

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                images.clear();
                storyIds.clear();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Story story = dataSnapshot.getValue(Story.class);
                    long currentTime = System.currentTimeMillis();
                    if (currentTime > story.getTimeStart() && currentTime < story.getTimeEnd()) {
                        images.add(story.getImageUrl());
                        storyIds.add(story.getStoryId());
                    }
                }

                storiesProgressView.setStoriesCount(images.size());
                storiesProgressView.setStoryDuration(5000L);
                storiesProgressView.setStoriesListener(StoryActivity.this);
                storiesProgressView.startStories(counter);

                Glide.with(getApplicationContext()).load(images.get(counter)).into(image);

                addView(storyIds.get(counter));
                seenNumber(storyIds.get(counter));

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void userInfo(String userId) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(userId);

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);

                Glide.with(getApplicationContext()).load(user.getImgUrl()).into(story_photo);
                story_username.setText(user.getUsername());

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void addView(String storyId) {
        FirebaseDatabase.getInstance().getReference("Story").child(userId).child(storyId)
                .child("views").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(true);
    }

    private void seenNumber(String storyId){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Story")
                .child(userId).child(storyId).child("views");

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                seen_number.setText(""+snapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}