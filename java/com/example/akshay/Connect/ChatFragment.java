package com.example.akshay.Connect;


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
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
public class ChatFragment extends Fragment {
View mView;
DatabaseReference mUserRef,referenceChat,referenceMessage;
RecyclerView chatRecycler;
FirebaseUser mCurrentUser;
TextView chatNull;
static String msgType;
    public ChatFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragments
        mView=inflater.inflate(R.layout.fragment_chat, container, false);
        mCurrentUser= FirebaseAuth.getInstance().getCurrentUser();
        chatNull=mView.findViewById(R.id.chat_null);
        chatRecycler=mView.findViewById(R.id.chat_fragment_recycler);
        chatRecycler.setLayoutManager(new LinearLayoutManager(mView.getContext()));
        mUserRef= FirebaseDatabase.getInstance().getReference().child("Users");
        referenceMessage=FirebaseDatabase.getInstance().getReference().child("messages").child(mCurrentUser.getUid());
        referenceChat=FirebaseDatabase.getInstance().getReference().child("Chat").child(mCurrentUser.getUid());
        return mView;
    }

    @Override
    public void onStart() {
        super.onStart();

        referenceChat.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(!dataSnapshot.hasChildren())
                {
                    chatNull.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        FirebaseRecyclerAdapter<Chat, ChatHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Chat, ChatHolder>
                (Chat.class, R.layout.chat_row, ChatHolder.class, referenceChat) {
            @Override
            protected void populateViewHolder(final ChatHolder viewHolder, Chat model, int position) {
            final String uid=getRef(position).getKey();
            mUserRef.child(uid).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(final DataSnapshot dataSnapshot) {
                        chatNull.setVisibility(View.INVISIBLE);
                        viewHolder.setDisplayName(dataSnapshot.child("name").getValue().toString());
                        viewHolder.setCircleImage(dataSnapshot.child("thumb_image").getValue().toString());
                        viewHolder.setOnlineStatus(String.valueOf(dataSnapshot.child("online").getValue()));

                        referenceMessage.child(uid).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                for(DataSnapshot child:dataSnapshot.getChildren())
                                {
                                    if(child.child("type").getValue().toString().equals("image"))
                                    {
                                        viewHolder.setLatestMsg("Photo");
                                    }
                                    else
                                    viewHolder.setLatestMsg(child.child("message").getValue().toString());
                                }

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent i1 = new Intent(getContext(), ChatActivity.class);
                                i1.putExtra("uid", uid);
                                i1.putExtra("name",dataSnapshot.child("name").getValue().toString());
                                i1.putExtra("uimage",dataSnapshot.child("thumb_image").getValue().toString());
                                i1.putExtra("onlinestatus",dataSnapshot.child("online").getValue().toString());
                                startActivity(i1);
                            }
                        });
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });



            }
        };
        chatRecycler.setAdapter(firebaseRecyclerAdapter);
    }
        public static class ChatHolder extends RecyclerView.ViewHolder {

        View mView;
        TextView displayName,latestMsg;
        CircleImageView circleImageView;
        ImageView onlineStatus;
        public ChatHolder(View itemView) {
                super(itemView);
                mView=itemView;
            }

            public void setDisplayName(String name)
            {
                    displayName=mView.findViewById(R.id.chat_row_displayname);
                    displayName.setText(name);
            }

            public void setLatestMsg(String msg)
            {
                latestMsg=mView.findViewById(R.id.chat_row_latestmsg);
                latestMsg.setText(msg);
            }

            public void setCircleImage(String url)
            {
                circleImageView=mView.findViewById(R.id.chat_row_image);
                if(url.equals("ThumbImage"))
                {
                    circleImageView.setImageResource(R.drawable.default_avatar);
                }
                else
                {
                    Picasso.with(mView.getContext()).load(url).into(circleImageView);
                }

            }

            public void setOnlineStatus(String status)
            {
                onlineStatus=mView.findViewById(R.id.chat_row_online_status);
                if(status.equals("true"))
                onlineStatus.setImageResource(R.drawable.online);
                else
                onlineStatus.setImageResource(R.drawable.offline);
            }


    }
}
