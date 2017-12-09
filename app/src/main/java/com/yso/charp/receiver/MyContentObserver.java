package com.yso.charp.receiver;

import android.database.ContentObserver;

import com.google.firebase.database.ValueEventListener;
import com.yso.charp.mannager.FireBaseManager;

/**
 * Created by Admin on 30-Nov-17.
 */

public class MyContentObserver  extends ContentObserver
{
    private ValueEventListener mValueEventListener;
    public MyContentObserver(ValueEventListener valueEventListener) {
        super(null);
        mValueEventListener = valueEventListener;
    }

    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);
        FireBaseManager.updateClientUsers(mValueEventListener, true);
    }
}
