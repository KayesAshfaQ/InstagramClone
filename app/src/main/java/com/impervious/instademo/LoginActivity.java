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
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    EditText email, password;
    TextView txt_signup;
    Button login;

    FirebaseAuth firebaseAuth;
    ProgressDialog pd;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        login = findViewById(R.id.login);
        txt_signup = findViewById(R.id.txt_signup);

        firebaseAuth = FirebaseAuth.getInstance();

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String str_email = email.getText().toString();
                String str_password = password.getText().toString();

                if (str_email.isEmpty() || str_password.isEmpty()){
                    Toast.makeText(LoginActivity.this, "all field must be filled", Toast.LENGTH_SHORT).show();
                }else if (str_password.length()<6){
                    Toast.makeText(LoginActivity.this, "password must be at least 6 character", Toast.LENGTH_SHORT).show();
                }else {
                    pd = new ProgressDialog(LoginActivity.this);
                    pd.setMessage("please wait...");
                    pd.show();
                    loginToAccount(str_email, str_password);
                }


            }
        });

        txt_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
            }
        });

    }

    private void loginToAccount(String str_email, String str_password) {

        firebaseAuth.signInWithEmailAndPassword(str_email, str_password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("User")
                            .child(firebaseAuth.getCurrentUser().getUid());

                    databaseReference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            pd.dismiss();
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }else {
                    pd.dismiss();
                    Toast.makeText(LoginActivity.this, "authentication failed", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}