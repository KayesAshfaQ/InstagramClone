package com.impervious.instademo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.impervious.instademo.Model.User;

import java.util.HashMap;

public class EditProfileActivity extends AppCompatActivity {

    private ImageView close, image_profile;
    private TextView save, photo_change;
    private EditText fullName, username, bio;
    private Uri imageUri;

    final int GET_IMAGE_REQ = 1;
    FirebaseUser firebaseUser;
    StorageReference storageReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editprofile);

        close = findViewById(R.id.close);
        image_profile = findViewById(R.id.image_profile);
        save = findViewById(R.id.save);
        photo_change = findViewById(R.id.photo_change);
        fullName = findViewById(R.id.fullName);
        username = findViewById(R.id.username);
        bio = findViewById(R.id.bio);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        storageReference = FirebaseStorage.getInstance().getReference("profile_images");

        //load data to edit
        showPreData();

        //adding listeners
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        photo_change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d("Tag1234", "onClick: ");

                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, GET_IMAGE_REQ);

            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //update on FireBaseDatabase
                updateProfile(
                        fullName.getText().toString(),
                        username.getText().toString(),
                        bio.getText().toString(), ""
                );

            }
        });

    }

    //load data form firebaseDatase
    private void showPreData() {

        FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        User user = snapshot.getValue(User.class);

                        fullName.setText(user.getFullname());
                        username.setText(user.getUsername());
                        bio.setText(user.getBio());
                        Glide.with(getApplicationContext()).load(user.getImgUrl()).into(image_profile);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    //update data to firebaseDatabase
    private void updateProfile(String fullname, String username, String bio, String imgUrl) {

        HashMap<String, Object> map = new HashMap<>();

        if (!fullname.equals("")) {
            map.put("fullname", fullname);
        }

        if (!username.equals("")) {
            map.put("username", username);
        }

        if (!bio.equals("")) {
            map.put("bio", bio);
        }

        if (!imgUrl.equals("")) {
            map.put("imgUrl", imgUrl);
        }

        FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid()).updateChildren(map)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(EditProfileActivity.this, "updated", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(EditProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    //get file extension
    private String getFileExtension(Uri uri) {
        ContentResolver resolver = getContentResolver();
        MimeTypeMap map = MimeTypeMap.getSingleton();
        String extension = map.getExtensionFromMimeType(resolver.getType(uri));
        return extension;
    }

    //get selected img uri & upload to FirebaseStorage
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GET_IMAGE_REQ) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    
                    ProgressDialog progressDialog = new ProgressDialog(this);
                    progressDialog.setMessage("Image Uploading....!");
                    progressDialog.show();
                    imageUri = data.getData();
                    image_profile.setImageURI(imageUri);

                    StorageReference imgReference = storageReference.child(System.currentTimeMillis() + "." + getFileExtension(imageUri));

                    imgReference.putFile(imageUri)
                            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                    imgReference.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Uri> task) {

                                            if (task.isSuccessful()) {

                                                Uri uri = task.getResult();
                                                String img_result = String.valueOf(uri);

                                                updateProfile("", "", "", img_result);
                                            } else {
                                                Toast.makeText(EditProfileActivity.this, "fail imgUrl", Toast.LENGTH_SHORT).show();
                                            }

                                        }
                                    });

                                }
                            });

                    progressDialog.dismiss();
                }
            }
        }

    }


}