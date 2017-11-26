package com.yso.charp.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.yso.charp.Interface.ChatItemClickListener;
import com.yso.charp.R;
import com.yso.charp.model.ChatMessage;
import com.yso.charp.model.ChatTitle;
import com.yso.charp.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by Admin on 21-Nov-17.
 */

public class ChatListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
{
    private HashMap<String, ChatTitle> mData = new HashMap<>();
    private String[] mKeys;
    private LayoutInflater mInflater;
    private ChatItemClickListener mClickListener;
    private Context mContext;

    public ChatListAdapter(Context context, HashMap<String, ChatTitle> data)
    {
        this.mContext = context;
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        mKeys = mData.keySet().toArray(new String[data.size()]);
    }

    public void setItems(HashMap<String, ChatTitle> data)
    {
        mData = data;
        mKeys = mData.keySet().toArray(new String[data.size()]);
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {

        View chatView = mInflater.inflate(R.layout.chat_list_item, parent, false);
        return new ChatViewHolder(chatView);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position)
    {
        ChatViewHolder chatViewHolder = (ChatViewHolder) holder;

        ChatTitle chatTitle = (ChatTitle) getItem(position);
        String contactName =  Utils.getContactName(chatTitle.getPhone(), mContext);
        String name = contactName.equals("") ? chatTitle.getPhone() : contactName;
        chatViewHolder.mName.setText(name);
        chatViewHolder.mLastMessage.setText(chatTitle.getLastMessage());
    }

    @Override
    public int getItemCount()
    {
        return mData.size();
    }

    private Object getItem(int position)
    {
        return mData.get(mKeys[position]);
    }

    public class ChatViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        TextView mName;
        TextView mLastMessage;

        ChatViewHolder(View itemView)
        {
            super(itemView);
            mName = (TextView) itemView.findViewById(R.id.chat_name);
            mLastMessage = (TextView) itemView.findViewById(R.id.last_message);
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

    public void setClickListener(ChatItemClickListener itemClickListener)
    {
        this.mClickListener = itemClickListener;
    }
}