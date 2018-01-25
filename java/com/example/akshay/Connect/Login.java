package com.example.akshay.Connect;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class Login extends AppCompatActivity implements View.OnClickListener {


    Toolbar toolbar;
    Button login, log_signup;
    TextInputLayout log_email;
    TextInputLayout log_password;
    FirebaseAuth mAuth;
    ProgressDialog dialog;
    FirebaseAuth firebaseAuth;
    DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        init();
        log_signup.setOnClickListener(this);
        log_signup.setOnClickListener(this);

    }


    public void init()
    {
        login = findViewById(R.id.log_btn_login);
        login.setOnClickListener(this);
        log_signup = findViewById(R.id.log_btn_signup);
        log_email=findViewById(R.id.log_email);
        log_password=findViewById(R.id.log_password);
        log_signup.setOnClickListener(this);
        userRef= FirebaseDatabase.getInstance().getReference().child("Users");
        toolbar = findViewById(R.id.log_tool);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle("Login");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mAuth=FirebaseAuth.getInstance();
        dialog=new ProgressDialog(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.log_btn_login:
                String log_email_str,log_pass_str;
                if(Validate())
                {
                    dialog.setTitle("Loggin in");
                    dialog.setMessage("Please wait while we are logging you in");
                    dialog.setCancelable(false);
                    dialog.show();
                    log_email_str=log_email.getEditText().getText().toString();
                    log_pass_str=log_password.getEditText().getText().toString();
                    login(log_email_str,log_pass_str);
                }
                break;
            case R.id.log_btn_signup:
                Intent i = new Intent(Login.this, Register.class);
                startActivity(i);
                break;
        }
    }

    public void login(String email,String password)
    {
        mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if(task.isSuccessful())
                {
                    String deviceToken= FirebaseInstanceId.getInstance().getToken();
                    userRef.child(mAuth.getCurrentUser().getUid()).child("token").setValue(deviceToken).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            dialog.dismiss();
                            Intent i=new Intent(Login.this,HomeActivity.class);
                            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(i);
                            finish();
                        }
                    });

                }
                else
                {
                    dialog.hide();
                    Toast.makeText(Login.this,"Error",Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    public boolean Validate()
    {
        log_email.setError(null);
        log_password.setError(null);
        boolean flag=true;
        if(TextUtils.isEmpty(log_email.getEditText().getText()))
        {
            log_email.setError("Enter User Name");
            flag=false;
        }
        if(TextUtils.isEmpty(log_password.getEditText().getText()))
        {
            log_password.setError("Enter User Name");
            flag=false;
        }
    return flag;

    }
}
