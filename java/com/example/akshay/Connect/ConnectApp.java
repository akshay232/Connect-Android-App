package com.example.akshay.Connect;

import android.app.Application;
import android.support.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

/**
 * Created by Akshay on 12/12/2017.
 */

public class ConnectApp extends Application {

    DatabaseReference mRootRef;
    FirebaseAuth firebaseauth;
    FirebaseUser firebaseUser;

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        firebaseauth=FirebaseAuth.getInstance();
        firebaseauth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getInstance().getCurrentUser() != null) {
                    mRootRef = FirebaseDatabase.getInstance().getReference().child("Users").child(firebaseAuth.getInstance().getCurrentUser().getUid().toString()).child("online");
                    mRootRef.onDisconnect().setValue(ServerValue.TIMESTAMP);
                }
            }
        });

        Picasso.Builder builder = new Picasso.Builder(this);
        builder.downloader((new OkHttpDownloader(this, Integer.MAX_VALUE)));
        Picasso picasso = builder.build();
        picasso.setIndicatorsEnabled(true);
        picasso.setLoggingEnabled(true);
        Picasso.setSingletonInstance(picasso);
    }
}
