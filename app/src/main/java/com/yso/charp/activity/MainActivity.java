package com.yso.charp.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ValueEventListener;
import com.yso.charp.Interface.SignOutListener;
import com.yso.charp.R;
import com.yso.charp.fragment.ChatFragment;
import com.yso.charp.fragment.ChatListFragment;
import com.yso.charp.fragment.GetSMSFragment;
import com.yso.charp.fragment.MainFragment;
import com.yso.charp.fragment.UserListFragment;
import com.yso.charp.mannager.FireBaseManager;

import static com.yso.charp.mannager.FireBaseManager.FB_CHILD_CLIENT_USERS;
import static com.yso.charp.mannager.FireBaseManager.FB_CHILD_MESSAGES;
import static com.yso.charp.mannager.FireBaseManager.getDatabaseReferencem;
import static com.yso.charp.mannager.FireBaseManager.getFirebaseUserPhone;

public class MainActivity extends AppCompatActivity
{
    public static final String MY_FRAGMENT = "MY_FRAGMENT";
    private static String TAG = MainActivity.class.getSimpleName();

    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 1;
    private static String[] PERMISSIONS_CONTACT = {Manifest.permission.READ_CONTACTS};

    private FirebaseUser mFirebaseUser;

    @RequiresApi (api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Restore instance state
        if (savedInstanceState != null)
        {
            onRestoreInstanceState(savedInstanceState);
        }

        mFirebaseUser = FireBaseManager.getFirebaseUser();

        checkPermission();
    }

    @RequiresApi (api = Build.VERSION_CODES.M)
    private void checkPermission()
    {
        if (checkSelfPermission(PERMISSIONS_CONTACT[0]) > PackageManager.PERMISSION_GRANTED)
        {
            Log.i(TAG, "Contact permissions have already been granted. Displaying contact details.");
            goToFirstFragment();
        }
        else
        {
            Log.i(TAG, "Contact permissions has NOT been granted. Requesting permission.");
            requestPermissions(PERMISSIONS_CONTACT, PERMISSIONS_REQUEST_READ_CONTACTS);
        }
    }

    public void goToFirstFragment()
    {
        if (mFirebaseUser == null)
        {
            /*startActivity(new Intent(this, PhoneAuthActivity.class));
            finish();*/
            getSupportFragmentManager().beginTransaction().add(R.id.container, new GetSMSFragment()).commit();
        }
        else
        {
            getSupportFragmentManager().beginTransaction().add(R.id.container, new MainFragment()).commit();
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
            getDatabaseReferencem().child(FB_CHILD_CLIENT_USERS).child(getFirebaseUserPhone()).removeEventListener(UserListFragment.getInstance().getValueEventListener());
            getDatabaseReferencem().child(FB_CHILD_MESSAGES).child(getFirebaseUserPhone()).removeEventListener(ChatListFragment.getInstance().getValueEventListener());
            FireBaseManager.signOut(this, new SignOutListener()
            {
                @Override
                public void onComplete()
                {
                    finish();
                }
            });
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        /*if (requestCode == SIGN_IN_REQUEST_CODE)
        {
            if (resultCode == RESULT_OK)
            {

                FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).
                        setValue(new User(FirebaseAuth.getInstance().getCurrentUser().getDisplayName(), FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber(), FirebaseAuth.getInstance().getCurrentUser().getUid()));
                init();
            }
            else
            {
                Snackbar.make(findViewById(android.R.id.content), "We couldn't sign you in. Please try again later.", Snackbar.LENGTH_SHORT).setAction("Action", null).show();

                finish();
            }
        }*/
    }

    @RequiresApi (api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        switch (requestCode)
        {
            case PERMISSIONS_REQUEST_READ_CONTACTS:
            {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    Log.d(TAG, "permission granted");
                    goToFirstFragment();
                }
                else
                {
                    Log.d(TAG, "permission denied");
                    Snackbar.make(findViewById(android.R.id.content), "You mast approve it", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                    checkPermission();
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        ChatListFragment.getInstance().refreshAdapter();
        UserListFragment.getInstance().refreshAdapter();
    }

    @Override
    public void onBackPressed()
    {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(MY_FRAGMENT);
        if(fragment instanceof ChatFragment)
        {
            setTitle("Charp");
            android.support.v4.app.FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left);
            ft.remove(getSupportFragmentManager().findFragmentById(R.id.container)).commit();
        }
        else
        {
            super.onBackPressed();
        }
    }

    public void goToChatFragment(String key)
    {
        ChatFragment chatFragment = ChatFragment.newInstance(key);
        android.support.v4.app.FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right);
        ft.add(R.id.container, chatFragment, MY_FRAGMENT).commit();
    }
}