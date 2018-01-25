package com.example.akshay.Connect;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsFragment extends Fragment {

    RecyclerView recyclerView;
    FirebaseAuth firebaseAuth;
    DatabaseReference referenceFriends;
    DatabaseReference referenceUsers;
    TextView noFriends;
    View mView;
    TextView getNoFriends;

    public FriendsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_friends, container, false);
        recyclerView = mView.findViewById(R.id.friends_recycler);
        firebaseAuth = FirebaseAuth.getInstance();
        noFriends=mView.findViewById(R.id.friends_null);
        referenceFriends = FirebaseDatabase.getInstance().getReference().child("friend").child(firebaseAuth.getCurrentUser().getUid());
        referenceUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        //recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(mView.getContext()));
        return mView;

    }

    @Override
    public void onStart() {
        super.onStart();
        referenceFriends.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()) {
                    noFriends.setVisibility(View.VISIBLE);
                }
                }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        FirebaseRecyclerAdapter<Friends, FriendsHolder> recyclerAdapter = new FirebaseRecyclerAdapter<Friends, FriendsHolder>
                (Friends.class, R.layout.friends_row, FriendsHolder.class, referenceFriends) {

            @Override
            protected void populateViewHolder(final FriendsHolder viewHolder, Friends model, final int position) {
                String uid = getRef(position).getKey();
                referenceUsers.child(uid).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(final DataSnapshot dataSnapshot) {

                        if (dataSnapshot.hasChildren()) {

                            noFriends.setVisibility(View.INVISIBLE);
                            final String uid = getRef(position).getKey();
                            viewHolder.setDisplayName(dataSnapshot.child("name").getValue().toString());
                            viewHolder.setStatus(dataSnapshot.child("status").getValue().toString());
                            viewHolder.setThumbImage(dataSnapshot.child("thumb_image").getValue().toString());
                            viewHolder.setOnlineStatus(dataSnapshot.child("online").getValue().toString());
                            viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    CharSequence[] options = {"Open Profile", "Send Message"};
                                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                                            .setTitle("Select Option")
                                            .setItems(options, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {

                                                    if (i == 0) {
                                                        Intent i1 = new Intent(getContext(), ProfileActivity.class);
                                                        i1.putExtra("uid", uid);
                                                        startActivity(i1);
                                                    } else if (i == 1) {
                                                        Intent i1 = new Intent(getContext(), ChatActivity.class);
                                                        i1.putExtra("uid", uid);
                                                        i1.putExtra("name",dataSnapshot.child("name").getValue().toString());
                                                        i1.putExtra("uimage",dataSnapshot.child("thumb_image").getValue().toString());
                                                        i1.putExtra("onlinestatus",dataSnapshot.child("online").getValue().toString());
                                                        startActivity(i1);
                                                    }

                                                }
                                            });
                                    builder.show();
                                }
                            });

                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        };
        recyclerView.setAdapter(recyclerAdapter);
    }

    public static class FriendsHolder extends RecyclerView.ViewHolder {
        View mView;

        public FriendsHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setDate(String data) {
            TextView textViewName = mView.findViewById(R.id.chat_row_displayname);
            textViewName.setText(data);
        }

        public void setDisplayName(String data) {
            TextView textViewName = mView.findViewById(R.id.chat_row_displayname);
            textViewName.setText(data);
        }

        public void setStatus(String data) {
            TextView textViewStatus = mView.findViewById(R.id.chat_row_latestmsg);
            textViewStatus.setText(data);
        }

        public void setOnlineStatus(String onlineStatus) {
            ImageView imageOnlineStatus = mView.findViewById(R.id.friends_row_online_status);
            if (onlineStatus.equals("true")) {
                imageOnlineStatus.setImageResource(R.drawable.online);
            } else {
                imageOnlineStatus.setImageResource(R.drawable.offline);
            }
        }

        public void setThumbImage(String url) {
            CircleImageView circleImageView = mView.findViewById(R.id.chat_row_image);
            if (url.equals("ThumbImage")) {
                circleImageView.setImageResource(R.drawable.default_avatar);
            } else {
                Picasso.with(mView.getContext()).load(url).into(circleImageView);
            }
        }


    }
}
