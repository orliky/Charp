package com.yso.charp;

import android.app.Fragment;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity
{
    private static int SIGN_IN_REQUEST_CODE = 9981;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        if (FirebaseAuth.getInstance().getCurrentUser() == null)
        {
            // Start sign in/sign up activity
            startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder().build(), SIGN_IN_REQUEST_CODE);
        }
        else
        {
            // User is already signed in. Therefore, display
            // a welcome Toast
            Toast.makeText(this, "Welcome " + FirebaseAuth.getInstance().getCurrentUser().getDisplayName(), Toast.LENGTH_LONG).show();

            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

            getSupportFragmentManager().beginTransaction().add(R.id.container, new ChatListFragment()).commit();

            //            chatID = FirebaseDatabase.getInstance().getReference().child().push().getKey();


            /*String UID = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
            Query query = rootRef.child(FirebaseAuth.getInstance().getCurrentUser().getDisplayName()).child(chatID).orderByChild(UID).equalTo(false);
            query.addChildEventListener(new ChildEventListener()
            {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s)
                {
                    String messageID = dataSnapshot.getKey();
                    setNotification(messageID);
                    //TODO: Handle Notification here, using the messageID
                    // A datasnapshot received here will be a new message that the user has not read
                    // If you want to display data about the message or chat,
                    // Use the chatID and/or messageID and declare a new
                    // SingleValueEventListener here, and add it to the chat/message DatabaseReference.
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s)
                {
                    String messageID = dataSnapshot.getKey();
                    //TODO: Remove the notification
                    // If the user reads the message in the app, before checking the notification
                    // then the notification is no longer relevant, remove it here.
                    // In onChildAdded you could use the messageID(s) to keep track of the notifications
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
            });*/
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {


        if (item.getItemId() == R.id.menu_sign_out)
        {
            AuthUI.getInstance().signOut(this).addOnCompleteListener(new OnCompleteListener<Void>()
            {
                @Override
                public void onComplete(@NonNull Task<Void> task)
                {
                    Toast.makeText(MainActivity.this, "You have been signed out.", Toast.LENGTH_LONG).show();

                    finish();
                }
            });
        }
        else if (item.getItemId() == R.id.menu_user_list)
        {
            if (getSupportFragmentManager().findFragmentById(R.id.container) instanceof ChatListFragment)
            {
                getSupportFragmentManager().beginTransaction().replace(R.id.container, new UserListFragment()).commit();
                item.setTitle("Chats");
            }
            else if (getSupportFragmentManager().findFragmentById(R.id.container) instanceof UserListFragment || getSupportFragmentManager().findFragmentById(R.id.container) instanceof ChatFragment)
            {
                getSupportFragmentManager().beginTransaction().replace(R.id.container, new ChatListFragment()).commit();
                item.setTitle("Users");
            }
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SIGN_IN_REQUEST_CODE)
        {
            if (resultCode == RESULT_OK)
            {
                Toast.makeText(this, "Successfully signed in. Welcome!", Toast.LENGTH_LONG).show();
                getSupportFragmentManager().beginTransaction().add(R.id.container, new ChatListFragment()).commit();

                FirebaseDatabase.getInstance().getReference().child("Users").push().
                        setValue(new User(FirebaseAuth.getInstance().getCurrentUser().getDisplayName(),
                        FirebaseAuth.getInstance().getCurrentUser().getEmail(),
                        FirebaseAuth.getInstance().getCurrentUser().getUid()));
            }
            else
            {
                Toast.makeText(this, "We couldn't sign you in. Please try again later.", Toast.LENGTH_LONG).show();

                // Close the app
                finish();
            }
        }

    }

    public void setNotification(String message)
    {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder b = new NotificationCompat.Builder(this);

        b.setAutoCancel(true).setDefaults(Notification.DEFAULT_ALL).setWhen(System.currentTimeMillis()).setSmallIcon(R.drawable.send).setTicker("Charp").setContentTitle(FirebaseAuth.getInstance().getCurrentUser().getDisplayName()).setContentText(message).setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND).setContentIntent(contentIntent).setContentInfo("Info");


        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, b.build());
    }
}
