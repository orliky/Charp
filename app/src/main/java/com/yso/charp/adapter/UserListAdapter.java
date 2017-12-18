package com.yso.charp.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.yso.charp.Interface.ChatItemClickListener;
import com.yso.charp.R;
import com.yso.charp.model.User;
import com.yso.charp.utils.ContactsUtils;

import java.util.HashMap;

/**
 * Created by Admin on 21-Nov-17.
 */

public class UserListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
{
    private HashMap<String, User> mData = new HashMap<>();
    private String[] mKeys;
    private LayoutInflater mInflater;
    private ChatItemClickListener mClickListener;
    private Context mContext;

    public UserListAdapter(Context context, HashMap<String, User> data)
    {
        this.mContext = context;
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        mKeys = mData.keySet().toArray(new String[data.size()]);
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

        View userView = mInflater.inflate(R.layout.user_list_item, parent, false);
        UserViewHolder userViewHolder = new UserViewHolder(userView);
        return userViewHolder;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position)
    {
        User user = (User) getItem(position);

        UserViewHolder userViewHolder = (UserViewHolder) holder;
        userViewHolder.mName.setText(user.getName());
        String contactName =  ContactsUtils.getContactName(user.getPhone());
        userViewHolder.mPhone.setText(contactName);

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

    public class UserViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        public TextView mName;
        public TextView mPhone;

        public UserViewHolder(View itemView)
        {
            super(itemView);
            mName = (TextView) itemView.findViewById(R.id.user_name);
            mPhone = (TextView) itemView.findViewById(R.id.user_phone);
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