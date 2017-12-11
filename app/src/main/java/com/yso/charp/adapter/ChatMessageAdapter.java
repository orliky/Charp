package com.yso.charp.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.yso.charp.Interface.ImageClickListener;
import com.yso.charp.R;
import com.yso.charp.mannager.FireBaseManager;
import com.yso.charp.model.ChatMessage;
import com.yso.charp.utils.ContactsUtils;

import java.util.List;

public class ChatMessageAdapter extends RecyclerView.Adapter<ChatMessageAdapter.MessageViewHolder>
{


    private List<ChatMessage> mMessageList;
    private Context mContext;
    private ImageClickListener mClickListener;

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
            progressBar = view.findViewById(R.id.message_image_progress);
            messageImage = view.findViewById(R.id.message_image);

        }
    }

    @SuppressLint ("NewApi")
    @Override
    public void onBindViewHolder(final MessageViewHolder viewHolder, int i)
    {
        final ChatMessage c = mMessageList.get(i);

        String contactName = ContactsUtils.getContactName(c.getMessageUser());
        String name = contactName.equals("") ? c.getMessageUser() : contactName;
        viewHolder.displayName.setText(name);
        viewHolder.messageText.setText(c.getMessageText());
        viewHolder.messageTime.setText(DateFormat.format("HH:mm", c.getMessageTime()));

        if (c.getBitmap() != null)
        {
            viewHolder.messageImage.setImageBitmap(c.getBitmap());
            viewHolder.messageImage.setVisibility(View.VISIBLE);
            viewHolder.messageImage.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    mClickListener.onItemClick(c.getBitmap());
                }
            });

        }
        else
        {
            viewHolder.messageImage.setImageBitmap(null);
            viewHolder.messageImage.setOnClickListener(null);
            viewHolder.messageImage.setVisibility(View.GONE);
        }

        setGravityByUser(viewHolder, c, i);
    }

    private void setGravityByUser(MessageViewHolder viewHolder, ChatMessage c, int position)
    {
        boolean isContinuationMessage = false;
        if (position > 0)
        {
            ChatMessage previousMessage = mMessageList.get(position - 1);
            if (c.getMessageUser().equals(previousMessage.getMessageUser()) && c.getMessageTime() - previousMessage.getMessageTime() < 20000)
            {
                isContinuationMessage = true;
            }
        }
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        if (c.getMessageUser().equals(FireBaseManager.getFirebaseUserPhone()))
        {
            layoutParams.gravity = Gravity.START;
            viewHolder.group.setLayoutParams(layoutParams);
            if (!isContinuationMessage)
            {
                viewHolder.group.setBackgroundResource(R.drawable.bg_my_message);
            }
            else
            {
                viewHolder.group.setBackgroundResource(R.drawable.bg_my1_message);
                layoutParams.setMarginStart(20);
            }
            layoutParams.setMarginEnd(150);
            if (!c.getMessageText().equals(""))
            {
                viewHolder.group.setPadding(0, 0, 35, 0);
            }
        }
        else
        {
            layoutParams.gravity = Gravity.END;
            viewHolder.group.setLayoutParams(layoutParams);
            if (!isContinuationMessage)
            {
                viewHolder.group.setBackgroundResource(R.drawable.bg_other_message);
            }
            else
            {
                viewHolder.group.setBackgroundResource(R.drawable.bg_other1_message);
                layoutParams.setMarginEnd(20);
            }
            layoutParams.setMarginStart(150);
            if (!c.getMessageText().equals(""))
            {
                viewHolder.group.setPadding(20, 0, 10, 0);
            }
        }
    }

    @Override
    public int getItemCount()
    {
        return mMessageList.size();
    }

    public void setClickListener(ImageClickListener imageClickListener)
    {
        this.mClickListener = imageClickListener;
    }
}
