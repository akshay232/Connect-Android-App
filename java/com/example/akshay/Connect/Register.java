package com.example.akshay.Connect;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;

import java.util.HashMap;

public class Register extends AppCompatActivity implements View.OnClickListener {

    android.support.v7.widget.Toolbar toolbar;
    private FirebaseAuth mAuth;
    TextInputLayout uname;
    TextInputLayout email;
    TextInputLayout password;
    Button signup;
    ProgressDialog dialog;
    DatabaseReference databaseReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        init();

    }

    public void init() {
        toolbar = findViewById(R.id.reg_tool);
        uname = findViewById(R.id.uname);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        signup = findViewById(R.id.signup_btn);
        setSupportActionBar(toolbar);
        signup.setOnClickListener(this);
        dialog = new ProgressDialog(this);
        dialog.setTitle("Registering");
        dialog.setMessage("Please wait while we register your account");
        dialog.setCancelable(false);
        getSupportActionBar().setTitle("Create Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mAuth = FirebaseAuth.getInstance();
    }

    public void register(final String uname_str, String email_str, String pass_str, final String imgurl) {
        dialog.show();
        mAuth.createUserWithEmailAndPassword(email_str, pass_str).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    dialog.dismiss();
                    String uid = mAuth.getCurrentUser().getUid();
                    String token= FirebaseInstanceId.getInstance().getToken();
                    databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
                    HashMap<String, String> userMap = new HashMap<>();
                    userMap.put("name", uname_str);
                    userMap.put("status", "Hi there i'm using Connect");
                    userMap.put("image", imgurl);
                    userMap.put("thumb_image", "ThumbImage");
                    userMap.put("token",token);
                    databaseReference.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Intent i = new Intent(Register.this, HomeActivity.class);
                                startActivity(i);
                                finish();
                            } else {
                                dialog.hide();
                                FirebaseException exception = (FirebaseException) task.getException();
                                Toast.makeText(Register.this, exception.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });

                }
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    private void updateUI(FirebaseUser currentUser) {
    }

    public boolean Validate() {

        email.setError(null);
        uname.setError(null);
        password.setError(null);

        boolean flag = true;
        if (TextUtils.isEmpty(email.getEditText().getText())) {
            email.setError("Enter Email ID");
            flag = false;
        }
        if (TextUtils.isEmpty(uname.getEditText().getText())) {
            uname.setError("Enter User Name");
            flag = false;
        }
        if (TextUtils.isEmpty(password.getEditText().getText())) {
            password.setError("Enter Password");
            flag = false;
        }
        if (password.getEditText().getText().toString().length() < 6) {
            password.setError("Password should not be less than 6 characters");
            flag = false;
        }
        return flag;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.signup_btn:
                if (Validate()) {

                    String email_str, uname_str, pass_str, imgurl;
                    email_str = email.getEditText().getText().toString();
                    uname_str = uname.getEditText().getText().toString();
                    pass_str = password.getEditText().getText().toString();
                    imgurl = "default";
                    register(uname_str, email_str, pass_str, imgurl);

                }
        }
    }
}
