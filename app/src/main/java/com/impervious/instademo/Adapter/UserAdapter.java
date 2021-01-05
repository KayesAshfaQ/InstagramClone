package com.impervious.instademo.Adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.impervious.instademo.Fragment.ProfileFragment;
import com.impervious.instademo.MainActivity;
import com.impervious.instademo.Model.User;
import com.impervious.instademo.R;
import com.impervious.instademo.ViewHolder.UserViewHolder;

import java.util.HashMap;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserViewHolder> {

    List<User> userList;
    Context context;
    boolean isFrag;
    FirebaseUser firebaseUser;


    public UserAdapter(List<User> userList, Context context, boolean isFrag) {
        this.userList = userList;
        this.context = context;
        this.isFrag = isFrag;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.user_item, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        final User user = userList.get(position);

        holder.btn_follow.setVisibility(View.VISIBLE);

        holder.username.setText(user.getUsername());
        holder.fullname.setText(user.getFullname());
        Glide.with(context).load(user.getImgUrl()).into(holder.image_profile);

        //check already following or not
        isFollowing(user.getUid(), holder.btn_follow);

        //user can't follow himself
        if (user.getUid().equals(firebaseUser.getUid())) {
            holder.btn_follow.setVisibility(View.GONE);
        }

        holder.image_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isFrag) {
                    //sending current user to profile fragment throw shared preference
                    SharedPreferences.Editor editor = context.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
                    editor.putString("profileId", user.getUid());
                    editor.apply();

                    //start profile fragment
                    ((FragmentActivity) context).getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, new ProfileFragment()).commit();
                }else {
                    Intent intent = new Intent(context, MainActivity.class);
                    intent.putExtra("publisherId", user.getUid());
                    context.startActivity(intent);
                }
            }
        });

        holder.btn_follow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Follow");

                if (holder.btn_follow.getText().equals("follow")) {
                    //set who am I following
                    reference.child(firebaseUser.getUid()).child("following").child(user.getUid()).setValue(true);

                    //set who are user followers
                    reference.child(user.getUid()).child("followers").child(firebaseUser.getUid()).setValue(true);

                    //add follow notification
                    addNotifications(user.getUid());
                }else {
                    //remove who am I not following
                    reference.child(firebaseUser.getUid()).child("following").child(user.getUid()).removeValue();

                    //remove who are not user followers
                    reference.child(user.getUid()).child("followers").child(firebaseUser.getUid()).removeValue();
                }

            }
        });

    }

    @Override
    public int getItemCount() {
        return userList.size();
    }


    private void isFollowing(final String uid, final Button button) {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Follow")
                .child(firebaseUser.getUid()).child("following");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.child(uid).exists()) {
                    button.setText("following");
                } else {
                    button.setText("follow");
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    //notification
    private void addNotifications(String userId) {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Notifications").child(userId);

        HashMap<String, Object> map = new HashMap<>();

        map.put("userId", firebaseUser.getUid());
        map.put("text", "started following you");
        map.put("postId", "");
        map.put("isPost", false);

        reference.push().setValue(map);

    }

}
