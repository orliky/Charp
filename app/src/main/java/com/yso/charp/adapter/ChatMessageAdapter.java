package com.yso.charp.adapter;

import android.annotation.SuppressLint;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.yso.charp.R;
import com.yso.charp.model.ChatMessage;

import java.util.List;

public class ChatMessageAdapter extends RecyclerView.Adapter<ChatMessageAdapter.MessageViewHolder> {


    private List<ChatMessage> mMessageList;

    public ChatMessageAdapter(List<ChatMessage> mMessageList) {

        this.mMessageList = mMessageList;

    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message, parent, false);

        return new MessageViewHolder(v);

    }

    class MessageViewHolder extends RecyclerView.ViewHolder {

        LinearLayout group;
        TextView messageText;
        TextView displayName;
        TextView messageTime;

        MessageViewHolder(View view) {
            super(view);

            group = view.findViewById(R.id.message_group);
            messageText = view.findViewById(R.id.message_text);
            displayName = view.findViewById(R.id.message_from_user);
            messageTime = view.findViewById(R.id.message_time);

        }
    }

    @SuppressLint("NewApi")
    @Override
    public void onBindViewHolder(final MessageViewHolder viewHolder, int i) {

        ChatMessage c = mMessageList.get(i);
        String from_user = c.getMessageUser();

        DatabaseReference userDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(from_user);

        userDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String name = dataSnapshot.getKey();
                if(viewHolder.displayName.getVisibility() == View.VISIBLE) {
                    viewHolder.displayName.setText(name);
                }

                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                if (name.equals(FirebaseAuth.getInstance().getCurrentUser().getDisplayName())) {
                    layoutParams.gravity = Gravity.START;
                    viewHolder.group.setLayoutParams(layoutParams);
                    viewHolder.group.setBackgroundResource(R.drawable.bubble_right_green);
                    layoutParams.setMarginEnd(150);
                } else {
                    layoutParams.gravity = Gravity.END;
                    viewHolder.group.setLayoutParams(layoutParams);
                    viewHolder.group.setBackgroundResource(R.drawable.bubble_left_gray);
                    layoutParams.setMarginStart(150);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        viewHolder.messageText.setText(c.getMessageText());
        viewHolder.messageTime.setText(DateFormat.format("h:mm a", c.getMessageTime()));
    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }


}
