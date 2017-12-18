package com.yso.charp.fragment;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.yso.charp.R;
import com.yso.charp.adapter.SectionsPagerAdapter;

/**
 * A simple {@link Fragment} subclass.
 */
public class MainFragment extends Fragment
{

    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private AppBarLayout mAppBarLayout;

    public MainFragment()
    {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        Toolbar toolbar = view.findViewById(R.id.my_toolbar);
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        mAppBarLayout = (AppBarLayout) view.findViewById(R.id.appBarLayout);
        //Tabs
        mViewPager = (ViewPager) view.findViewById(R.id.main_tabPager);
        mSectionsPagerAdapter = new SectionsPagerAdapter(getActivity().getSupportFragmentManager());

        mViewPager.setAdapter(mSectionsPagerAdapter);

        mTabLayout = (TabLayout) view.findViewById(R.id.main_tabs);
        mTabLayout.setupWithViewPager(mViewPager);
    }
}
