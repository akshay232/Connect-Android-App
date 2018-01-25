package com.example.akshay.Connect;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;
import jp.wasabeef.picasso.transformations.BlurTransformation;

public class Setting_Activity extends AppCompatActivity implements View.OnClickListener {
    Toolbar toolbar;
    DatabaseReference reference, referenceOnline;
    FirebaseUser mDatabaseUser;
    ImageView backImage;
    TextView displayname, statustv;
    Button change_photo, change_status;
    String uname, status, image, thumb_image;
    BlurTransformation blur;
    CircleImageView circleImageView;
    byte[] thumb_byte;
    ProgressDialog dialog;
    private StorageReference mStorageRef;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_);
        init();
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                uname = dataSnapshot.child("name").getValue().toString();
                status = dataSnapshot.child("status").getValue().toString();
                image = dataSnapshot.child("image").getValue().toString();
                thumb_image = dataSnapshot.child("thumb_image").getValue().toString();
                displayname.setText(uname);
                statustv.setText(status);
                if (thumb_image.equals("thumb_image")) {
                    backImage.setImageResource(R.color.colorPrimary);
                    circleImageView.setImageResource(R.drawable.default_avatar);
                } else
                    Picasso.with(Setting_Activity.this).load(thumb_image).networkPolicy(NetworkPolicy.OFFLINE).transform(blur).into(backImage,new Callback() {
                        @Override
                        public void onSuccess() {
                            Picasso.with(Setting_Activity.this).load(thumb_image).networkPolicy(NetworkPolicy.OFFLINE).into(circleImageView, new Callback() {
                                @Override
                                public void onSuccess() {
                                    dialog.dismiss();
                                }

                                @Override
                                public void onError() {
                                    Toast.makeText(Setting_Activity.this, "Loading failed. Try again", Toast.LENGTH_SHORT).show();
                                }
                            });

                        }

                        @Override
                        public void onError() {
                            Picasso.with(Setting_Activity.this).load(thumb_image).into(circleImageView);
                        }
                    });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void init() {
        toolbar = findViewById(R.id.setting_tool);
        mDatabaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference().child("Users").child(mDatabaseUser.getUid());
        if (reference != null)
            reference.keepSynced(true);
        change_photo = findViewById(R.id.setting_changephoto);
        change_status = findViewById(R.id.setting_changestatus);
        displayname = findViewById(R.id.setting_display_name);
        statustv = findViewById(R.id.setting_status);
        backImage = findViewById(R.id.setting_back_image);
        change_status.setOnClickListener(this);
        dialog=new ProgressDialog(Setting_Activity.this);
        dialog.setTitle("Loading...");
        dialog.setMessage("Wait while we load your profile");
        dialog.show();
        blur = new BlurTransformation(Setting_Activity.this);
        mStorageRef = FirebaseStorage.getInstance().getReference();
        change_photo.setOnClickListener(this);
        circleImageView = findViewById(R.id.setting_circleimage);
        mDatabaseUser = FirebaseAuth.getInstance().getCurrentUser();
        referenceOnline = FirebaseDatabase.getInstance().getReference().child("Users").child(firebaseAuth.getInstance().getCurrentUser().getUid()).child("online");
        referenceOnline.setValue("true");
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.setting_changestatus:
                Intent i = new Intent(Setting_Activity.this, StatusActivity.class);
                i.putExtra("status", status);
                startActivity(i);
                break;
            case R.id.setting_changephoto:
              /*  Intent i1=new Intent();
                i1.setType("image/*");
                i1.setAction(Intent.ACTION_GET_CONTENT);
                startActivity(i1);*/
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1, 1)
                        .setMinCropResultSize(500, 500)
                        .start(this);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            final CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                File thumb_image_file = new File(resultUri.getPath());
                dialog.setTitle("Uploading...");
                dialog.setMessage("Please wait while we upload your photo to the server");
                dialog.setCancelable(false);
                dialog.show();


                Bitmap thumb_image = null;
                try {
                    thumb_image = new Compressor(this)
                            .setMaxWidth(200)
                            .setMaxHeight(200)
                            .setQuality(75)
                            .compressToBitmap(thumb_image_file);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                thumb_image.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
                thumb_byte = byteArrayOutputStream.toByteArray();
                StorageReference path = mStorageRef.child("profile_images").child(mDatabaseUser.getUid() + ".jpg");
                final StorageReference thumb_path = mStorageRef.child("profile_images").child("thumb_images").child(mDatabaseUser.getUid() + ".jpg");
                path.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull final Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {

                            UploadTask uploadTask = thumb_path.putBytes(thumb_byte);
                            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumb_task) {
                                    if (thumb_task.isSuccessful()) {
                                        String url = task.getResult().getDownloadUrl().toString();
                                        final String thumb_url = thumb_task.getResult().getDownloadUrl().toString();
                                        Map hashMap = new HashMap();
                                        hashMap.put("image", url);
                                        hashMap.put("thumb_image", thumb_url);
                                        reference.updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener() {
                                            @Override
                                            public void onComplete(@NonNull Task task) {
                                                if (task.isSuccessful()) {
                                                    Picasso.with(Setting_Activity.this).load(thumb_url).networkPolicy(NetworkPolicy.OFFLINE).into(circleImageView);
                                                    dialog.dismiss();
                                                } else {
                                                    Toast.makeText(Setting_Activity.this, "Error", Toast.LENGTH_LONG).show();
                                                }
                                            }
                                        });

                                    }
                                }
                            });


                        } else {
                            dialog.dismiss();
                        }
                    }
                });

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }


        }
    }
}
