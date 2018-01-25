package com.example.akshay.Connect;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import jp.wasabeef.picasso.transformations.BlurTransformation;

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener {
    String uid;
    TextView displayName, displayStatus;
    Button sendreq, declinereq;
    ImageView imageView;
    DatabaseReference reference;
    DatabaseReference referenceRequest;
    DatabaseReference referenceFriends, mNotification;
    static String mRequestState;
    ProgressDialog dialog;
    FirebaseUser mCurrentUser;
    BlurTransformation blur;
    Toolbar toolbar;
    CircleImageView profileCircleImage;
    DatabaseReference mRootDatabase;
    FirebaseAuth firebaseAuth;
    private DatabaseReference referenceOnline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        init();
        dialog.setTitle("Fetching User Details");
        dialog.setCancelable(false);
        dialog.show();
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("DataSnapShot", dataSnapshot.toString());
                final String dname, status, image, thumb_image;
                dname = dataSnapshot.child("name").getValue().toString();
                status = dataSnapshot.child("status").getValue().toString();
                image = dataSnapshot.child("image").getValue().toString();
                thumb_image = dataSnapshot.child("thumb_image").getValue().toString();
                displayName.setText(dname);
                displayStatus.setText(status);
                if (thumb_image.equals("ThumbImage")) {
                    imageView.setImageResource(R.drawable.default_avatar);
                    dialog.dismiss();
                } else
                    Picasso.with(ProfileActivity.this).load(thumb_image).networkPolicy(NetworkPolicy.OFFLINE).into(imageView, new Callback() {
                        @Override
                        public void onSuccess() {
                        }
                        @Override
                        public void onError() {
                            Picasso.with(ProfileActivity.this).load(thumb_image).into(imageView);
                        }
                    });
                if (referenceRequest != null) {
                    referenceRequest.child(mCurrentUser.getUid()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            if (dataSnapshot.hasChild(uid)) {
                                String req_type = dataSnapshot.child(uid).child("request_type").getValue().toString();
                                if (req_type.equals("received")) {
                                    mRequestState = "received";
                                    sendreq.setText("Accept Request");
                                    declinereq.setVisibility(View.VISIBLE);
                                } else if (req_type.equals("sent")) {
                                    mRequestState = "sent";
                                    sendreq.setText("Cancel Request");
                                    declinereq.setVisibility(View.INVISIBLE);
                                } else if (mRequestState.equals("not_friends")) {
                                    sendreq.setText("Send Friend Request");
                                    declinereq.setVisibility(View.INVISIBLE);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });

                    if (referenceFriends.child(mCurrentUser.getUid()) != null) {
                        referenceFriends.child(mCurrentUser.getUid()).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.hasChild(uid)) {
                                    mRequestState = "friends";
                                    sendreq.setText("Unfriend");
                                    declinereq.setVisibility(View.INVISIBLE);
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                dialog.dismiss();
            }
        });
        dialog.dismiss();
    }

    public void init() {
        toolbar = findViewById(R.id.profile_tool);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        displayName = findViewById(R.id.profile_displayname);
        displayStatus = findViewById(R.id.profile_status);

        uid = getIntent().getStringExtra("uid");
        blur = new BlurTransformation(ProfileActivity.this);
        imageView = findViewById(R.id.profile_image);
        mRequestState = "not_friends";//notfriends
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
        mNotification = FirebaseDatabase.getInstance().getReference().child("Notification");
        mRootDatabase = FirebaseDatabase.getInstance().getReference();
        if (reference != null)
            reference.keepSynced(true);
        referenceRequest = FirebaseDatabase.getInstance().getReference().child("friend_request");
        if (referenceRequest != null)
            referenceRequest.keepSynced(true);
        referenceFriends = FirebaseDatabase.getInstance().getReference().child("friend");
        if (referenceFriends != null)
            referenceFriends.keepSynced(true);

        referenceOnline = FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentUser.getUid()).child("online");
        referenceOnline.setValue("true");


        sendreq = findViewById(R.id.profile_btn_send);
        sendreq.setText("Send Friend Request");
        dialog = new ProgressDialog(this);
        declinereq = findViewById(R.id.profile_btn_decline);
        declinereq.setVisibility(View.INVISIBLE);
        sendreq.setOnClickListener(ProfileActivity.this);
        declinereq.setOnClickListener(ProfileActivity.this);
    }

    //--------------Send Request----------------
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.profile_btn_decline:
                dialog.setTitle("Declining...");
                dialog.setMessage("Please Wait Declining Request");
                dialog.show();
                Map hashMapDecline = new HashMap<>();
                hashMapDecline.put("friend_request/" + mCurrentUser.getUid() + "/" + uid + "/request_type", null);
                hashMapDecline.put("friend_request/" + uid + "/" + mCurrentUser.getUid() + "/request_type", null);
                mRootDatabase.updateChildren(hashMapDecline, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        if (databaseError == null) {
                            declinereq.setVisibility(View.INVISIBLE);
                            sendreq.setText("Send Friend Request");
                            mRequestState = "not_friends";
                            dialog.dismiss();
                        } else {
                            dialog.hide();
                            Toast.makeText(ProfileActivity.this, "There was some error Try Again", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            case R.id.profile_btn_send:
                if (mRequestState.equals("not_friends")) {
                    dialog.setTitle("Sending...");
                    dialog.setMessage("Sending friend request! Please wait");
                    dialog.show();

                    Map hashMapSending = new HashMap<>();
                    hashMapSending.put("friend_request/" + mCurrentUser.getUid() + "/" + uid + "/request_type", "sent");
                    hashMapSending.put("friend_request/" + uid + "/" + mCurrentUser.getUid() + "/request_type", "received");
                    mRootDatabase.updateChildren(hashMapSending, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError == null) {
                                HashMap<String, String> notificationMap = new HashMap<>();
                                notificationMap.put("from", mCurrentUser.getUid());
                                notificationMap.put("type", "request");
                                mNotification.child(uid).push().setValue(notificationMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        sendreq.setText("Cancel Request");
                                        mRequestState = "sent";
                                        dialog.dismiss();
                                    }
                                });

                            } else {
                                dialog.hide();
                                declinereq.setVisibility(View.INVISIBLE);
                                Toast.makeText(ProfileActivity.this, "There was some error Try Again", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
                //----------------Friend Received Request---------------
                if (mRequestState.equals("received")) {
                    dialog.setTitle("Accepting...");
                    dialog.setMessage("Accepting friend request! Please wait");
                    dialog.show();

                    final String currentDate = DateFormat.getInstance().format(new Date());
                    Map hashMapFriend = new HashMap<>();
                    hashMapFriend.put("friend/" + mCurrentUser.getUid() + "/" + uid + "/date", currentDate);
                    hashMapFriend.put("friend/" + uid + "/" + mCurrentUser.getUid() + "/date", currentDate);
                    hashMapFriend.put("friend_request/" + mCurrentUser.getUid() + "/" + uid + "/request_type", null);
                    hashMapFriend.put("friend_request/" + uid + "/" + mCurrentUser.getUid() + "/request_type", null);
                    mRootDatabase.updateChildren(hashMapFriend, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError == null) {
                                sendreq.setText("Unfriend");
                                mRequestState = "friends";
                                declinereq.setVisibility(View.INVISIBLE);
                                dialog.dismiss();
                            } else {
                                dialog.hide();
                                Toast.makeText(ProfileActivity.this, "There was some error Try Again", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }

                //----------------Friend Cancel Request---------------
                if (mRequestState.equals("sent")) {
                    dialog.setTitle("Canceling...");
                    dialog.setMessage("Canceling friend request! Please wait");
                    dialog.show();
                    Map hashMapCancel = new HashMap<>();
                    hashMapCancel.put("friend_request/" + mCurrentUser.getUid() + "/" + uid + "/request_type", null);
                    hashMapCancel.put("friend_request/" + uid + "/" + mCurrentUser.getUid() + "/request_type", null);
                    mRootDatabase.updateChildren(hashMapCancel, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError == null) {
                                mNotification.child(uid).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        sendreq.setText("Send Friend Request");
                                        mRequestState = "not_friends";
                                        dialog.dismiss();
                                    }
                                });

                            } else {
                                dialog.hide();
                                Toast.makeText(ProfileActivity.this, "There was some error Try Again", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }

                //----------------Remove Friend---------------
                if (mRequestState.equals("friends")) {
                    dialog.setTitle("Removing...");
                    dialog.setMessage("Removing friend! Please wait");
                    dialog.show();

                    Map hashMapRemoveFriend = new HashMap<>();
                    hashMapRemoveFriend.put("friend/" + mCurrentUser.getUid() + "/" + uid + "/date", null);
                    hashMapRemoveFriend.put("friend/" + uid + "/" + mCurrentUser.getUid() + "/date", null);
                    mRootDatabase.updateChildren(hashMapRemoveFriend, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError == null) {
                                sendreq.setText("Send Friend Request");
                                mRequestState = "not_friends";
                                dialog.dismiss();
                            } else {
                                dialog.hide();
                                Toast.makeText(ProfileActivity.this, "There was some error Try Again", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }

        }
    }
}
