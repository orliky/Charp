package com.yso.charp.fragment;


import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.yso.charp.R;
import com.yso.charp.activity.MainActivity;
import com.yso.charp.adapter.SectionsPagerAdapter;
import com.yso.charp.mannager.FireBaseManager;
import com.yso.charp.mannager.PermissionManager;
import com.yso.charp.mannager.PersistenceManager;
import com.yso.charp.model.User;
import com.yso.charp.receiver.MyContentObserver;
import com.yso.charp.service.FirebaseNotificationService;
import com.yso.charp.utils.ContactsUtils;

import java.util.HashMap;

import static com.yso.charp.mannager.FireBaseManager.FB_CHILD_CLIENT_USERS;
import static com.yso.charp.mannager.FireBaseManager.getFirebaseUserPhone;

/**
 * A simple {@link Fragment} subclass.
 */
public class MainFragment extends Fragment
{

    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    //    private AppBarLayout mAppBarLayout;
    private ValueEventListener mValueEventListener;
    private FloatingActionButton myFab;


    public MainFragment()
    {

    }


    @RequiresApi (api = Build.VERSION_CODES.M)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        Toolbar toolbar = view.findViewById(R.id.my_toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

        initValueEventListener();
        FireBaseManager.updateClientUsers(mValueEventListener, false);

        getActivity().startService(new Intent(getActivity(), FirebaseNotificationService.class));

        MyContentObserver contentObserver = new MyContentObserver(mValueEventListener);
        getActivity().getApplicationContext().getContentResolver().registerContentObserver(ContactsContract.Contacts.CONTENT_URI, true, contentObserver);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        //        mAppBarLayout = view.findViewById(R.id.appBarLayout);

        mViewPager = view.findViewById(R.id.main_tabPager);
        mSectionsPagerAdapter = new SectionsPagerAdapter(getActivity().getSupportFragmentManager());

        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
            {

            }

            @Override
            public void onPageSelected(int position)
            {
                if (position == 0)
                {
                    myFab.setImageResource(R.drawable.sym_action_add);
                }
                else
                {
                    myFab.setImageResource(R.drawable.ic_menu_back);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state)
            {

            }
        });

        mTabLayout = view.findViewById(R.id.main_tabs);
        mTabLayout.setupWithViewPager(mViewPager);

        myFab = view.findViewById(R.id.fab);
        myFab.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                slidePager();
            }
        });
    }

    private void slidePager()
    {
        if (mViewPager.getCurrentItem() == 0)
        {
            mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1);
        }
        else
        {
            mViewPager.setCurrentItem(mViewPager.getCurrentItem() - 1);
        }
    }

    private void initValueEventListener()
    {
        mValueEventListener = new ValueEventListener()
        {
            final HashMap<String, User> clientUsers = new HashMap<>();

            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                for (DataSnapshot snapshot : dataSnapshot.getChildren())
                {
                    User user = snapshot.getValue(User.class);
                    assert user != null;
                    if (!user.getPhone().equals(getFirebaseUserPhone()))
                    {
                        if (ContactsUtils.getContactNumber(user.getPhone()) != null && !ContactsUtils.getContactNumber(user.getPhone()).equals(""))
                        {
                            clientUsers.put(user.getPhone(), user);
                        }
                    }
                }
                FirebaseDatabase.getInstance().getReference().child(FB_CHILD_CLIENT_USERS).child(getFirebaseUserPhone()).setValue(clientUsers);
                PersistenceManager.getInstance().setUsersMap(clientUsers);
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        };
    }
}
