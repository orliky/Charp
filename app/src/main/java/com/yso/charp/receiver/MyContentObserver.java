package com.yso.charp.receiver;

import android.database.ContentObserver;

import com.yso.charp.mannager.FireBaseManager;

/**
 * Created by Admin on 30-Nov-17.
 */

public class MyContentObserver  extends ContentObserver
{
    public MyContentObserver() {
        super(null);
    }

    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);
        FireBaseManager.updateClientUsers();
    }
}
