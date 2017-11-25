package com.yso.charp.fragment;


import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.yso.charp.R;
import com.yso.charp.adapter.ChatListAdapter;
import com.yso.charp.adapter.UserListAdapter;
import com.yso.charp.model.ChatMessage;
import com.yso.charp.model.ChatTitle;
import com.yso.charp.model.User;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 */
@SuppressLint("ValidFragment")
public class ChatListFragment extends Fragment implements ChatListAdapter.ItemClickListener {

    private RecyclerView listOfChats;
    private HashMap<String, ChatTitle> chatList = new HashMap<>();
    private ChatListAdapter mAdapter;


    public ChatListFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chart_list, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initAdapter(view);

        mAdapter.setClickListener(this);
        listOfChats.setLayoutManager(new LinearLayoutManager(getActivity()));
        listOfChats.setAdapter(mAdapter);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(listOfChats.getContext(), new LinearLayoutManager(getContext()).getOrientation());
        listOfChats.addItemDecoration(dividerItemDecoration);

        displayChatList();
    }

    private void initAdapter(View view) {
        mAdapter = new ChatListAdapter(getContext(), chatList);
        listOfChats = view.findViewById(R.id.list_of_chats);
    }

    private void displayChatList() {
        FirebaseDatabase.getInstance().getReference().child("Messages").child(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map userMessagesMap = (Map) dataSnapshot.getValue();
                if (userMessagesMap != null) {

                    Map.Entry<String, String> userMessagesEntry = (Map.Entry<String, String>) userMessagesMap.entrySet().iterator().next();
                    Map messagesMap = (Map) userMessagesMap.get(userMessagesEntry.getKey());

                    ChatTitle chatTitle = new ChatTitle();
                    chatTitle.setPhone(userMessagesEntry.getKey());
                    chatTitle.setLastMessage(getMessage(messagesMap));

                    chatList.put(userMessagesEntry.getKey(), chatTitle);
                    mAdapter.setItems(chatList);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private String getMessage(Map messagesMap) {
        String message = null;
        Iterator iterator = messagesMap.keySet().iterator();
        while (iterator.hasNext()) {
            String key = (String) iterator.next();
            if(key.equals("lastMessage"))
            {
                Map lastMessageMap = (Map) messagesMap.get(key);
                Iterator iter = lastMessageMap.keySet().iterator();
                while (iter.hasNext()) {
                    String k = (String) iter.next();
                    if(k.equals("messageText"))
                    {
                        message = (String) lastMessageMap.get(k);
                    }
                }
            }
        }
        return message;
    }

    @Override
    public void onItemClick(String key) {
        Bundle bundle = new Bundle();
        bundle.putString("user_phone", key);

        ChatFragment chatFragment = new ChatFragment();
        chatFragment.setArguments(bundle);
        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.container, chatFragment).commit();
    }
}