package com.example.akshay.Connect;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageActivity;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class ChatActivity extends AppCompatActivity implements View.OnClickListener {

    Toolbar toolbar;
    ActionBar actionBar;
    TextView displayName, lastSeen;
    CircleImageView chatImage;
    String uname, uid, uimage, onlinestatus;
    DatabaseReference mRootRef, msgRef;
    ImageButton chatAdd, chatSend;
    EditText chatEnterText;
    FirebaseUser mCurrentUser;
    MessageAdapter messageAdapter;
    RecyclerView recyclerView;
    StorageReference chatImageRef;
    List<Messages> messageList = new ArrayList<>();
    LinearLayoutManager linearLayoutManager;
    String pushKey;
    byte[] thumb_byte;
    private int GALLERY_PICK = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        toolbar = findViewById(R.id.chat_tool);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.chat_custom_tool, null);
        actionBar.setCustomView(view);
        uname = getIntent().getStringExtra("name");
        uid = getIntent().getStringExtra("uid");
        uimage = getIntent().getStringExtra("uimage");
        onlinestatus = getIntent().getStringExtra("onlinestatus");
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        msgRef = FirebaseDatabase.getInstance().getReference().child("messages").child(mCurrentUser.getUid()).child(uid).push();
        pushKey = msgRef.getKey();
        lastSeen = findViewById(R.id.chat_tool_lastseen);
        chatImage = findViewById(R.id.chat_image);
        displayName = findViewById(R.id.chat_tool_displayname);
        chatAdd = findViewById(R.id.chat_add_item);
        chatSend = findViewById(R.id.chat_send_item);
        recyclerView = findViewById(R.id.messageRecyclerView);
        linearLayoutManager = new LinearLayoutManager(ChatActivity.this);
        //recyclerView.setHasFixedSize(true);
        chatImageRef = FirebaseStorage.getInstance().getReference().child("ChatImage").child(pushKey + ".jpg");
        chatAdd.setOnClickListener(this);
        chatSend.setOnClickListener(this);
        chatEnterText = findViewById(R.id.chat_text_enter);
        messageAdapter = new MessageAdapter(messageList, uimage);
        mRootRef = FirebaseDatabase.getInstance().getReference();
        displayName.setText(uname);
        if (onlinestatus.equals("true"))
            lastSeen.setText("Online");
        else {
            GetTimeAgo getTimeAgo = new GetTimeAgo();
            Log.d("ONLINE STATUS-****", onlinestatus);
            Long time = Long.parseLong(onlinestatus);
            String lastSeenStatus = getTimeAgo.getTimeAgo(time, getApplicationContext());
            lastSeen.setText(lastSeenStatus);
        }

        if (uimage.equals("ThumbImage")) {
            chatImage.setImageResource(R.drawable.default_avatar);
        } else {
            Picasso.with(ChatActivity.this).load(uimage).into(chatImage);
        }

        mRootRef.child("Chat").child(mCurrentUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChild(uid)) {
                    Map addChatMap = new HashMap();
                    addChatMap.put("seem", false);
                    addChatMap.put("timestamp", ServerValue.TIMESTAMP);

                    Map addUserMap = new HashMap();
                    addUserMap.put("Chat/" + mCurrentUser.getUid() + "/" + uid, addChatMap);
                    addUserMap.put("Chat/" + uid + "/" + mCurrentUser.getUid(), addChatMap);

                    mRootRef.updateChildren(addUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError != null) {
                                Log.d("ChatLog", databaseError.getMessage().toString());
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(messageAdapter);
        loadMessage();
    }


    public void loadMessage() {
        mRootRef.child("messages").child(mCurrentUser.getUid()).child(uid).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                Messages c = dataSnapshot.getValue(Messages.class);
                messageList.add(c);
                recyclerView.scrollToPosition(recyclerView.getAdapter().getItemCount() - 1);
                messageAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                messageAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                messageAdapter.notifyDataSetChanged();

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.chat_send_item:
                sendMessage();
                break;
            case R.id.chat_add_item:
                /*Intent galleryIntent=new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(galleryIntent,GALLERY_PICK);*/
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(this);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                File thumb_image_file = new File(resultUri.getPath());

                Bitmap thumb_image = null;
                try {
                    thumb_image = new Compressor(this)
                            .setQuality(75)
                            .compressToBitmap(thumb_image_file);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                thumb_image.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
                thumb_byte = byteArrayOutputStream.toByteArray();
                UploadTask uploadTask = chatImageRef.putBytes(thumb_byte);
                uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful())
                        {
                            String downloadUrl=task.getResult().getDownloadUrl().toString();
                            String current_userRef = "messages/" + mCurrentUser.getUid() + "/" + uid;
                            String chat_userRef = "messages/" + uid + "/" + mCurrentUser.getUid();
                            Map map=new HashMap();
                            map.put("message", downloadUrl);
                            map.put("seen", false);
                            map.put("type", "image");
                            map.put("timestamp", ServerValue.TIMESTAMP);
                            map.put("from", mCurrentUser.getUid().toString());
                            Map messageUserMap = new HashMap();
                            messageUserMap.put(current_userRef + "/" + pushKey, map);
                            messageUserMap.put(chat_userRef + "/" + pushKey, map);
                            mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                            if(databaseError!=null)
                                            {
                                                Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                                            }
                                }
                            });
                        }

                    }
                });

            }

        }
    }

    public void sendMessage() {
        if (!TextUtils.isEmpty(chatEnterText.getText().toString())) {
            String current_userRef = "messages/" + mCurrentUser.getUid() + "/" + uid;
            String chat_userRef = "messages/" + uid + "/" + mCurrentUser.getUid();
            String message = chatEnterText.getText().toString();

            DatabaseReference databaseReference = mRootRef.child("messages").child(mCurrentUser.getUid()).child(uid).push();
            String push_id = databaseReference.getKey();

            Map messageMap = new HashMap();
            messageMap.put("message", message);
            messageMap.put("seen", false);
            messageMap.put("type", "text");
            messageMap.put("timestamp", ServerValue.TIMESTAMP);
            messageMap.put("from", mCurrentUser.getUid().toString());
            Map messageUserMap = new HashMap();
            messageUserMap.put(current_userRef + "/" + push_id, messageMap);
            messageUserMap.put(chat_userRef + "/" + push_id, messageMap);
            chatEnterText.setText("");
            mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if (databaseError == null) {

                    }


                }
            });


        }
    }
}
