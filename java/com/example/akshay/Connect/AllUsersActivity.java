package com.example.akshay.Connect;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Filter;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;


public class AllUsersActivity extends AppCompatActivity {

    Toolbar users_tool;
    RecyclerView mrecyclerView;
    ProgressDialog dialog;
    DatabaseReference reference, referenceOnline;
    Query searchQuery;
    FirebaseUser firebaseUser;
    ArrayList<Users> mFilteredList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_users);
        users_tool = findViewById(R.id.users_tool);
        setSupportActionBar(users_tool);
        getSupportActionBar().setTitle("Users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        init();
    }

    public void init() {
        mrecyclerView = findViewById(R.id.users_recycler);
        mFilteredList = new ArrayList<>();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference().child("Users");
        referenceOnline = FirebaseDatabase.getInstance().getReference().child("Users").child(firebaseUser.getUid()).child("online");
        referenceOnline.setValue("true");
        if (reference != null)
            reference.keepSynced(true);
        mrecyclerView.setHasFixedSize(true);
        dialog = new ProgressDialog(this);
        dialog.setIndeterminateDrawable(getDrawable(R.drawable.custom_progress));
        mrecyclerView.setLayoutManager(new LinearLayoutManager(AllUsersActivity.this));
    }

    @Override
    protected void onStart() {
        super.onStart();
        dialog.setTitle("Fetching...");
        dialog.setMessage("Fetching user list");
        dialog.setCancelable(false);
        dialog.show();
        FirebaseRecyclerAdapter<Users, UserViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Users, UserViewHolder>(
                Users.class,
                R.layout.users_row,
                UserViewHolder.class,
                reference) {
            @Override
            protected void populateViewHolder(UserViewHolder viewHolder, Users model, final int position) {

                if (!getRef(position).getKey().equals(firebaseUser.getUid())) {
                    viewHolder.setName(model.getName());
                    viewHolder.setStatus(model.getStatus());
                    viewHolder.setImage(model.getThumb_image());
                    final String uid = getRef(position).getKey();
                    viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View view) {
                                                                Intent i = new Intent(AllUsersActivity.this, ProfileActivity.class);
                                                                i.putExtra("uid", uid);
                                                                startActivity(i);
                                                            }
                                                        }
                    );

                } else {
                    viewHolder.mView.setVisibility(View.GONE);
                    viewHolder.mView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
                }

            }

            @Override
            public void onDataChanged() {
                super.onDataChanged();
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
            }
        };


        mrecyclerView.setAdapter(firebaseRecyclerAdapter);
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public UserViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setName(String name) {
            TextView dname = (TextView) mView.findViewById(R.id.users_row_name);
            dname.setText(name);
        }

        public void setStatus(String status) {
            TextView ustatus = (TextView) mView.findViewById(R.id.users_row_status);
            ustatus.setText(status);
        }

        public void setImage(String imageUrl) {
            CircleImageView circleImageView = (CircleImageView) mView.findViewById(R.id.users_row_image);
            if (imageUrl.equals("ThumbImage")) {
                circleImageView.setImageResource(R.drawable.default_avatar);
            } else
                Picasso.with(mView.getContext()).load(imageUrl).networkPolicy(NetworkPolicy.OFFLINE).into(circleImageView);
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.all_users, menu);

        MenuItem search_item = menu.findItem(R.id.user_search);
        SearchView searchView = (SearchView) search_item.getActionView();
        searchView.setQueryHint("Search User");
        searchView.setFocusable(false);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                                              @Override
                                              public boolean onQueryTextSubmit(String query) {
                                                  return false;
                                              }

                                              @Override
                                              public boolean onQueryTextChange(String newText) {

                                                  if (newText.equals("")) {
                                                      searchQuery = reference;
                                                  } else {
                                                      searchQuery = reference.orderByChild("name").startAt(newText).endAt(newText + "\uf8ff");
                                                  }
                                                  FirebaseRecyclerAdapter<Users, UserViewHolder> firebaseSearchRecyclerAdapter = new FirebaseRecyclerAdapter<Users, UserViewHolder>(
                                                          Users.class,
                                                          R.layout.users_row,
                                                          UserViewHolder.class,
                                                          searchQuery) {
                                                      @Override
                                                      protected void populateViewHolder(UserViewHolder viewHolder, Users model, final int position) {

                                                          if (!getRef(position).getKey().equals(firebaseUser.getUid())) {
                                                              viewHolder.setName(model.getName());
                                                              viewHolder.setStatus(model.getStatus());
                                                              viewHolder.setImage(model.getThumb_image());
                                                              final String uid = getRef(position).getKey();
                                                              viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                                                                                                      @Override
                                                                                                      public void onClick(View view) {
                                                                                                          Intent i = new Intent(AllUsersActivity.this, ProfileActivity.class);
                                                                                                          i.putExtra("uid", uid);
                                                                                                          startActivity(i);
                                                                                                      }
                                                                                                  }
                                                              );

                                                          } else {
                                                              viewHolder.mView.setVisibility(View.GONE);
                                                              viewHolder.mView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
                                                          }

                                                      }
                                                  };

                                                  mrecyclerView.setAdapter(firebaseSearchRecyclerAdapter);

                                                  return true;
                                              }
                                          }
        );
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        return super.onOptionsItemSelected(item);
    }

}
