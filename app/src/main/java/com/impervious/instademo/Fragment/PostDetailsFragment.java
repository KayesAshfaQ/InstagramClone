package com.impervious.instademo.Fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.impervious.instademo.Adapter.PostAdapter;
import com.impervious.instademo.CommentsActivity;
import com.impervious.instademo.Model.Post;
import com.impervious.instademo.R;

import java.util.ArrayList;
import java.util.List;

public class PostDetailsFragment extends Fragment {

    String postId;
    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private List<Post> posts;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_post_details, container, false);

        SharedPreferences preferences = getContext().getSharedPreferences("PREFS", Context.MODE_PRIVATE);
        postId = preferences.getString("postId", "none");

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        posts = new ArrayList<>();
        postAdapter = new PostAdapter(getContext(), posts);
        recyclerView.setAdapter(postAdapter);

        readPost();

        return view;
    }

    private void readPost() {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts").child(postId);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                posts.clear();
                Post post = snapshot.getValue(Post.class);
                posts.add(post);

                postAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
    
}