package com.yso.charp.fragment;


import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.yso.charp.Interface.ImageClickListener;
import com.yso.charp.R;
import com.yso.charp.adapter.ChatMessageAdapter;
import com.yso.charp.mannager.FireBaseManager;
import com.yso.charp.mannager.PersistenceManager;
import com.yso.charp.mannager.dataBase.ChatMessageRepo;
import com.yso.charp.model.ChatMessage;
import com.yso.charp.model.User;
import com.yso.charp.utils.NotificationUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.app.Activity.RESULT_OK;
import static com.yso.charp.mannager.FireBaseManager.FB_CHILD_MESSAGES;
import static com.yso.charp.mannager.FireBaseManager.FB_CHILD_MESSAGES_LAST_MESSAGE;


public class ChatFragment extends Fragment implements ImageClickListener {
    private final int PICK_IMAGE_REQUEST = 71;

    private RecyclerView mRecyclerView;
    private EditText input;
    private String mCurrentUserId, mChatUser;
    private DatabaseReference mRootRef;
    private List<ChatMessage> messagesList = new ArrayList<>();
    private ChatMessageAdapter mAdapter;
    private Bitmap mMessageBitmap;
    private ImageView mImageView;
    private HashMap<String, ChatMessage> mChatMap = new HashMap<>();
    private ChatMessageRepo mChatMessageRepo;
    @SuppressLint("StaticFieldLeak")
    private static ChatFragment mInstance;
    private ChildEventListener mChildEventListener;

    public ChatFragment() {

    }

    public static ChatFragment getInstance() {
        if (mInstance == null) {
            mInstance = new ChatFragment();
        }
        return mInstance;
    }

    @SuppressLint("NewApi")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        mChatMessageRepo = new ChatMessageRepo();

        input = view.findViewById(R.id.input);

        mRootRef = FireBaseManager.getDatabaseReferencem();
        mCurrentUserId = FireBaseManager.getFirebaseUserPhone();

        Bundle bundle = this.getArguments();

        if (bundle != null) {
            mChatUser = bundle.getString("user_phone");
        }

        mRecyclerView = view.findViewById(R.id.list_of_messages);
        initListFromDB();
        mAdapter = new ChatMessageAdapter(getContext(), messagesList);

        initChildEventListener();
        loadMessages();

        mAdapter.setClickListener(this);
        LinearLayoutManager linearLayout = new LinearLayoutManager(getActivity());
        linearLayout.setStackFromEnd(true);

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(linearLayout);

        mRecyclerView.setAdapter(mAdapter);

