package com.yso.charp.adapter;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.util.Base64;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;
import com.yso.charp.Interface.ChatItemClickListener;
import com.yso.charp.Interface.ImageClickListener;
import com.yso.charp.MyApplication;
import com.yso.charp.R;
import com.yso.charp.model.ChatMessage;
import com.yso.charp.utils.Utils;

import java.io.ByteArrayOutputStream;
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

    public void setItems(List<ChatMessage> messagesList)
    {
        this.mMessageList = messagesList;
        notifyDataSetChanged();
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

        setGravityByUser(viewHolder, c);

        String contactName = Utils.getContactName(c.getMessageUser(), mContext);
        String name = contactName.equals("") ? c.getMessageUser() : contactName;
        viewHolder.displayName.setText(name);
        viewHolder.messageText.setText(c.getMessageText());
        viewHolder.messageTime.setText(DateFormat.format("HH:mm", c.getMessageTime()));

        if (c.getBitmap() != null)
        {
        /*if (c.getBase64Image() != null && !c.getBase64Image().equals(""))
        {

            byte[] imageBytes = Base64.decode(c.getBase64Image(), Base64.DEFAULT);
            Bitmap decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);


//            Bitmap ThumbImage = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length), 50, 50);

            float aspectRatio = decodedImage.getWidth() / (float) decodedImage.getHeight();
            int width = 480;
            int height = Math.round(width / aspectRatio);
            decodedImage = Bitmap.createScaledBitmap(decodedImage, width, height, false);


            viewHolder.messageImage.setImageBitmap(decodedImage);

            final Bitmap finalDecodedImage = decodedImage;*/
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

    }

    private void setGravityByUser(MessageViewHolder viewHolder, ChatMessage c)
    {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        if (c.getMessageUser().equals(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber()))
        {
            layoutParams.gravity = Gravity.START;
            viewHolder.group.setLayoutParams(layoutParams);
            viewHolder.group.setBackgroundResource(R.drawable.bg_my_message);
            layoutParams.setMarginEnd(150);
            viewHolder.group.setPadding(0, 0, 35, 0);
        }
        else
        {
            layoutParams.gravity = Gravity.END;
            viewHolder.group.setLayoutParams(layoutParams);
            viewHolder.group.setBackgroundResource(R.drawable.bg_other_message);
            layoutParams.setMarginStart(150);
            viewHolder.group.setPadding(20, 0, 10, 0);
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
