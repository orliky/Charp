package com.yso.charp.activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseUser;
import com.yso.charp.Interface.SignOutListener;
import com.yso.charp.R;
import com.yso.charp.fragment.ChatFragment;
import com.yso.charp.fragment.ChatListFragment;
import com.yso.charp.fragment.MainFragment;
import com.yso.charp.fragment.MyDetailsFragment;
import com.yso.charp.fragment.PhoneAuthFragment;
import com.yso.charp.fragment.UserListFragment;
import com.yso.charp.mannager.FireBaseManager;
import com.yso.charp.mannager.PermissionManager;

import static com.yso.charp.mannager.FireBaseManager.FB_CHILD_CLIENT_USERS;
import static com.yso.charp.mannager.FireBaseManager.FB_CHILD_MESSAGES;
import static com.yso.charp.mannager.FireBaseManager.getDatabaseReferencem;
import static com.yso.charp.mannager.FireBaseManager.getFirebaseUserPhone;
import static com.yso.charp.mannager.PermissionManager.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE;
import static com.yso.charp.mannager.PermissionManager.PERMISSIONS_REQUEST_READ_CONTACTS;

public class MainActivity extends AppCompatActivity
{
    public static final String MY_FRAGMENT = "MY_FRAGMENT";
    private static String TAG = MainActivity.class.getSimpleName();

    private FirebaseUser mFirebaseUser;
    private Toolbar mToolbar;

    @RequiresApi (api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        mToolbar = findViewById(R.id.my_toolbar);
//        setSupportActionBar(mToolbar);

        // Restore instance state
        if (savedInstanceState != null)
        {
            onRestoreInstanceState(savedInstanceState);
        }

        mFirebaseUser = FireBaseManager.getFirebaseUser();

        if (PermissionManager.checkContactPermission(MainActivity.this))
        {
            goToFirstFragment();
        }
    }

    public void goToFirstFragment()
    {
        if (mFirebaseUser == null)
        {
            /*startActivity(new Intent(this, PhoneAuthActivity.class));
            finish();*/
//            mToolbar.setSubtitle("כניסה");
            getSupportFragmentManager().beginTransaction().add(R.id.container, new PhoneAuthFragment()).commit();
        }
        else
        {
            getSupportFragmentManager().beginTransaction().add(R.id.container, new MainFragment()).commit();
//            mToolbar.setSubtitle("צ'אטים");
//            getSupportFragmentManager().beginTransaction().add(R.id.container, ChatListFragment.getInstance()).commit();
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

        if (item.getItemId() == R.id.menu_account)
        {
            android.support.v4.app.FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right);
            ft.add(R.id.container, new MyDetailsFragment(), MY_FRAGMENT).commit();
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
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    Log.d(TAG, "permission granted");
                    PermissionManager.checkStoragePermission(MainActivity.this);
                }
                else
                {
                    Log.d(TAG, "permission denied");
                    Snackbar.make(findViewById(android.R.id.content), "ללא אישור גישה האפליקציה לא תפעל כראוי", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            finish();
                        }
                    }, 1000);
                }
                break;

            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    Log.d(TAG, "permission granted");
                }
                else
                {
                    Log.d(TAG, "permission denied");
                }
                goToFirstFragment();
                break;
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        ChatListFragment.getInstance().refreshAdapter();
        UserListFragment.getInstance().refreshAdapter();
    }

    @Override
    public void onBackPressed()
    {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.chat_container);
        if (fragment != null && !fragment.isHidden())
        {
            bacToFragment("");
        }
        else
        {
            super.onBackPressed();
        }
    }

    private void bacToFragment(String title)
    {
        android.support.v4.app.FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left);
        ft.hide(getSupportFragmentManager().findFragmentById(R.id.chat_container)).commit();
//        mToolbar.setSubtitle(title);
    }

    public void goToFragment(Fragment fragment, String title)
    {
        android.support.v4.app.FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right);
        ft.show(fragment).commit();
//        mToolbar.setSubtitle(title);
    }
}