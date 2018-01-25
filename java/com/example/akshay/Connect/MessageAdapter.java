package com.example.akshay.Connect;

/**
 * Created by Akshay on 12/16/2017.
 */

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    FirebaseUser firebaseUser;
    String thumb_image;
    View mView;
    List<Messages> messageList;

    public MessageAdapter(List<Messages> messagesList, String thumb_image) {
        this.thumb_image = thumb_image;
        this.messageList = messagesList;
    }

    @Override
    public MessageAdapter.MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        mView = LayoutInflater.from(parent.getContext()).inflate(R.layout.messages_custom_list, parent, false);
        return new MessageViewHolder(mView);

    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;
        CircleImageView messagePic;
        ImageView chatImageMsg;

        public MessageViewHolder(View itemView) {
            super(itemView);
            mView=itemView;
            messageText = mView.findViewById(R.id.message_text);
            messagePic = mView.findViewById(R.id.message_profile_image);
            chatImageMsg=mView.findViewById(R.id.chat_custom_image);

        }
    }

    @Override
    public void onBindViewHolder(final MessageAdapter.MessageViewHolder holder, int position) {

        String from;
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) holder.messageText.getLayoutParams();
        final Messages c = messageList.get(position);
        from = c.getFrom();

       if(c.getType().equals("text")) {
           if (from.equals(firebaseUser.getUid())) {
               layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
               holder.messageText.setBackgroundResource(R.drawable.message_text_sender);
               holder.messageText.setTextColor(Color.BLACK);
               holder.chatImageMsg.setVisibility(View.GONE);
               holder.messagePic.setVisibility(View.GONE);
               holder.messageText.setLayoutParams(layoutParams);
               holder.messageText.setText(c.getMessage());


           } else {
               layoutParams.removeRule(RelativeLayout.ALIGN_PARENT_RIGHT);
               holder.messageText.setLayoutParams(layoutParams);
               holder.messagePic.setVisibility(View.VISIBLE);
               holder.chatImageMsg.setVisibility(View.GONE);
               Picasso.with(mView.getContext()).load(thumb_image).networkPolicy(NetworkPolicy.OFFLINE).into(holder.messagePic);
               holder.messageText.setBackgroundResource(R.drawable.message_text);
               holder.messageText.setTextColor(Color.WHITE);
               holder.messageText.setText(c.getMessage());
           }
       }
       else
       {
           if (from.equals(firebaseUser.getUid())) {
               layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
               holder.messagePic.setVisibility(View.GONE);
               holder.messageText.setVisibility(View.GONE);
               holder.chatImageMsg.setLayoutParams(layoutParams);
               Picasso.with(mView.getContext()).load(c.getMessage()).networkPolicy(NetworkPolicy.OFFLINE).into(holder.chatImageMsg, new Callback() {
                   @Override
                   public void onSuccess() {

                   }

                   @Override
                   public void onError() {
                       Picasso.with(mView.getContext()).load(c.getMessage()).into(holder.chatImageMsg);
                   }
               });

           } else {
               layoutParams.removeRule(RelativeLayout.ALIGN_PARENT_RIGHT);
               holder.chatImageMsg.setLayoutParams(layoutParams);
               holder.messagePic.setVisibility(View.VISIBLE);
               holder.messageText.setVisibility(View.GONE);
               Picasso.with(mView.getContext()).load(thumb_image).networkPolicy(NetworkPolicy.OFFLINE).into(holder.messagePic);
               Picasso.with(mView.getContext()).load(c.getMessage()).networkPolicy(NetworkPolicy.OFFLINE).into(holder.chatImageMsg);

           }
       }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }
}


