package com.impervious.instademo.Adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.impervious.instademo.CommentsActivity;
import com.impervious.instademo.FollowersActivity;
import com.impervious.instademo.Fragment.HomeFragment;
import com.impervious.instademo.Fragment.PostDetailsFragment;
import com.impervious.instademo.Fragment.ProfileFragment;
import com.impervious.instademo.MainActivity;
import com.impervious.instademo.Model.Post;
import com.impervious.instademo.Model.User;
import com.impervious.instademo.PostActivity;
import com.impervious.instademo.R;
import com.impervious.instademo.ViewHolder.PostViewHolder;

import java.util.HashMap;
import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostViewHolder> {

    Context mContext;
    List<Post> mPost;

    FirebaseUser firebaseUser;

    public PostAdapter(Context mContext, List<Post> mPost) {
        this.mContext = mContext;
        this.mPost = mPost;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(mContext).inflate(R.layout.post_item, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if ( mPost.get(position) != null){
           Post post = mPost.get(position);


        Glide.with(mContext).load(post.getPostImage())
                .apply(new RequestOptions().placeholder(R.drawable.placeholder))
                .into(holder.post_image);

        if (post.getDescription().equals("")) {
            holder.description.setVisibility(View.GONE);
        } else {
            holder.description.setVisibility(View.VISIBLE);
            holder.description.setText(post.getDescription());
        }

        publisherInfo(holder.image_profile, holder.username, holder.publisher, post.getPublisher());

        //like
        isLiked(post.getPostId(), holder.like);
        countLike(post.getPostId(), holder.likes);
        holder.like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.like.getTag().equals("like")) {
                    FirebaseDatabase.getInstance().getReference().child("Likes").child(post.getPostId())
                            .child(firebaseUser.getUid()).setValue(true);

                    addNotifications(post.getPublisher(), post.getPostId());
                } else {
                    FirebaseDatabase.getInstance().getReference().child("Likes").child(post.getPostId())
                            .child(firebaseUser.getUid()).removeValue();
                }
            }
        });


        //add listeners to see list of post liked users
        holder.likes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(mContext, FollowersActivity.class);
                intent.putExtra("id", post.getPostId());
                intent.putExtra("title", "likes");
                mContext.startActivity(intent);

            }
        });

        //comment
        getComments(post.getPostId(), holder.comments);
        holder.comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, CommentsActivity.class);
                intent.putExtra("postId", post.getPostId());
                intent.putExtra("publisherId", post.getPublisher());
                mContext.startActivity(intent);

            }
        });

        //comments
        holder.comments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, CommentsActivity.class);
                intent.putExtra("postId", post.getPostId());
                intent.putExtra("publisherId", post.getPublisher());
                mContext.startActivity(intent);

            }
        });

        //save btn
        isSaved(post.getPostId(), holder.save);
        holder.save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.save.getTag().equals("save")) {
                    FirebaseDatabase.getInstance().getReference().child("Saves").child(firebaseUser.getUid())
                            .child(post.getPostId()).setValue(true);
                } else {
                    FirebaseDatabase.getInstance().getReference().child("Saves").child(firebaseUser.getUid())
                            .child(post.getPostId()).removeValue();
                }
            }
        });

        //add listener to profile image or username
        holder.image_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
                editor.putString("profileId", post.getPublisher());
                editor.apply();

                ((FragmentActivity) mContext).getSupportFragmentManager()
                        .beginTransaction().replace(R.id.fragment_container, new ProfileFragment()).commit();

            }
        });
        holder.username.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
                editor.putString("profileId", post.getPublisher());
                editor.apply();

                ((FragmentActivity) mContext).getSupportFragmentManager()
                        .beginTransaction().replace(R.id.fragment_container, new ProfileFragment()).commit();

            }
        });
        holder.publisher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
                editor.putString("profileId", post.getPublisher());
                editor.apply();

                ((FragmentActivity) mContext).getSupportFragmentManager()
                        .beginTransaction().replace(R.id.fragment_container, new ProfileFragment()).commit();

            }
        });

        holder.post_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
                editor.putString("postId", post.getPostId());
                editor.putString("fromMyFOTOS", "0");
                editor.apply();

                ((FragmentActivity) mContext).getSupportFragmentManager()
                        .beginTransaction().replace(R.id.fragment_container, new PostDetailsFragment()).commit();
            }
        });

        holder.more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(mContext, v);

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @SuppressLint("NonConstantResourceId")
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch(item.getItemId()){

                            case R.id.edit:
                                editPost(post.getPostId());
                                return true;

                            case R.id.delete:
                                FirebaseDatabase.getInstance().getReference("Posts").child(post.getPostId()).removeValue()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()){
                                                    Toast.makeText(mContext, "Deleted!", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                return true;

                            case R.id.report:
                                Toast.makeText(mContext, "report clicked!", Toast.LENGTH_SHORT).show();
                                return true;

                            default:
                                return false;
                        }
                    }
                });
                popupMenu.inflate(R.menu.post_menu);
                if (!post.getPublisher().equals(firebaseUser.getUid())){
                    popupMenu.getMenu().findItem(R.id.edit).setVisible(false);
                    popupMenu.getMenu().findItem(R.id.delete).setVisible(false);
                }
                popupMenu.show();
            }
        });

        }
    }

    @Override
    public int getItemCount() {
        return mPost.size();
    }


    //add publisherInfo with every post
    private void publisherInfo(final ImageView image_profile, final TextView username,
                               final TextView publisher, final String userId) {

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userId);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                User user = snapshot.getValue(User.class);

                if (user != null) {
                    Glide.with(mContext).load(user.getImgUrl()).into(image_profile);
                    username.setText(user.getUsername());
                    publisher.setText(user.getUsername());
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    //like
    private void isLiked(final String postId, final ImageView imageView) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("Likes")
                .child(postId);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(firebaseUser.getUid()).exists()) {
                    imageView.setImageResource(R.drawable.ic_liked);
                    imageView.setTag("liked");
                } else {
                    imageView.setImageResource(R.drawable.ic_like);
                    imageView.setTag("like");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void countLike(final String postId, final TextView likes) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("Likes")
                .child(postId);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                likes.setText(snapshot.getChildrenCount() + " likes");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //comment
    private void getComments(final String postId, final TextView comments) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Comments").child(postId);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                comments.setText("view all " + snapshot.getChildrenCount() + " comments");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    //save
    private void isSaved(String postId, ImageView imageView) {

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Saves")
                .child(currentUser.getUid());

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(postId).exists()) {
                    imageView.setImageResource(R.drawable.ic_saved);
                    imageView.setTag("saved");
                } else {
                    imageView.setImageResource(R.drawable.ic_save);
                    imageView.setTag("save");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //notification
    private void addNotifications(String userId, String postId) {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Notifications").child(userId);

        HashMap<String, Object> map = new HashMap<>();

        map.put("userId", firebaseUser.getUid());
        map.put("text", "liked your post");
        map.put("postId", postId);
        map.put("isPost", true);

        reference.push().setValue(map);

    }

    //edit post
    private void editPost(String postId){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
        alertDialog.setTitle("Edit post");

        final EditText editText = new EditText(mContext);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );

        editText.setLayoutParams(layoutParams);
        alertDialog.setView(editText);

        getText(postId, editText);

        alertDialog.setPositiveButton("Edit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                HashMap<String, Object> map = new HashMap<>();
                map.put("description", editText.getText().toString());

                FirebaseDatabase.getInstance().getReference("Posts").child(postId).updateChildren(map);
                dialog.dismiss();
            }
        });

        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = alertDialog.create();
        dialog.show();

    }

    //getText for editPost
    private  void getText(String postId, final EditText editText){

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts").child(postId);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                editText.setText(snapshot.getValue(Post.class).getDescription());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

}