        mImageView = view.findViewById(R.id.image_input);
        ImageView chooseImage = view.findViewById(R.id.message_choose_image);
        chooseImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });

        return view;
    }

    private void initChildEventListener() {
        mChildEventListener =  new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (!dataSnapshot.getKey().equals(FB_CHILD_MESSAGES_LAST_MESSAGE)) {
                    ChatMessage chatMessage = dataSnapshot.getValue(ChatMessage.class);
//                    if(mChatMap.get(dataSnapshot.getKey()) == null) {
                    mChatMap.put(dataSnapshot.getKey(), chatMessage);

                    assert chatMessage != null;
                    getImage(chatMessage);

                    messagesList.add(chatMessage);
                    mAdapter.notifyDataSetChanged();
                    mRecyclerView.scrollToPosition(mAdapter.getItemCount() - 1);
//                    }
                    if (mChatMessageRepo.getById(dataSnapshot.getKey()) == null) {
                        mChatMessageRepo.insert(dataSnapshot.getKey(), chatMessage.getMessageUser(), chatMessage);
                    }
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
        };
    }

    private void initListFromDB() {
        messagesList.addAll(mChatMessageRepo.getByChat(mCurrentUserId, mChatUser));
        messagesList.addAll(mChatMessageRepo.getByChat(mChatUser, mCurrentUserId));
        Collections.sort(messagesList, new Comparator<ChatMessage>() {
            @Override
            public int compare(ChatMessage s1, ChatMessage s2) {
                return Long.compare(s1.getMessageTime(), s2.getMessageTime());
            }
        });
        for (ChatMessage chatMessage : messagesList) {
            getImage(chatMessage);
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FloatingActionButton fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
                input.setText("");
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri filePath = data.getData();
            try {
                mMessageBitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), filePath);

                float aspectRatio = mMessageBitmap.getWidth() / (float) mMessageBitmap.getHeight();
                int width = 480;
                int height = Math.round(width / aspectRatio);
                mMessageBitmap = Bitmap.createScaledBitmap(mMessageBitmap, width, height, false);

                mImageView.setVisibility(View.VISIBLE);
                mImageView.setImageBitmap(mMessageBitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
//        mChatMap.clear();
        FireBaseManager.getDatabaseReferencem().child(FB_CHILD_MESSAGES).child(mCurrentUserId).child(mChatUser).removeEventListener(mChildEventListener);
    }

    private void loadMessages() {
        messagesList.clear();
        FireBaseManager.loadChatMessages(mCurrentUserId, mChatUser, mChildEventListener);
    }

    private void getImage(ChatMessage chatMessage) {
        if (chatMessage.getBase64Image() != null && !chatMessage.getBase64Image().equals("")) {
            byte[] imageBytes = Base64.decode(chatMessage.getBase64Image(), Base64.DEFAULT);
            Bitmap decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);

            chatMessage.setBitmap(decodedImage);
        }
    }

    private void sendMessage() {
        final String message = input.getText().toString();
        if ((!TextUtils.isEmpty(message) && message.trim().length() > 0) || mMessageBitmap != null) {
            mImageView.setVisibility(View.GONE);
            String current_user_ref = FB_CHILD_MESSAGES + "/" + mCurrentUserId + "/" + mChatUser;
            String chat_user_ref = FB_CHILD_MESSAGES + "/" + mChatUser + "/" + mCurrentUserId;

            DatabaseReference user_message_push = mRootRef.child(FB_CHILD_MESSAGES).child(mCurrentUserId).child(mChatUser).push();
            String push_id = user_message_push.getKey();
            ChatMessage chatMessage = new ChatMessage(message, FireBaseManager.getFirebaseUserPhone());

            if (mMessageBitmap != null) {
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                mMessageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                byte[] b = stream.toByteArray();
                String b64Image = Base64.encodeToString(b, Base64.DEFAULT);
                chatMessage.setBase64Image(b64Image);

                mMessageBitmap = null;
            }

            Map<String, ChatMessage> messageUserMap = new HashMap<>();
            messageUserMap.put(current_user_ref + "/" + push_id, chatMessage);
            messageUserMap.put(current_user_ref + "/" + FB_CHILD_MESSAGES_LAST_MESSAGE, chatMessage);
            messageUserMap.put(chat_user_ref + "/" + push_id, chatMessage);
            messageUserMap.put(chat_user_ref + "/" + FB_CHILD_MESSAGES_LAST_MESSAGE, chatMessage);

            input.setText("");

            FireBaseManager.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if (databaseError != null) {
                        Log.d("CHAT_LOG", databaseError.getMessage());
                        return;
                    }
                    sendNotificationToUser(mChatUser, message);
                    mRecyclerView.scrollToPosition(mAdapter.getItemCount() - 1);
                    Snackbar.make(getActivity().findViewById(android.R.id.content), "נשלחה הודעה", Snackbar.LENGTH_SHORT).setAction("Action", null).show();
                }
            });
        } else {
            Snackbar.make(getActivity().findViewById(android.R.id.content), "אנא הכנס הודעה או תמונה", Snackbar.LENGTH_SHORT).setAction("Action", null).show();
        }
    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    private void sendNotificationToUser(String userPhone, String message) {
        HashMap users = PersistenceManager.getInstance().getUsersMap();
        User user = (User) users.get(userPhone);
        if (user != null && !user.getPhone().equals(FireBaseManager.getFirebaseUserPhone())) {
            NotificationUtils.sendNotification(user.getPhone(), FireBaseManager.getFirebaseUserPhone(), message, "chat_view");
        }
    }

    @Override
    public void onItemClick(Bitmap bitmap) {
        final Dialog nagDialog = new Dialog(getActivity(), android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
        nagDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        nagDialog.setContentView(R.layout.preview_image);
        ImageView ivPreview = nagDialog.findViewById(R.id.iv_preview_image);
        ImageView closePreview = nagDialog.findViewById(R.id.close_preview);
        closePreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nagDialog.dismiss();
            }
        });
        ivPreview.setImageBitmap(bitmap);
        nagDialog.getWindow().setBackgroundDrawable(null);
        nagDialog.show();
    }
}
