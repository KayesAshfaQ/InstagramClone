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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.impervious.instademo.MainActivity;
import com.impervious.instademo.Model.Comment;
import com.impervious.instademo.Model.User;
import com.impervious.instademo.R;
import com.impervious.instademo.ViewHolder.CommentViewHolder;

import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentViewHolder>{

    Context context;
    List<Comment> commentList;
    String postId;

    FirebaseUser firebaseUser;

    public CommentAdapter(Context context, List<Comment> commentList, String postId) {
        this.context = context;
        this.commentList = commentList;
        this.postId = postId;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.comment_item, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        Comment comment = commentList.get(position);

        holder.comment.setText(comment.getComment());
        getUserInfo(comment.getPublisher(), holder.image_profile, holder.username);

        holder.comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, MainActivity.class);
                intent.putExtra("publisherId", comment.getPublisher());
                context.startActivity(intent);
            }
        });

        holder.image_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, MainActivity.class);
                intent.putExtra("publisherId", comment.getPublisher());
                context.startActivity(intent);
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                if (comment.getPublisher().equals(firebaseUser.getUid())){
                    AlertDialog alertDialog = new AlertDialog.Builder(context).create();

                    alertDialog.setTitle("Do you wat to delete?");
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            FirebaseDatabase.getInstance().getReference("Comments").child(postId)
                                    .child(comment.getCommentId()).removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                Toast.makeText(context, "Deleted!", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                            dialog.dismiss();
                        }
                    });

                    alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });

                    alertDialog.show();

                }

                return false;
            }
        });

    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    private void getUserInfo(String publisherId,   final ImageView imageView, final TextView textView){

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(publisherId);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
               if (user != null){
                   Glide.with(context).load(user.getImgUrl()).into(imageView);
                   textView.setText(user.getUsername());
               }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}
