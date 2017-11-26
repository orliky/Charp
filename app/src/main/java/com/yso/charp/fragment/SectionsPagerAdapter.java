package com.yso.charp.fragment;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.yso.charp.NewVersion.RequestsFragment;

/**
 * Created by AkshayeJH on 11/06/17.
 */

public class SectionsPagerAdapter extends FragmentPagerAdapter
{


    public SectionsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {

        switch(position) {
            case 0:
                ChatListFragment chatListFragment = new ChatListFragment();
                return  chatListFragment;

            case 1:
                UserListFragment userListFragment = new UserListFragment();
                return userListFragment;

            default:
                return  null;
        }

    }

    @Override
    public int getCount() {
        return 2;
    }

    public CharSequence getPageTitle(int position){

        switch (position) {
            case 0:
                return "CHATS";

            case 1:
                return "USERS";

            default:
                return null;
        }

    }

}
