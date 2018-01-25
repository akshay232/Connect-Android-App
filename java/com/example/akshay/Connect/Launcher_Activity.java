package com.example.akshay.Connect;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;

public class Launcher_Activity extends AppCompatActivity {

    Animation animation;
    ImageView launcherLogo;
    Thread t;
    FirebaseAuth firebaseAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher_);
        firebaseAuth=FirebaseAuth.getInstance();
        animation= AnimationUtils.loadAnimation(Launcher_Activity.this,R.anim.launcher_anim);
        launcherLogo=findViewById(R.id.launcher_logo);
        launcherLogo.startAnimation(animation);
        t=new Thread(){
            public void run()
            {
                try {
                    Thread.sleep(2000);
                    firebaseAuth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
                        @Override
                        public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                                if(firebaseAuth.getCurrentUser()!=null)
                                {
                                            Intent i=new Intent(Launcher_Activity.this,HomeActivity.class);
                                            startActivity(i);
                                }
                                else
                                {
                                    Intent i=new Intent(Launcher_Activity.this,MainActivity.class);
                                    startActivity(i);
                                }

                        }
                    });

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                finally {
                    finish();
                }
            }
        };
        t.start();
    }
}
