package com.yso.charp.activity;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.yso.charp.R;
import com.yso.charp.fragment.ChatFragment;
import com.yso.charp.fragment.SectionsPagerAdapter;
import com.yso.charp.service.FirebaseNotificationService;

public class MainActivity extends AppCompatActivity
{
    private static String TAG = MainActivity.class.getSimpleName();

    private static int SIGN_IN_REQUEST_CODE = 9981;

    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 1;
    private static String[] PERMISSIONS_CONTACT = {Manifest.permission.READ_CONTACTS};

    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private FrameLayout mContainer;
    private AppBarLayout mAppBarLayout;

    @RequiresApi (api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkPernission();
    }

    @RequiresApi (api = Build.VERSION_CODES.M)
    private void checkPernission()
    {
        if (checkSelfPermission(PERMISSIONS_CONTACT[0]) > PackageManager.PERMISSION_GRANTED)
        {
            Log.i(TAG, "Contact permissions have already been granted. Displaying contact details.");
            init();
        }
        else
        {
            Log.i(TAG, "Contact permissions has NOT been granted. Requesting permission.");
            requestPermissions(PERMISSIONS_CONTACT, PERMISSIONS_REQUEST_READ_CONTACTS);
        }
    }

    private void init()
    {
        if (FirebaseAuth.getInstance().getCurrentUser() == null)
        {
            // Start sign in/sign up activity
            //            startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder().build(), SIGN_IN_REQUEST_CODE);
            startActivity(new Intent(this, PhoneAuthActivity.class));
            finish();
        }
        else
        {
            //            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

            // User is already signed in. Therefore, display
            // a welcome Toast
            Snackbar.make(findViewById(android.R.id.content), "Welcome " + FirebaseAuth.getInstance().getCurrentUser().getDisplayName(), Snackbar.LENGTH_SHORT).setAction("Action", null).show();

            mContainer = (FrameLayout) findViewById(R.id.container);
            mAppBarLayout = (AppBarLayout) findViewById(R.id.appBarLayout);
            //Tabs
            mViewPager = (ViewPager) findViewById(R.id.main_tabPager);
            mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

            mViewPager.setAdapter(mSectionsPagerAdapter);

            mTabLayout = (TabLayout) findViewById(R.id.main_tabs);
            mTabLayout.setupWithViewPager(mViewPager);

            startService(new Intent(this, FirebaseNotificationService.class));
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
                    finish();
                }
            });
        }
        //        else
         /*   if (item.getItemId() == R.id.menu_user_list)
        {
            getSupportFragmentManager().beginTransaction().replace(R.id.container, new UserListFragment()).commit();
        }
        else if (item.getItemId() == R.id.menu_chats_list)
        {
            getSupportFragmentManager().beginTransaction().replace(R.id.container, new ChatListFragment()).commit();
        }*/
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        //        if (requestCode == SIGN_IN_REQUEST_CODE)
        //        {
        //            if (resultCode == RESULT_OK)
        //            {
        //                Toast.makeText(this, "Successfully signed in. Welcome!", Toast.LENGTH_LONG).show();
        //                getSupportFragmentManager().beginTransaction().replace(R.id.container, new UserListFragment(UserListFragment.TypeList.CHATS)).commit();
        //
        //                FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).
        //                        setValue(new User(FirebaseAuth.getInstance().getCurrentUser().getDisplayName(), FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber(), FirebaseAuth.getInstance().getCurrentUser().getUid()));
        //
        //            }
        //            else
        //            {
        //                Toast.makeText(this, "We couldn't sign you in. Please try again later.", Toast.LENGTH_LONG).show();
        //
        //                finish();
        //            }
        //        }

    }

    @RequiresApi (api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {
        switch (requestCode)
        {
            case PERMISSIONS_REQUEST_READ_CONTACTS:
            {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    Log.d(TAG, "permission granted");
                    init();
                }
                else
                {
                    Log.d(TAG, "permission denied");
                    Snackbar.make(findViewById(android.R.id.content), "You mast approve it", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                    checkPernission();
                }
                return;
            }
        }
    }

    @Override
    public void onBackPressed()
    {
        if (mContainer.getVisibility() == View.GONE)
        {
            super.onBackPressed();
        }
        else
        {
            mContainer.setVisibility(View.GONE);
            mAppBarLayout.setVisibility(View.VISIBLE);
        }
    }

    public void addChatFragment(Bundle bundle)
    {
        mAppBarLayout.setVisibility(View.GONE);
        mContainer.setVisibility(View.VISIBLE);
        ChatFragment chatFragment = new ChatFragment();
        chatFragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().replace(R.id.container, chatFragment).commit();
        //        setTitle("Chat");
    }
}
