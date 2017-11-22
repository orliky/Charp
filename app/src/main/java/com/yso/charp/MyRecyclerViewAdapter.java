package com.yso.charp;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.HashMap;

/**
 * Created by Admin on 21-Nov-17.
 */

public class MyRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
{
    public static final int TYPE_USERS = 0;
    public static final int TYPE_CHATS = 1;

    private HashMap<String, User> mData = new HashMap<>();
    private String[] mKeys;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private int mType;

    public MyRecyclerViewAdapter(Context context, HashMap<String, User> data, int type)
    {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        mKeys = mData.keySet().toArray(new String[data.size()]);
        mType = type;
    }

    public void setItems(HashMap<String, User> data)
    {
        mData = data;
        mKeys = mData.keySet().toArray(new String[data.size()]);
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        switch (mType) {
            case TYPE_CHATS :

                View chatView = mInflater.inflate(R.layout.chat, parent, false);
                ChatViewHolder chatViewHolder = new ChatViewHolder(chatView);
                return chatViewHolder;

            case TYPE_USERS:

                View userView = mInflater.inflate(R.layout.user, parent, false);
                UserViewHolder userViewHolder = new UserViewHolder(userView);
                return userViewHolder;
        }

        View chatView = mInflater.inflate(R.layout.chat, parent, false);
        ChatViewHolder chatViewHolder = new ChatViewHolder(chatView);
        return chatViewHolder;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position)
    {
        User user = (User) getItem(position);
        switch (mType)
        {
            case TYPE_CHATS:
                ChatViewHolder chatViewHolder = (ChatViewHolder) holder;
                chatViewHolder.mName.setText(user.getName());
                break;

            case TYPE_USERS:
                UserViewHolder userViewHolder = (UserViewHolder) holder;
                userViewHolder.mName.setText(user.getName());
                userViewHolder.mEmail.setText(user.getEmail());
        }
    }

    @Override
    public int getItemCount()
    {
        return mData.size();
    }

    public Object getItem(int position)
    {
        return mData.get(mKeys[position]);
    }

    public class ChatViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        public TextView mName;

        public ChatViewHolder(View itemView)
        {
            super(itemView);
            mName = (TextView) itemView.findViewById(R.id.chat_name);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view)
        {
            if (mClickListener != null)
            {
                mClickListener.onItemClick(mKeys[getAdapterPosition()]);
            }
        }
    }

    public class UserViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        public TextView mName;
        public TextView mEmail;

        public UserViewHolder(View itemView)
        {
            super(itemView);
            mName = (TextView) itemView.findViewById(R.id.user_name);
            mEmail = (TextView) itemView.findViewById(R.id.user_email);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view)
        {
            if (mClickListener != null)
            {
                mClickListener.onItemClick(mKeys[getAdapterPosition()]);
            }
        }
    }

    public void setClickListener(ItemClickListener itemClickListener)
    {
        this.mClickListener = itemClickListener;
    }

    public interface ItemClickListener
    {
        void onItemClick(String key);
    }
}