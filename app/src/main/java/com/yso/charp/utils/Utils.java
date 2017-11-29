package com.yso.charp.utils;

import android.app.Dialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.Html;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.yso.charp.Interface.UpdateUsersListener;
import com.yso.charp.R;
import com.yso.charp.activity.MainActivity;
import com.yso.charp.mannager.PersistenceManager;
import com.yso.charp.model.Notification;
import com.yso.charp.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by orlik on 24/11/2017.
 */

public abstract class Utils
{

    public static String getContactName(final String phoneNumber, Context context)
    {
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));

        String[] projection = new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME};

        String contactName = "";
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);

        if (cursor != null)
        {
            if (cursor.moveToFirst())
            {
                contactName = cursor.getString(0);
            }
            cursor.close();
        }

        return contactName;
    }

    public static void sendNotification(final Context context, String user_id, String message, String description, String type)
    {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("notifications").child(user_id);
        String pushKey = databaseReference.push().getKey();

        Notification notification = new Notification();
        notification.setDescription(description);
        notification.setMessage(message);
        notification.setUser_id(user_id);
        notification.setType(type);

        Map<String, Object> forumValues = notification.toMap();
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put(pushKey, forumValues);
        databaseReference.setPriority(ServerValue.TIMESTAMP);
        databaseReference.updateChildren(childUpdates, new DatabaseReference.CompletionListener()
        {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference)
            {
                if (databaseError == null)
                {
                }
            }
        });
    }

    public static void openDialogImage(Context context, Bitmap bitmap) {
        final Dialog nagDialog = new Dialog(context, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
        nagDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        nagDialog.setContentView(R.layout.preview_image);
        ImageView ivPreview = (ImageView) nagDialog.findViewById(R.id.iv_preview_image);
        ImageView closePreview = (ImageView) nagDialog.findViewById(R.id.close_preview);
        closePreview.setOnClickListener(new View.OnClickListener() {
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

    public static void showNotification(FirebaseDatabase database, FirebaseAuth firebaseAuth, Context context, Notification notification, String notification_key){
        flagNotificationAsSent(database, firebaseAuth, notification_key);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(notification.getDescription())
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setContentText(Html.fromHtml(notification.getMessage()
                ))
                .setAutoCancel(true);

        Intent backIntent = new Intent(context, MainActivity.class);
        backIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        Intent intent = new Intent(context, MainActivity.class);

        /*  Use the notification type to switch activity to stack on the main activity*/
        if(notification.getType().equals("chat_view")){
            intent = new Intent(context, MainActivity.class);
        }


        final PendingIntent pendingIntent = PendingIntent.getActivities(context, 900,
                new Intent[] {backIntent}, PendingIntent.FLAG_ONE_SHOT);


        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(MainActivity.class);

        mBuilder.setContentIntent(pendingIntent);


        NotificationManager mNotificationManager =  (NotificationManager)context. getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(1, mBuilder.build());

    }

    private static void flagNotificationAsSent(FirebaseDatabase database, FirebaseAuth firebaseAuth, String notification_key) {
        database.getReference().child("notifications")
                .child(firebaseAuth.getCurrentUser().getPhoneNumber())
                .child(notification_key)
                .child("status")
                .setValue(1);
    }

    public static ArrayList<String> getAllContactPhoneNumbers(Context context) {
        String CONTACT_ID = ContactsContract.Contacts._ID;
        String HAS_PHONE_NUMBER = ContactsContract.Contacts.HAS_PHONE_NUMBER;

        String PHONE_NUMBER = ContactsContract.CommonDataKinds.Phone.NUMBER;
        String PHONE_CONTACT_ID = ContactsContract.CommonDataKinds.Phone.CONTACT_ID;

        ContentResolver cr = context.getContentResolver();

        Cursor pCur = cr.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{PHONE_NUMBER, PHONE_CONTACT_ID},
                null,
                null,
                null
        );
        if(pCur != null){
            if(pCur.getCount() > 0) {
                HashMap<Integer, ArrayList<String>> phones = new HashMap<>();
                while (pCur.moveToNext()) {
                    Integer contactId = pCur.getInt(pCur.getColumnIndex(PHONE_CONTACT_ID));
                    ArrayList<String> curPhones = new ArrayList<>();
                    if (phones.containsKey(contactId)) {
                        curPhones = phones.get(contactId);
                    }
                    curPhones.add(pCur.getString(pCur.getColumnIndex(PHONE_NUMBER)));
                    phones.put(contactId, curPhones);
                }
                Cursor cur = cr.query(
                        ContactsContract.Contacts.CONTENT_URI,
                        new String[]{CONTACT_ID, HAS_PHONE_NUMBER},
                        HAS_PHONE_NUMBER + " > 0",
                        null,null);
                if (cur != null) {
                    if (cur.getCount() > 0) {
                        ArrayList<String> contacts = new ArrayList<>();
                        while (cur.moveToNext()) {
                            int id = cur.getInt(cur.getColumnIndex(CONTACT_ID));
                            if(phones.containsKey(id)) {
                                contacts.addAll(phones.get(id));
                            }
                        }
                        return contacts;
                    }
                    cur.close();
                }
            }
            pCur.close();
        }
        return null;
    }

    public static void updateClientUsers(final UpdateUsersListener updateUsersListener)
    {
        final HashMap<String, User> clientUsers = new HashMap<>();
        final HashMap<String, User> allUsers = new HashMap<>();
        FirebaseDatabase.getInstance().getReference().child("Users").addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                for (DataSnapshot snapshot : dataSnapshot.getChildren())
                {
                    User user = snapshot.getValue(User.class);
                    if (!user.getPhone().equals(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber()))
                    {
                        allUsers.put(user.getPhone(), user);
                    }
                }

                ArrayList contacts = PersistenceManager.getInstance().getContactPhoneNumbers();

                for (Object phone : contacts)
                {
                    if (allUsers.get(phone) != null)
                    {
                        clientUsers.put((String) phone, allUsers.get(phone));
                    }
                }

                FirebaseDatabase.getInstance().getReference().child("ClientUsers").child(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber()).setValue(clientUsers);

                PersistenceManager.getInstance().setUsersMap(clientUsers);

                updateUsersListener.onDataChange(clientUsers);
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });
    }
}
