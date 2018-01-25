package com.example.akshay.Connect;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class RequestFragment extends Fragment {
    RecyclerView recyclerView;
    DatabaseReference mRootRef, referenceRequest, mRootReference,mNotification;
    View mView;
    TextView noReq;
    FirebaseUser firebaseUser;
    Button accept, cancel;

    public RequestFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_request, container, false);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        recyclerView = mView.findViewById(R.id.request_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(mView.getContext()));
        accept = mView.findViewById(R.id.request_row_accept_req);
        cancel = mView.findViewById(R.id.request_row_cancel_req);
        noReq = mView.findViewById(R.id.request_row_null);
        //recyclerView.setHasFixedSize(true);
        mRootRef = FirebaseDatabase.getInstance().getReference().child("Users");
        mNotification=FirebaseDatabase.getInstance().getReference().child("Notification");
        mRootReference=FirebaseDatabase.getInstance().getReference();
        referenceRequest = FirebaseDatabase.getInstance().getReference().child("friend_request").child(firebaseUser.getUid());
        return mView;
    }

    @Override
    public void onStart() {
        super.onStart();
        referenceRequest.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    noReq.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        final FirebaseRecyclerAdapter<Request, RequestHolder> recyclerAdapter = new FirebaseRecyclerAdapter<Request, RequestHolder>
                (Request.class, R.layout.request_row, RequestHolder.class, referenceRequest) {
            @Override
            protected void populateViewHolder(final RequestHolder viewHolder, final Request model, final int position) {

                final String uid = getRef(position).getKey();
                referenceRequest.child(uid).child("request_type").getRef().addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.exists() && dataSnapshot.getValue().equals("received")) {
                            mRootRef.child(uid).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    noReq.setVisibility(View.INVISIBLE);
                                    viewHolder.setImage(dataSnapshot.child("thumb_image").getValue().toString());
                                    viewHolder.setDisplayName(dataSnapshot.child("name").getValue().toString());


                                    viewHolder.mView.findViewById(R.id.request_row_accept_req).setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            final String currentDate = DateFormat.getInstance().format(new Date());
                                            Map hashMapFriend = new HashMap<>();
                                            hashMapFriend.put("friend/" + firebaseUser.getUid() + "/" + uid + "/date", currentDate);
                                            hashMapFriend.put("friend/" + uid + "/" + firebaseUser.getUid() + "/date", currentDate);
                                            hashMapFriend.put("friend_request/" + firebaseUser.getUid() + "/" + uid + "/request_type", null);
                                            hashMapFriend.put("friend_request/" + uid + "/" + firebaseUser.getUid() + "/request_type", null);
                                            mRootReference.updateChildren(hashMapFriend, new DatabaseReference.CompletionListener() {
                                                @Override
                                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                                    if (databaseError == null) {
                                                        ProfileActivity.mRequestState = "friends";
                                                    } else {
                                                        Toast.makeText(mView.getContext(), "There was some error Try Again", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                        }
                                    });

                                    viewHolder.mView.findViewById(R.id.request_row_cancel_req).setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {

                                            Map hashMapCancel = new HashMap<>();
                                            hashMapCancel.put("friend_request/" + firebaseUser.getUid() + "/" + uid + "/request_type", null);
                                            hashMapCancel.put("friend_request/" + uid + "/" + firebaseUser.getUid() + "/request_type", null);
                                            mRootReference.updateChildren(hashMapCancel, new DatabaseReference.CompletionListener() {
                                                @Override
                                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                                    if (databaseError == null) {
                                                        mNotification.child(uid).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                ProfileActivity.mRequestState = "not_friends";
                                                            }
                                                        });

                                                    } else {

                                                        Toast.makeText(mView.getContext(), "There was some error Try Again", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });

                                        }
                                    });

                                    viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {

                                            Intent intent = new Intent(mView.getContext(), ProfileActivity.class);
                                            intent.putExtra("uid", uid);
                                            startActivity(intent);
                                        }
                                    });

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                }
                            });
                        } else {

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


    public static class RequestHolder extends RecyclerView.ViewHolder {

        View mView;

        public RequestHolder(View itemView) {
            super(itemView);
            mView = itemView;

        }

        public void setImage(String url) {
            CircleImageView circleImageView;
            circleImageView = mView.findViewById(R.id.request_row_image);
            if (url.equals("ThumbImage")) {
                circleImageView.setImageResource(R.drawable.default_avatar);
            } else {
                Picasso.with(mView.getContext()).load(url).into(circleImageView);
            }
        }

        public void setDisplayName(String name) {
            TextView mDisplayName;
            mDisplayName = mView.findViewById(R.id.request_row_displayname);
            mDisplayName.setText(name);
        }
    }

}
