package com.impervious.instademo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    EditText username, fullname, email, password;
    Button register;
    TextView txt_login;

    ProgressDialog pd;
    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        username = findViewById(R.id.username);
        fullname = findViewById(R.id.fullname);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        register = findViewById(R.id.register);
        txt_login = findViewById(R.id.txt_login);

        firebaseAuth = FirebaseAuth.getInstance();

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String str_username = username.getText().toString();
                String str_fullname = fullname.getText().toString();
                String str_email = email.getText().toString();
                String str_password = password.getText().toString();

                if (str_username.isEmpty() || str_email.isEmpty() || str_fullname.isEmpty() || str_password.isEmpty()){
                    Toast.makeText(RegisterActivity.this, "all field must be filled", Toast.LENGTH_SHORT).show();
                }else if (str_password.length()<6){
                    Toast.makeText(RegisterActivity.this, "password must be at least 6 character", Toast.LENGTH_SHORT).show();
                }else {

                    pd = new ProgressDialog(RegisterActivity.this);
                    pd.setMessage("please wait");
                    pd.show();
                    register_account(str_username, str_fullname, str_email, str_password);
                }

            }
        });

        txt_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                finish();
            }
        });

    }

    private void register_account(String str_username, String str_fullname, String str_email, String str_password) {

        firebaseAuth.createUserWithEmailAndPassword(str_email, str_password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){

                    FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                    String uid = firebaseUser.getUid();

                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("User").child(uid);

                    HashMap<String, Object> map = new HashMap<>();
                    map.put("uid", uid);
                    map.put("username", str_username.toLowerCase());
                    map.put("fullname", str_fullname);
                    map.put("bio", "");
                    map.put("imgUrl", "https://firebasestorage.googleapis.com/v0/b/instademo-f45e2.appspot.com/o/place_holder.png?alt=media&token=16631f89-dfe9-440d-8cfd-39669982f768");

                    databaseReference.setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                pd.dismiss();
                                Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            pd.dismiss();
                            Toast.makeText(RegisterActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

                }else {
                    pd.dismiss();
                    Toast.makeText(RegisterActivity.this, "can't register with this email, please try again", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}