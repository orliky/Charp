package com.yso.charp.fragment;


import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Contacts;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.yso.charp.Interface.ChatItemClickListener;
import com.yso.charp.Interface.UpdateUsersListener;
import com.yso.charp.R;
import com.yso.charp.activity.MainActivity;
import com.yso.charp.adapter.ChatListAdapter;
import com.yso.charp.mannager.PersistenceManager;
import com.yso.charp.model.ChatTitle;
import com.yso.charp.model.User;
import com.yso.charp.utils.Utils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 */
@SuppressLint ("ValidFragment")
public class ChatListFragment extends Fragment implements ChatItemClickListener
{

    private RecyclerView mRecyclerView;
    private HashMap<String, ChatTitle> chatList = new HashMap<>();
    private ChatListAdapter mAdapter;
    private ProgressBar mProgressBar;


    public ChatListFragment()
    {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_chart_list, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        chatList = PersistenceManager.getInstance().getChatsMap();

        initAdapter(view);

        mAdapter.setClickListener(this);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setAdapter(mAdapter);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(), new LinearLayoutManager(getContext()).getOrientation());
        mRecyclerView.addItemDecoration(dividerItemDecoration);

        mProgressBar = (ProgressBar) view.findViewById(R.id.chat_list_progress) ;

        displayChatList();
    }

    private void initAdapter(View view)
    {
        mAdapter = new ChatListAdapter(getContext(), chatList);
        mRecyclerView = view.findViewById(R.id.list_of_chats);
    }

    private void displayChatList()
    {
        mProgressBar.setVisibility(View.VISIBLE);
        FirebaseDatabase.getInstance().getReference().child("Messages").child(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber()).addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                Map userMessagesMap = (Map) dataSnapshot.getValue();
                if (userMessagesMap != null)
                {

                    Iterator iterator = userMessagesMap.entrySet().iterator();
                    while (iterator.hasNext())
                    {
                        Map.Entry<String, String> userMessagesEntry = (Map.Entry<String, String>) iterator.next();
                        Map messagesMap = (Map) userMessagesMap.get(userMessagesEntry.getKey());

                        ChatTitle chatTitle = new ChatTitle();
                        chatTitle.setPhone(userMessagesEntry.getKey());
                        chatTitle.setLastMessage(getMessage(messagesMap));

                        chatList.put(userMessagesEntry.getKey(), chatTitle);
                    }
                }
                PersistenceManager.getInstance().setChatsMap(chatList);
                mProgressBar.setVisibility(View.GONE);
                mAdapter.setItems(chatList);
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });
    }

    private String getMessage(Map messagesMap)
    {
        String message = null;
        Iterator iterator = messagesMap.keySet().iterator();
        while (iterator.hasNext())
        {
            String key = (String) iterator.next();
            if (key.equals("lastMessage"))
            {
                Map lastMessageMap = (Map) messagesMap.get(key);
                Iterator iter = lastMessageMap.keySet().iterator();
                while (iter.hasNext())
                {
                    String k = (String) iter.next();
                    if (k.equals("messageText"))
                    {
                        message = (String) lastMessageMap.get(k);
                    }
                }
            }
        }
        return message;
    }

    @Override
    public void onItemClick(final String key)
    {
        PersistenceManager.getInstance().setContactPhoneNumbers(Utils.getAllContactPhoneNumbers(getActivity()));
        HashMap users = PersistenceManager.getInstance().getUsersMap();
        if(users.get(key) == null)
        {
            showNewUserDialog(key);
        }
        else
        {
            goToChatFragment(key);
        }
    }

    private void goToChatFragment(String key)
    {
        Bundle bundle = new Bundle();
        bundle.putString("user_phone", key);

        ((MainActivity) getActivity()).addChatFragment(bundle);
    }

    private void showNewUserDialog(final String key)
    {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(getActivity(), android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(getActivity());
        }
        builder.setTitle("איש קשר חדש")
                .setMessage("להוסיף לאנשי קשר?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mProgressBar.setVisibility(View.VISIBLE);

                        Intent i = new Intent(Intent.ACTION_INSERT_OR_EDIT);
                        i.setType(ContactsContract.Contacts.CONTENT_ITEM_TYPE);
                        i.putExtra(ContactsContract.Intents.Insert.PHONE,key);
                        startActivity(i);

                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
}