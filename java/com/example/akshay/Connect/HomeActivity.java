package com.example.akshay.Connect;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

public class HomeActivity extends AppCompatActivity {

    private ViewPageAdapter viewPageAdapter;
    DatabaseReference referenceOnline;
    private Toolbar toolbar;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    ProgressDialog dialog;
    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        init();
    }

    private void init() {
        viewPager = findViewById(R.id.home_viewpager);
        viewPageAdapter = new ViewPageAdapter(getSupportFragmentManager());
        viewPager.setAdapter(viewPageAdapter);
        viewPager.setOffscreenPageLimit(2);
        tabLayout = findViewById(R.id.home_tab);
        tabLayout.setupWithViewPager(viewPager);
        TabLayout.Tab tab=tabLayout.getTabAt(1);
        tab.select();
        toolbar = findViewById(R.id.home_tool);
        firebaseAuth=FirebaseAuth.getInstance();
        dialog=new ProgressDialog(this);
        referenceOnline= FirebaseDatabase.getInstance().getReference().child("Users").child(firebaseAuth.getCurrentUser().getUid()).child("online");
        referenceOnline.setValue("true");
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Connect");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        referenceOnline.setValue(ServerValue.TIMESTAMP);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()) {
            case R.id.home_menu_setting:
                Intent i = new Intent(HomeActivity.this, Setting_Activity.class);
                startActivity(i);
                break;
            case R.id.home_menu_all_users:
                Intent i1 = new Intent(HomeActivity.this, AllUsersActivity.class);
                startActivity(i1);
                break;
            case R.id.home_menu_logout:
                dialog.setTitle("Logging you Out");
                dialog.setMessage("Please Wait you are logging out");
                dialog.setCancelable(false);
                dialog.show();
                firebaseAuth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
                    @Override
                    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                        firebaseAuth.signOut();
                        firebaseAuth.removeAuthStateListener(this);
                        dialog.dismiss();
                        referenceOnline.setValue(ServerValue.TIMESTAMP);
                        Intent i = new Intent(HomeActivity.this, MainActivity.class);
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        finish();
                        startActivity(i);


                    }
                });

        }


        return true;
    }
}
