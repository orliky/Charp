package com.yso.charp;

import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class ChatMessageAdapter extends RecyclerView.Adapter<ChatMessageAdapter.MessageViewHolder>{


    private List<ChatMessage> mMessageList;

    ChatMessageAdapter(List<ChatMessage> mMessageList) {

        this.mMessageList = mMessageList;

    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message ,parent, false);

        return new MessageViewHolder(v);

    }

    class MessageViewHolder extends RecyclerView.ViewHolder {

        TextView messageText;
        TextView displayName;
        TextView messageTime;

        MessageViewHolder(View view) {
            super(view);


            messageText = view.findViewById(R.id.message_text);
            displayName = view.findViewById(R.id.message_from_user);
            messageTime = view.findViewById(R.id.message_time);

        }
    }

    @Override
    public void onBindViewHolder(final MessageViewHolder viewHolder, int i) {

        ChatMessage c = mMessageList.get(i);
        String from_user = c.getMessageUser();

        DatabaseReference userDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(from_user);

        userDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String name = dataSnapshot.getKey();
                viewHolder.displayName.setText(name);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        viewHolder.messageText.setText(c.getMessageText());
        viewHolder.messageTime.setText(DateFormat.format("dd-MM-yyyy (HH:mm:ss)", c.getMessageTime()));
    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }



}
