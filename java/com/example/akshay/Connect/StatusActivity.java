package com.example.akshay.Connect;

import android.app.ActionBar;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class StatusActivity extends AppCompatActivity {

    FirebaseUser mUser;
    DatabaseReference mReference,referenceOnline;
    TextInputLayout status_change;
    Button change_status;
    Intent i;
    ProgressDialog dialog;
    String status;
    Toolbar toolbar;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        init();
        change_status.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (status.equals(status_change.getEditText().getText().toString())) {
                    Toast.makeText(StatusActivity.this,"Please Enter new status",Toast.LENGTH_SHORT).show();
                    dialog.hide();
                }
                else
                {
                    String status = status_change.getEditText().getText().toString();
                    dialog.setTitle("Changing Status");
                    dialog.setMessage("Please wait while we change Status");
                    dialog.setCancelable(false);
                    dialog.show();
                    mReference.setValue(status.toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful())
                            {
                                dialog.dismiss();
                                Toast.makeText(StatusActivity.this,"Changed Successfully",Toast.LENGTH_SHORT).show();
                                Intent i=new Intent(StatusActivity.this,Setting_Activity.class);
                                startActivity(i);
                                finish();
                            }
                        }
                    });


                }
            }
        });
    }

    public void init() {
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        mReference = FirebaseDatabase.getInstance().getReference().child("Users").child(mUser.getUid()).child("status");
        i = getIntent();
        toolbar=findViewById(R.id.status_tool);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        dialog=new ProgressDialog(this);
        status_change = findViewById(R.id.status_change_text);
        change_status = findViewById(R.id.setting_changestatus);
        referenceOnline= FirebaseDatabase.getInstance().getReference().child("Users").child(firebaseAuth.getInstance().getCurrentUser().getUid()).child("online");
        referenceOnline.setValue("true");
        status_change.getEditText().setText(i.getStringExtra("status"));
        status = i.getStringExtra("status");
    }
}
