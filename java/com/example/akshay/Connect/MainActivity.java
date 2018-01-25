package com.example.akshay.Connect;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Button btn_login, btn_create;
    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        btn_login.setOnClickListener(this);
        btn_create.setOnClickListener(this);
        firebaseAuth=FirebaseAuth.getInstance();
        firebaseAuth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(firebaseAuth.getCurrentUser()!=null)
                {
                    Intent i=new Intent(MainActivity.this,HomeActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                }
            }
        });
    }

    public void init() {
        btn_login = findViewById(R.id.main_btn_login);
        btn_create = findViewById(R.id.main_btn_create);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }



    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.main_btn_login:
                Intent i = new Intent(MainActivity.this, Login.class);
                startActivity(i);
                break;
            case R.id.main_btn_create:
                Intent i1 = new Intent(MainActivity.this, Register.class);
                startActivity(i1);
                break;

        }
    }
}

