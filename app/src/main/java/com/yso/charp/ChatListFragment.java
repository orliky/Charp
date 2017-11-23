package com.yso.charp;


import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
@SuppressLint ("ValidFragment")
public class ChatListFragment extends Fragment implements MyRecyclerViewAdapter.ItemClickListener
{

    public enum TypeList
    {
        USERS, CHATS
    }

    private TypeList mTypeList;
    private RecyclerView listOfChats;
    private HashMap<String, User> userList = new HashMap<>();
    private MyRecyclerViewAdapter mAdapter;


    public ChatListFragment(TypeList typeList)
    {
        mTypeList = typeList;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View x = getView(inflater, container);
        if (x != null)
        {
            return x;
        }
        return inflater.inflate(R.layout.fragment_chart_list, container, false);
    }

    @Nullable
    private View getView(LayoutInflater inflater, ViewGroup container)
    {
        switch (mTypeList)
        {
            case CHATS:
                return inflater.inflate(R.layout.fragment_chart_list, container, false);

            case USERS:
                return inflater.inflate(R.layout.fragment_user_list, container, false);
        }
        return null;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        initAdapter(view);

        mAdapter.setClickListener(this);
        listOfChats.setLayoutManager(new LinearLayoutManager(getActivity()));
        listOfChats.setAdapter(mAdapter);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(listOfChats.getContext(), new LinearLayoutManager(getContext()).getOrientation());
        listOfChats.addItemDecoration(dividerItemDecoration);

        displayChatList();
    }

    private void initAdapter(View view)
    {
        switch (mTypeList)
        {
            case CHATS:
                mAdapter = new MyRecyclerViewAdapter(getContext(), userList, MyRecyclerViewAdapter.TYPE_CHATS);
                listOfChats = view.findViewById(R.id.list_of_chats);
                break;

            case USERS:
                mAdapter = new MyRecyclerViewAdapter(getContext(), userList, MyRecyclerViewAdapter.TYPE_USERS);
                listOfChats = view.findViewById(R.id.list_of_users);
                break;
        }
    }

    private void displayChatList()
    {

        FirebaseDatabase.getInstance().getReference().child("Users").addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                for (DataSnapshot snapshot : dataSnapshot.getChildren())
                {
                    User user = snapshot.getValue(User.class);
                    if (!user.getUID().equals(FirebaseAuth.getInstance().getCurrentUser().getUid()))
                    {
                        userList.put(snapshot.getKey(), user);
                    }
                }
                mAdapter.setItems(userList);
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });
    }

    @Override
    public void onItemClick(String key)
    {
        Bundle bundle = new Bundle();
        bundle.putString("user_id", key);

        ChatFragment chatFragment = new ChatFragment();
        chatFragment.setArguments(bundle);
        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.container, chatFragment).commit();
    }
}