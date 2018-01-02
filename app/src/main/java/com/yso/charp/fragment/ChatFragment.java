package com.yso.charp.fragment;


import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
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
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.yso.charp.Interface.ImageClickListener;
import com.yso.charp.R;
import com.yso.charp.activity.MainActivity;
import com.yso.charp.adapter.ChatMessageAdapter;
import com.yso.charp.mannager.FireBaseManager;
import com.yso.charp.mannager.PermissionManager;
import com.yso.charp.mannager.PersistenceManager;
import com.yso.charp.mannager.dataBase.ChatMessageRepo;
import com.yso.charp.model.ChatMessage;
import com.yso.charp.model.User;
import com.yso.charp.utils.ContactsUtils;
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
import static com.yso.charp.mannager.FireBaseManager.getDatabaseReferencem;
import static com.yso.charp.mannager.FireBaseManager.loadChatMessages;
import static com.yso.charp.mannager.PermissionManager.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE;
import static com.yso.charp.mannager.PermissionManager.PERMISSIONS_REQUEST_READ_CONTACTS;
import static com.yso.charp.utils.SelectImageUtils.getPath;
import static com.yso.charp.utils.SelectImageUtils.modifyOrientation;


public class ChatFragment extends Fragment implements ImageClickListener
{
    private final int PICK_IMAGE_REQUEST = 71;
    private static final String USER_PHONE = "user_phone";

    private RecyclerView mRecyclerView;
    private EditText input;
    private String mCurrentUserId, mChatUser;
    private DatabaseReference mRootRef;
    private List<ChatMessage> messagesList = new ArrayList<>();
    private ChatMessageAdapter mAdapter;
    private Bitmap mMessageBitmap;
    private ImageView mImageView;
    private TextView mAddContact;
    private ChatMessageRepo mChatMessageRepo;
    @SuppressLint ("StaticFieldLeak")
    private ChildEventListener mChildEventListener;
    @SuppressLint ("StaticFieldLeak")
    private static ChatFragment mInstance;

    public ChatFragment()
    {

    }

    public static ChatFragment newInstance(String key)
    {
        Bundle bundle = new Bundle();
        bundle.putString(USER_PHONE, key);

        ChatFragment chatFragment = new ChatFragment();
        chatFragment.setArguments(bundle);
        return chatFragment;
    }

    public static ChatFragment getInstance(String key)
    {
        if (mInstance == null)
        {
            mInstance = newInstance(key);
        }
        return mInstance;
    }

    @SuppressLint ("NewApi")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        mChatMessageRepo = new ChatMessageRepo();

        input = view.findViewById(R.id.input);

        mRootRef = getDatabaseReferencem();
        mCurrentUserId = FireBaseManager.getFirebaseUserPhone();

        Bundle bundle = this.getArguments();

        if (bundle != null)
        {
            mChatUser = bundle.getString(USER_PHONE);
            getActivity().setTitle(ContactsUtils.getContactName(mChatUser));
        }

        mRecyclerView = view.findViewById(R.id.list_of_messages);
        initListFromDB();
        mAdapter = new ChatMessageAdapter(getContext(), messagesList);

        initChildEventListener();

        mAdapter.setClickListener(this);
        LinearLayoutManager linearLayout = new LinearLayoutManager(getActivity());
        linearLayout.setStackFromEnd(true);

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(linearLayout);

        mRecyclerView.setAdapter(mAdapter);

