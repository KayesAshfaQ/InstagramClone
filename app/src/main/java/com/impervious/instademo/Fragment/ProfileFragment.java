package com.impervious.instademo.Fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.impervious.instademo.Adapter.MyFotoAdapter;
import com.impervious.instademo.EditProfileActivity;
import com.impervious.instademo.FollowersActivity;
import com.impervious.instademo.Model.Post;
import com.impervious.instademo.Model.User;
import com.impervious.instademo.OptionActivity;
import com.impervious.instademo.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;


public class ProfileFragment extends Fragment {

    ImageView image_profile, options;
    TextView posts, followers, following, fullName, bio, username;
    TextView edit_profile;

    RecyclerView recyclerView;
    List<Post> postList;
    MyFotoAdapter myFotoAdapter;

    RecyclerView recyclerView_saves;
    List<Post> postList_saves;
    MyFotoAdapter myFotoAdapter_saves;
    FirebaseUser firebaseUser;
    String profileId;
    ImageView my_fotos, saved_fotos;
    private List<String> mySaves;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        SharedPreferences prefs = getContext().getSharedPreferences("PREFS", Context.MODE_PRIVATE);
        profileId = prefs.getString("profileId", "none");


        image_profile = view.findViewById(R.id.image_profile);
        options = view.findViewById(R.id.options);
        posts = view.findViewById(R.id.posts);
        followers = view.findViewById(R.id.followers);
        following = view.findViewById(R.id.following);
        fullName = view.findViewById(R.id.fullName);
        bio = view.findViewById(R.id.bio);
        username = view.findViewById(R.id.username);
        edit_profile = view.findViewById(R.id.edit_profile);
        my_fotos = view.findViewById(R.id.my_fotos);
        saved_fotos = view.findViewById(R.id.saved_fotos);

        //for myFotos
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new GridLayoutManager(getContext(), 3);
        recyclerView.setLayoutManager(layoutManager);
        postList = new ArrayList<>();
        myFotoAdapter = new MyFotoAdapter(getContext(), postList);
        myFotos();
        recyclerView.setAdapter(myFotoAdapter);

        //for saved
        recyclerView_saves = view.findViewById(R.id.recycler_view_save);
        recyclerView_saves.setHasFixedSize(true);
        LinearLayoutManager layoutManager_saves = new GridLayoutManager(getContext(), 3);
        recyclerView_saves.setLayoutManager(layoutManager_saves);
        postList_saves = new ArrayList<>();
        myFotoAdapter_saves = new MyFotoAdapter(getContext(), postList_saves);

        recyclerView_saves.setAdapter(myFotoAdapter_saves);
        recyclerView.setVisibility(View.VISIBLE);
        recyclerView_saves.setVisibility(View.GONE);

        //profile details
        userInfo();
        getFollowers();
        countPost();

        //options
        options.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), OptionActivity.class));
            }
        });

        //follow or following or edit profile btn
        if (profileId.equals(firebaseUser.getUid())) {
            edit_profile.setText("Edit Profile");
        } else {
            checkFollow();
            saved_fotos.setVisibility(View.GONE);
        }

        edit_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String btn = edit_profile.getText().toString();
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Follow");
                if (btn.equals("Edit Profile")) {
                    //go to edit profile
                    Intent intent = new Intent(getContext(), EditProfileActivity.class);
                    startActivity(intent);
                } else if (btn.equals("follow")) {
                    reference.child(firebaseUser.getUid()).child("following").child(profileId).setValue(true);
                    reference.child(profileId).child("followers").child(firebaseUser.getUid()).setValue(true);

                    //add follow notification
                    addNotifications();

                } else if (btn.equals("following")) {
                    reference.child(firebaseUser.getUid()).child("following").child(profileId).removeValue();
                    reference.child(profileId).child("followers").child(firebaseUser.getUid()).removeValue();
                }
            }
        });

        //show myFotos item
        mySaves();
        my_fotos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recyclerView.setVisibility(View.VISIBLE);
                recyclerView_saves.setVisibility(View.GONE);
            }
        });

        //show mySaved item
        saved_fotos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recyclerView.setVisibility(View.GONE);
                recyclerView_saves.setVisibility(View.VISIBLE);
            }
        });

        //add listeners to followers to see list of them
        followers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), FollowersActivity.class);
                intent.putExtra("id", profileId);
                intent.putExtra("title", "followers");
                startActivity(intent);
            }
        });

        //add listeners to following to see list of them
        following.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), FollowersActivity.class);
                intent.putExtra("id", profileId);
                intent.putExtra("title", "following");
                startActivity(intent);
            }
        });

        return view;
    }


    private void userInfo() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(profileId);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (getContext() == null) {
                    return;
                }
                User user = snapshot.getValue(User.class);

                Glide.with(getContext()).load(user.getImgUrl()).into(image_profile);
                username.setText(user.getUsername());
                fullName.setText(user.getFullname());
                bio.setText(user.getBio());

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void checkFollow() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("Follow")
                .child(firebaseUser.getUid()).child("following");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(profileId).exists()) {
                    edit_profile.setText("following");
                } else {
                    edit_profile.setText("follow");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getFollowers() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Follow")
                .child(profileId);

        reference.child("followers").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                followers.setText("" + snapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        reference.child("following").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                following.setText("" + snapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void countPost() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                int count = 0;
                for (DataSnapshot snap : snapshot.getChildren()) {
                    Post post = snap.getValue(Post.class);

                    if (post.getPublisher().equals(profileId)) {
                        count++;
                    }

                }
                posts.setText("" + count);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void myFotos() {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    Post post = snap.getValue(Post.class);
                    if (post.getPublisher().equals(profileId)) {
                        postList.add(post);
                    }
                }
                Collections.reverse(postList);
                myFotoAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void mySaves() {
        mySaves = new ArrayList<>();
        FirebaseDatabase.getInstance().getReference("Saves").child(firebaseUser.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot snap : snapshot.getChildren()) {
                            mySaves.add(snap.getKey());
                        }
                        readSaves();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    private void readSaves() {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                postList_saves.clear();

                for (DataSnapshot snap : snapshot.getChildren()) {
                    Post post = snap.getValue(Post.class);

                    for (String savedPostId : mySaves) {
                        if (post.getPostId().equals(savedPostId)) {
                            postList_saves.add(post);
                        }
                    }

                }
                myFotoAdapter_saves.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    //notification
    private void addNotifications() {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Notifications").child(profileId);

        HashMap<String, Object> map = new HashMap<>();

        map.put("userId", firebaseUser.getUid());
        map.put("text", "started following you");
        map.put("postId", "");
        map.put("isPost", false);

        reference.push().setValue(map);

    }

}
