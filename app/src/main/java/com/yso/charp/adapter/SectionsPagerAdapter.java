package com.yso.charp.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import com.yso.charp.fragment.ChatListFragment;
import com.yso.charp.fragment.UserListFragment;

public class SectionsPagerAdapter extends FragmentPagerAdapter
{

    public SectionsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {

        switch(position) {
            case 0:
                return ChatListFragment.getInstance();

            case 1:
                return UserListFragment.getInstance();

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
                return "צ'אטים";

            case 1:
                return "אנשי קשר";

            default:
                return null;
        }
    }
}
