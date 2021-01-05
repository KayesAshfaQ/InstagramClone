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
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.File;
import java.util.HashMap;

public class AddStoryActivity extends AppCompatActivity {

    private Uri mImageUri;
    String imageUrl;
    StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_story);

        storageReference = FirebaseStorage.getInstance().getReference("storage");
        CropImage.activity().setAspectRatio(9, 16).start(AddStoryActivity.this);

    }

    //get file extension
    private String getFileExtension(Uri uri) {
        String extension;

        //Check uri format to avoid null
        if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            //If scheme is a content
            final MimeTypeMap mime = MimeTypeMap.getSingleton();
            extension = mime.getExtensionFromMimeType(getContentResolver().getType(uri));
        } else {
            //If scheme is a File
            //This will replace white spaces with %20 and also other special characters.
            //This will avoid returning null values on file name with spaces and special characters.
            extension = MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(new File(uri.getPath())).toString());
        }

        return extension;
    }

    //upload Story of FirebaseStorage
    private void uploadStory(){

        if (mImageUri != null){
            ProgressDialog pd = new ProgressDialog(this);
            pd.setMessage("posting...");
            pd.show();

            StorageReference imageRef = storageReference.child(System.currentTimeMillis()+"."+getFileExtension(mImageUri));

            imageRef.putFile(mImageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            imageRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {

                                    if (task.isSuccessful()){

                                        Uri uri = task.getResult();
                                        imageUrl = String.valueOf(uri);

                                        publishStory();
                                        pd.dismiss();
                                        Toast.makeText(AddStoryActivity.this, "story published", Toast.LENGTH_SHORT).show();
                                        finish();

                                    }else {
                                        Toast.makeText(AddStoryActivity.this, "can't get url", Toast.LENGTH_SHORT).show();
                                        pd.dismiss();
                                        finish();
                                    }

                                }
                            });

                        }
                    });

        }

    }

    //add on FirebaseDatabase
    private void publishStory(){

        String myId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Story").child(myId);

        String storyId = reference.push().getKey();
        long timeEnd = System.currentTimeMillis()+86400000; //24 hour

        HashMap<String, Object> hashMap = new HashMap<>();

        hashMap.put("imageUrl", imageUrl);
        hashMap.put("timeStart", ServerValue.TIMESTAMP);
        hashMap.put("timeEnd", timeEnd);
        hashMap.put("userId", myId);
        hashMap.put("storyId", storyId);

        reference.child(storyId).setValue(hashMap);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK){

            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            mImageUri = result.getUri();

            uploadStory();

        }else {
            Toast.makeText(this, "Something gone wrong!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(AddStoryActivity.this, MainActivity.class));
        }

    }
}