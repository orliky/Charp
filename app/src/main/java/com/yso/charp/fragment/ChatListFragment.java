package com.yso.charp.fragment;


import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.yso.charp.Interface.ChatItemClickListener;
import com.yso.charp.R;
import com.yso.charp.activity.MainActivity;
import com.yso.charp.adapter.ChatListAdapter;
import com.yso.charp.mannager.FireBaseManager;
import com.yso.charp.mannager.PersistenceManager;
import com.yso.charp.model.ChatTitle;
import com.yso.charp.utils.ContactsUtils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static com.yso.charp.activity.MainActivity.MY_FRAGMENT;
import static com.yso.charp.mannager.FireBaseManager.FB_CHILD_MESSAGES;
import static com.yso.charp.mannager.FireBaseManager.FB_CHILD_MESSAGES_LAST_MESSAGE;
import static com.yso.charp.mannager.FireBaseManager.FB_CHILD_MESSAGES_MESSAGE_TEXT;
import static com.yso.charp.mannager.FireBaseManager.getDatabaseReferencem;
import static com.yso.charp.mannager.FireBaseManager.getFirebaseUser;
import static com.yso.charp.mannager.FireBaseManager.getFirebaseUserPhone;


public class ChatListFragment extends Fragment implements ChatItemClickListener
{

    private HashMap<String, ChatTitle> chatList = new HashMap<>();
    private ChatListAdapter mAdapter;
    private ProgressBar mProgressBar;
    private TextView mNoChatsTitle;
    @SuppressLint ("StaticFieldLeak")
    private static ChatListFragment mInstance;
    private ValueEventListener mValueEventListener;
    private FloatingActionButton myFab;

    public ChatListFragment()
    {

    }

    public static ChatListFragment getInstance()
    {
        if (mInstance == null)
        {
            mInstance = new ChatListFragment();
        }
        return mInstance;
    }

    public ValueEventListener getValueEventListener()
    {
        return mValueEventListener;
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

        mAdapter = new ChatListAdapter(getContext(), chatList);
        RecyclerView recyclerView = view.findViewById(R.id.list_of_chats);

        mAdapter.setClickListener(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(mAdapter);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), new LinearLayoutManager(getContext()).getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);

        mProgressBar = view.findViewById(R.id.chat_list_progress);
        mNoChatsTitle = view.findViewById(R.id.no_chats_title);
        myFab = view.findViewById(R.id.fab);
        myFab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ((MainActivity) getActivity()).goToFragment(UserListFragment.getInstance(), "אנשי קשר");
            }
        });

        initValueEventListener();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        loadChatList();
        if(chatList.size() == 0)
        {
            mNoChatsTitle.setVisibility(View.VISIBLE);
        }
        else
        {
            mNoChatsTitle.setVisibility(View.GONE);
        }
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (getFirebaseUser() != null)
        {
            getDatabaseReferencem().child(FB_CHILD_MESSAGES).child(getFirebaseUserPhone()).removeEventListener(mValueEventListener);
        }
    }

    private void loadChatList()
    {
        //        mProgressBar.setVisibility(View.VISIBLE);
        FireBaseManager.loadChatList(mValueEventListener);
    }

    private void initValueEventListener()
    {
        mValueEventListener = new ValueEventListener()
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
                //                mProgressBar.setVisibility(View.GONE);
                mAdapter.setItems(chatList);
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {
                //                mProgressBar.setVisibility(View.GONE);
            }
        };
    }

    private String getMessage(Map messagesMap)
    {
        String message = null;
        Iterator iterator = messagesMap.keySet().iterator();
        while (iterator.hasNext())
        {
            String key = (String) iterator.next();
            if (key.equals(FB_CHILD_MESSAGES_LAST_MESSAGE))
            {
                Map lastMessageMap = (Map) messagesMap.get(key);
                Iterator iter = lastMessageMap.keySet().iterator();
                while (iter.hasNext())
                {
                    String k = (String) iter.next();
                    if (k.equals(FB_CHILD_MESSAGES_MESSAGE_TEXT))
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
        PersistenceManager.getInstance().setContactPhoneNumbers(ContactsUtils.getAllContactPhoneNumbers(getActivity()));
//        HashMap users = PersistenceManager.getInstance().getUsersMap();
//        if (users.get(key) == null)
//        {
//            showNewUserDialog(key);
//        }
//        else
//        {
            ((MainActivity) getActivity()).goToFragment(ChatFragment.newInstance(key), "");
//        }
    }

    private void showNewUserDialog(final String key)
    {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            builder = new AlertDialog.Builder(getActivity(), android.R.style.Theme_Material_Dialog_Alert);
        }
        else
        {
            builder = new AlertDialog.Builder(getActivity());
        }
        builder.setTitle("איש קשר חדש").setMessage("להוסיף לאנשי קשר?").setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int which)
            {

                Intent i = new Intent(Intent.ACTION_INSERT_OR_EDIT);
                i.setType(ContactsContract.Contacts.CONTENT_ITEM_TYPE);
                i.putExtra(ContactsContract.Intents.Insert.PHONE, key);
                startActivity(i);

            }
        }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.dismiss();
            }
        }).show();
    }

    public void refreshAdapter()
    {
        if (mAdapter != null)
        {
            mAdapter.notifyDataSetChanged();
        }
    }
}