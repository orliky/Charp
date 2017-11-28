package com.yso.charp.fragment;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.yso.charp.Interface.ImageClickListener;
import com.yso.charp.R;
import com.yso.charp.adapter.ChatMessageAdapter;
import com.yso.charp.mannager.PersistenceManager;
import com.yso.charp.model.ChatMessage;
import com.yso.charp.model.User;
import com.yso.charp.utils.Utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.app.Activity.RESULT_OK;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatFragment extends Fragment implements ImageClickListener
{
    private final int PICK_IMAGE_REQUEST = 71;

    private FirebaseListAdapter<ChatMessage> adapter;
    private RecyclerView mRecyclerView;
    private String chatWith;
    private EditText input;
    private String mCurrentUserId, mChatUser;
    private DatabaseReference mRootRef;
    private FirebaseAuth mAuth;
    private final List<ChatMessage> messagesList = new ArrayList<>();
    private LinearLayoutManager mLinearLayout;
    private ChatMessageAdapter mAdapter;
    private Bitmap mMessageBitmap;
    private ImageView mImageView;
    private ImageView mChooseImage;

    public ChatFragment()
    {

    }

    @SuppressLint ("NewApi")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        input = (EditText) view.findViewById(R.id.input);

        mRootRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getCurrentUser().getPhoneNumber();

        Bundle bundle = this.getArguments();

        if (bundle != null)
        {
            mChatUser = bundle.getString("user_phone");
        }

        mRecyclerView = (RecyclerView) view.findViewById(R.id.list_of_messages);

        mAdapter = new ChatMessageAdapter(getContext(), messagesList);
        mAdapter.setClickListener(this);
        mLinearLayout = new LinearLayoutManager(getActivity());
        mLinearLayout.setStackFromEnd(true);

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(mLinearLayout);

        mRecyclerView.setAdapter(mAdapter);

        loadMessages();

        mImageView = (ImageView) view.findViewById(R.id.image_input);
        mChooseImage = (ImageView) view.findViewById(R.id.message_choose_image);
        mChooseImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                chooseImage();
            }
        });
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
                sendMessage();
                input.setText("");
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null)
        {
            Uri filePath = data.getData();
            try
            {
                mMessageBitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), filePath);
                mImageView.setVisibility(View.VISIBLE);
                mImageView.setImageBitmap(mMessageBitmap);
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    private void loadMessages()
    {
        mRootRef.child("Messages").child(mCurrentUserId).child(mChatUser).addChildEventListener(new ChildEventListener()
        {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s)
            {
                if (!dataSnapshot.getKey().equals("lastMessage"))
                {
                    ChatMessage chatMessage = dataSnapshot.getValue(ChatMessage.class);

                    messagesList.add(chatMessage);
                    mAdapter.notifyDataSetChanged();

                    mRecyclerView.scrollToPosition(mAdapter.getItemCount() - 1);
                }

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s)
            {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot)
            {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s)
            {

            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });

    }

    private void sendMessage()
    {
        final String message = input.getText().toString();
        if (!TextUtils.isEmpty(message) && message.trim().length() > 0)
        {
            mImageView.setVisibility(View.GONE);
            String current_user_ref = "Messages/" + mCurrentUserId + "/" + mChatUser;
            String chat_user_ref = "Messages/" + mChatUser + "/" + mCurrentUserId;

            DatabaseReference user_message_push = mRootRef.child("Messages").child(mCurrentUserId).child(mChatUser).push();

            String push_id = user_message_push.getKey();

            ChatMessage chatMessage = new ChatMessage(input.getText().toString(), FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber());

            if (mMessageBitmap != null)
            {
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                mMessageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream); // 'bitmap' is the image returned
                byte[] b = stream.toByteArray();
                String b64Image = Base64.encodeToString(b, Base64.DEFAULT);
                chatMessage.setBase64Image(b64Image);

                mMessageBitmap = null;
            }

            Map messageUserMap = new HashMap();
            messageUserMap.put(current_user_ref + "/" + push_id, chatMessage);
            messageUserMap.put(current_user_ref + "/" + "lastMessage", chatMessage);
            messageUserMap.put(chat_user_ref + "/" + push_id, chatMessage);
            messageUserMap.put(chat_user_ref + "/" + "lastMessage", chatMessage);

            input.setText("");

            mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener()
            {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference)
                {
                    if (databaseError != null)
                    {
                        Log.d("CHAT_LOG", databaseError.getMessage().toString());
                        return;
                    }
                    sendNotificationToUser(mChatUser, message);
                    mRecyclerView.scrollToPosition(mAdapter.getItemCount() - 1);
                }
            });
        }
        else
        {
            Snackbar.make(getActivity().findViewById(android.R.id.content), "אנא הכנס הודעה", Snackbar.LENGTH_SHORT).setAction("Action", null).show();
        }
    }

    private void chooseImage()
    {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    private void sendNotificationToUser(String userPhone, String message)
    {
        HashMap users = PersistenceManager.getInstance().getUsersMap();
        User user = (User) users.get(userPhone);
        if (!user.getPhone().equals(mAuth.getCurrentUser().getPhoneNumber()))
        {
            Utils.sendNotification(getActivity(), user.getPhone(), mAuth.getCurrentUser().getPhoneNumber(), message, "chat_view");
        }
    }

    @Override
    public void onItemClick(Bitmap bitmap) {
        Utils.openImage(getActivity(), bitmap);
    }
}
