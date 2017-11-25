package com.yso.charp.fragment;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.yso.charp.adapter.ChatMessageAdapter;
import com.yso.charp.R;
import com.yso.charp.model.ChatMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatFragment extends Fragment {
    private FirebaseListAdapter<ChatMessage> adapter;
    private RecyclerView listOfMessages;
    private String chatWith;
    private EditText input;
    private String mCurrentUserId, mChatUser;
    private DatabaseReference mRootRef;
    private FirebaseAuth mAuth;
    private final List<ChatMessage> messagesList = new ArrayList<>();
    private LinearLayoutManager mLinearLayout;
    private ChatMessageAdapter mAdapter;

    public ChatFragment() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        input = (EditText) view.findViewById(R.id.input);

        mRootRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getCurrentUser().getPhoneNumber();

        Bundle bundle = this.getArguments();

        if (bundle != null) {
            mChatUser = bundle.getString("user_phone");
        }

        listOfMessages = (RecyclerView) view.findViewById(R.id.list_of_messages);
        mAdapter = new ChatMessageAdapter(messagesList);
        mLinearLayout = new LinearLayoutManager(getActivity());

        listOfMessages.setHasFixedSize(true);
        listOfMessages.setLayoutManager(mLinearLayout);

        listOfMessages.setAdapter(mAdapter);
//        displayChatMessages();
        loadMessages();

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Read the input field and push a new instance
                // of ChatMessage to the Firebase database
//                FirebaseDatabase.getInstance().getReference().child("Users").child(chatWith).child("Chat").push()
//                        .setValue(new ChatMessage(input.getText().toString(), FirebaseAuth.getInstance().getCurrentUser().getDisplayName()));
                sendMessage();
                // Clear the input
                input.setText("");
            }
        });
    }

    private void displayChatMessages() {
        if (chatWith != null) {
            adapter = new FirebaseListAdapter<ChatMessage>(getActivity(), ChatMessage.class, R.layout.message, FirebaseDatabase.getInstance().getReference().child("Users").child(chatWith).child("Chat")) {
                @Override
                protected void populateView(View v, ChatMessage model, int position) {
                    // Get references to the views of message.xml
                    TextView messageText = v.findViewById(R.id.message_text);
                    TextView messageUser = v.findViewById(R.id.message_from_user);
                    TextView messageTime = v.findViewById(R.id.message_time);

                    // Set their text
                    messageText.setText(model.getMessageText());
                    messageUser.setText(model.getMessageUser());

                    // Format the date before showing it
                    messageTime.setText(DateFormat.format("dd-MM-yyyy (HH:mm:ss)", model.getMessageTime()));

                }

                @Override
                protected void onDataChanged() {
                    super.onDataChanged();
//                    listOfMessages.setSelection(adapter.getCount() - 1);
                }
            };

//            listOfMessages.setAdapter(adapter);
//            listOfMessages.setSelection(adapter.getCount() - 1);
        }
    }

    private void loadMessages() {
        mRootRef.child("Messages").child(mCurrentUserId).child(mChatUser).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (!dataSnapshot.getKey().equals("lastMessage")) {
                    ChatMessage chatMessage = dataSnapshot.getValue(ChatMessage.class);

                    messagesList.add(chatMessage);
                    mAdapter.notifyDataSetChanged();
                }

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void sendMessage() {


        String message = input.getText().toString();

        if (!TextUtils.isEmpty(message)) {

            String current_user_ref = "Messages/" + mCurrentUserId + "/" + mChatUser;
            String chat_user_ref = "Messages/" + mChatUser + "/" + mCurrentUserId;

            DatabaseReference user_message_push = mRootRef.child("Messages")
                    .child(mCurrentUserId).child(mChatUser).push();

            String push_id = user_message_push.getKey();

//            Map messageMap = new HashMap();
//            messageMap.put("message", message);
//            messageMap.put("seen", false);
//            messageMap.put("type", "text");
//            messageMap.put("time", ServerValue.TIMESTAMP);
//            messageMap.put("from", mCurrentUserId);
            ChatMessage messageMap = new ChatMessage(input.getText().toString(), FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
            Map messageUserMap = new HashMap();
            messageUserMap.put(current_user_ref + "/" + push_id, messageMap);
            messageUserMap.put(current_user_ref + "/" + "lastMessage", messageMap);
            messageUserMap.put(chat_user_ref + "/" + push_id, messageMap);
            messageUserMap.put(chat_user_ref + "/" + "lastMessage", messageMap);

            input.setText("");

            mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                    if (databaseError != null) {

                        Log.d("CHAT_LOG", databaseError.getMessage().toString());

                    }

                }
            });

        }

    }

}
