package com.yso.charp;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatFragment extends Fragment
{
    private FirebaseListAdapter<ChatMessage> adapter;
    private ListView listOfMessages;
    private String chatWith;
    private  EditText input;

    public ChatFragment()
    {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        input = (EditText) view.findViewById(R.id.input);

        Bundle bundle = this.getArguments();

        if (bundle != null)
        {
            chatWith = bundle.getString("chat_with");

        }

        listOfMessages = (ListView) view.findViewById(R.id.list_of_messages);
        displayChatMessages();

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                // Read the input field and push a new instance
                // of ChatMessage to the Firebase database
                FirebaseDatabase.getInstance().getReference().child("Users").child(chatWith).child("Chat").push()
                        .setValue(new ChatMessage(input.getText().toString(), FirebaseAuth.getInstance().getCurrentUser().getDisplayName()));

                // Clear the input
                input.setText("");
            }
        });
    }

    private void displayChatMessages()
    {
        if(chatWith != null)
        {
            adapter = new FirebaseListAdapter<ChatMessage>(getActivity(), ChatMessage.class, R.layout.message, FirebaseDatabase.getInstance().getReference().child("Users").child(chatWith).child("Chat"))
            {
                @Override
                protected void populateView(View v, ChatMessage model, int position)
                {
                    // Get references to the views of message.xml
                    TextView messageText = (TextView) v.findViewById(R.id.message_text);
                    TextView messageUser = (TextView) v.findViewById(R.id.message_user);
                    TextView messageTime = (TextView) v.findViewById(R.id.message_time);

                    // Set their text
                    messageText.setText(model.getMessageText());
                    messageUser.setText(model.getMessageUser());

                    // Format the date before showing it
                    messageTime.setText(DateFormat.format("dd-MM-yyyy (HH:mm:ss)", model.getMessageTime()));

                }

                @Override
                protected void onDataChanged()
                {
                    super.onDataChanged();
                    listOfMessages.setSelection(adapter.getCount() - 1);
                }
            };

            listOfMessages.setAdapter(adapter);
            listOfMessages.setSelection(adapter.getCount() - 1);
        }
    }

}
