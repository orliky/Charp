package com.yso.charp.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.Html;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.yso.charp.R;
import com.yso.charp.activity.MainActivity;
import com.yso.charp.model.Notification;
import com.yso.charp.utils.Utils;


public class FirebaseNotificationService extends Service {

    SharedPreferences sharedPreferences;
    public FirebaseDatabase mDatabase;
    FirebaseAuth firebaseAuth;
    Context context;
    static String TAG = "FirebaseService";

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        mDatabase = FirebaseDatabase.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        setupNotificationListener();
    }


    private boolean alReadyNotified(String key){
        if(sharedPreferences.getBoolean(key,false)){
            return true;
        }else{
            return false;
        }
    }


    private void saveNotificationKey(String key){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key,true);
        editor.commit();
    }

    private void setupNotificationListener() {

        mDatabase.getReference().child("notifications")
                .child(firebaseAuth.getCurrentUser().getPhoneNumber())
                .orderByChild("status").equalTo(0)
                .addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if(dataSnapshot != null){
                    Notification notification = dataSnapshot.getValue(Notification.class);
                    Utils.showNotification(mDatabase, firebaseAuth, context, notification,dataSnapshot.getKey());
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

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        startService(new Intent(getApplicationContext(), FirebaseNotificationService.class));
    }




}
