package com.yso.charp.utils;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.Html;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.yso.charp.R;
import com.yso.charp.activity.MainActivity;
import com.yso.charp.mannager.FireBaseManager;
import com.yso.charp.model.Notification;

import java.util.HashMap;
import java.util.Map;

import static com.yso.charp.mannager.FireBaseManager.FB_CHILD_NOTIFICATION;

/**
 * Created by Admin on 30-Nov-17.
 */

public abstract class NotificationUtils
{
    public static void sendNotification(String user_id, String message, String description, String type)
    {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(FB_CHILD_NOTIFICATION).child(user_id);
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

        FireBaseManager.sendNotification(databaseReference, childUpdates, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference)
            {
                if (databaseError != null)
                {
                    Log.d("SEND_NOTIF", databaseError.getMessage().toString());
                    return;
                }
            }
        });
    }

    public static void showNotification(FirebaseDatabase database, FirebaseAuth firebaseAuth, Context context, Notification notification, String notification_key){
        FireBaseManager.setFlagNotificationAsSent(database, firebaseAuth, notification_key);

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
}