        mImageView = view.findViewById(R.id.image_input);
        ImageView chooseImage = view.findViewById(R.id.message_choose_image);
        chooseImage.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                chooseImage();
            }
        });

        mAddContact = view.findViewById(R.id.add_contact);

        mAddContact.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent i = new Intent(Intent.ACTION_INSERT_OR_EDIT);
                i.setType(ContactsContract.Contacts.CONTENT_ITEM_TYPE);
                i.putExtra(ContactsContract.Intents.Insert.PHONE, mChatUser);
                startActivity(i);
            }
        });

        return view;
    }

    @RequiresApi (api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        switch (requestCode)
        {
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    chooseImage();
                }
                else
                {
                    Snackbar.make(getActivity().findViewById(android.R.id.content), "ללא אישור גישה לא ניתן לבחור תמונה", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                }
                break;
        }
    }

    private void initChildEventListener()
    {
        mChildEventListener = new ChildEventListener()
        {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s)
            {
                if (!dataSnapshot.getKey().equals(FB_CHILD_MESSAGES_LAST_MESSAGE))
                {
                    //if is not in db
                    if (mChatMessageRepo.getById(dataSnapshot.getKey()) == null)
                    {
                        ChatMessage chatMessage = dataSnapshot.getValue(ChatMessage.class);
                        getImage(chatMessage);
                        messagesList.add(chatMessage);
                        //add to DB
                        mChatMessageRepo.insert(dataSnapshot.getKey(), mChatUser, chatMessage);
                    }
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
        };
    }

    private void initListFromDB()
    {
        messagesList.addAll(mChatMessageRepo.getByChat(mCurrentUserId, mChatUser));
        messagesList.addAll(mChatMessageRepo.getByChat(mChatUser, mCurrentUserId));
        for (ChatMessage chatMessage : messagesList)
        {
            getImage(chatMessage);
        }
        Collections.sort(messagesList, new Comparator<ChatMessage>()
        {
            @Override
            public int compare(ChatMessage s1, ChatMessage s2)
            {
                return Long.compare(s1.getMessageTime(), s2.getMessageTime());
            }
        });
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        FloatingActionButton fab = view.findViewById(R.id.fab);
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

                float aspectRatio = mMessageBitmap.getWidth() / (float) mMessageBitmap.getHeight();
                int width = 480;
                int height = Math.round(width / aspectRatio);
                mMessageBitmap = Bitmap.createScaledBitmap(mMessageBitmap, width, height, false);

                mMessageBitmap = modifyOrientation(mMessageBitmap, getPath(getActivity(), filePath));

                mImageView.setVisibility(View.VISIBLE);
                mImageView.setImageBitmap(mMessageBitmap);
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
        loadMessages();

        HashMap users = PersistenceManager.getInstance().getUsersMap();
        if (users.get(mChatUser) == null)
        {
            mAddContact.setVisibility(View.VISIBLE);
        }
        else
        {
            mAddContact.setVisibility(View.GONE);
        }
    }

    @Override
    public void onPause()
    {
        super.onPause();
        getDatabaseReferencem().child(FB_CHILD_MESSAGES).child(mCurrentUserId).child(mChatUser).removeEventListener(mChildEventListener);
    }

    private void loadMessages()
    {
        loadChatMessages(mCurrentUserId, mChatUser, mChildEventListener);
    }

    private void getImage(ChatMessage chatMessage)
    {
        if (chatMessage.getBase64Image() != null && !chatMessage.getBase64Image().equals(""))
        {
            byte[] imageBytes = Base64.decode(chatMessage.getBase64Image(), Base64.DEFAULT);
            Bitmap decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);

            chatMessage.setBitmap(decodedImage);
        }
    }

    private void sendMessage()
    {
        final String message = input.getText().toString();
        if ((!TextUtils.isEmpty(message) && message.trim().length() > 0) || mMessageBitmap != null)
        {
            mImageView.setVisibility(View.GONE);
            String current_user_ref = FB_CHILD_MESSAGES + "/" + mCurrentUserId + "/" + mChatUser;
            String chat_user_ref = FB_CHILD_MESSAGES + "/" + mChatUser + "/" + mCurrentUserId;

            DatabaseReference user_message_push = mRootRef.child(FB_CHILD_MESSAGES).child(mCurrentUserId).child(mChatUser).push();
            String push_id = user_message_push.getKey();
            ChatMessage chatMessage = new ChatMessage(message, FireBaseManager.getFirebaseUserPhone());

            if (mMessageBitmap != null)
            {
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

            FireBaseManager.updateChildren(messageUserMap, new DatabaseReference.CompletionListener()
            {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference)
                {
                    if (databaseError != null)
                    {
                        Log.d("CHAT_LOG", databaseError.getMessage());
                        return;
                    }
                    //send notif
                    sendNotificationToUser(mChatUser, message);
                    //scroll to bottom
                    mRecyclerView.scrollToPosition(mAdapter.getItemCount() - 1);
                    //show sign on screen
                    Snackbar.make(getActivity().findViewById(android.R.id.content), "נשלחה הודעה", Snackbar.LENGTH_SHORT).setAction("Action", null).show();
                }
            });
        }
        else
        {
            Snackbar.make(getActivity().findViewById(android.R.id.content), "אנא הכנס הודעה או תמונה", Snackbar.LENGTH_SHORT).setAction("Action", null).show();
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
        if (user != null && !user.getPhone().equals(FireBaseManager.getFirebaseUserPhone()))
        {
            NotificationUtils.sendNotification(user.getPhone(), FireBaseManager.getFirebaseUserPhone(), message, "chat_view");
        }
    }

    @Override
    public void onItemClick(Bitmap bitmap)
    {
        final Dialog nagDialog = new Dialog(getActivity(), android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
        nagDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        nagDialog.setContentView(R.layout.preview_image);
        ImageView ivPreview = nagDialog.findViewById(R.id.iv_preview_image);
        ImageView closePreview = nagDialog.findViewById(R.id.close_preview);
        closePreview.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                nagDialog.dismiss();
            }
        });
        ivPreview.setImageBitmap(bitmap);
        nagDialog.getWindow().setBackgroundDrawable(null);
        nagDialog.show();
    }
}
