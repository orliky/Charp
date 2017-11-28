package com.yso.charp.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.util.Base64;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.yso.charp.MyApplication;
import com.yso.charp.R;
import com.yso.charp.model.ChatMessage;
import com.yso.charp.utils.Utils;

import java.util.List;

public class ChatMessageAdapter extends RecyclerView.Adapter<ChatMessageAdapter.MessageViewHolder>
{


    private List<ChatMessage> mMessageList;
    private Context mContext;

    public ChatMessageAdapter(Context context, List<ChatMessage> mMessageList)
    {

        this.mContext = context;
        this.mMessageList = mMessageList;
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_list_item, parent, false);

        return new MessageViewHolder(v);

    }

    class MessageViewHolder extends RecyclerView.ViewHolder
    {

        LinearLayout group;
        TextView messageText;
        TextView displayName;
        TextView messageTime;
        ImageView messageImage;
        ProgressBar progressBar;

        MessageViewHolder(View view)
        {
            super(view);

            group = view.findViewById(R.id.message_group);
            messageText = view.findViewById(R.id.message_text);
            displayName = view.findViewById(R.id.message_from_user);
            messageTime = view.findViewById(R.id.message_time);
            messageImage = view.findViewById(R.id.message_image);
            progressBar = view.findViewById(R.id.message_image_progress);

        }
    }

    @SuppressLint ("NewApi")
    @Override
    public void onBindViewHolder(final MessageViewHolder viewHolder, int i)
    {

        ChatMessage c = mMessageList.get(i);

        setGravityByUser(viewHolder, c);

        String contactName = Utils.getContactName(c.getMessageUser(), mContext);
        String name = contactName.equals("") ? c.getMessageUser() : contactName;
        viewHolder.displayName.setText(name);
        viewHolder.messageText.setText(c.getMessageText());
        viewHolder.messageTime.setText(DateFormat.format("HH:mm", c.getMessageTime()));

        if (c.getBase64Image() != null && !c.getBase64Image().equals(""))
        {
            viewHolder.progressBar.setVisibility(View.VISIBLE);
            viewHolder.messageImage.setVisibility(View.VISIBLE);

            byte[] imageBytes = Base64.decode(c.getBase64Image(), Base64.DEFAULT);
            Bitmap decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            viewHolder.messageImage.setImageBitmap(decodedImage);
            viewHolder.progressBar.setVisibility(View.GONE);
            //            Glide.with(MyApplication.getAppContext()).load(c.getBase64Image()).crossFade().fitCenter().into(viewHolder.messageImage);
        }
        else
        {
            viewHolder.progressBar.setVisibility(View.GONE);
            viewHolder.messageImage.setVisibility(View.GONE);
        }

    }

    private void setGravityByUser(MessageViewHolder viewHolder, ChatMessage c)
    {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        if (c.getMessageUser().equals(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber()))
        {
            layoutParams.gravity = Gravity.START;
            viewHolder.group.setLayoutParams(layoutParams);
            viewHolder.group.setBackgroundResource(R.drawable.my_bubble);
            layoutParams.setMarginEnd(150);
        }
        else
        {
            layoutParams.gravity = Gravity.END;
            viewHolder.group.setLayoutParams(layoutParams);
            viewHolder.group.setBackgroundResource(R.drawable.other_bubble);
            layoutParams.setMarginStart(150);
        }
    }

    @Override
    public int getItemCount()
    {
        return mMessageList.size();
    }
}
