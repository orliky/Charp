package com.yso.charp;

import android.app.Application;
import android.content.Context;

import com.yso.charp.mannager.PersistenceManager;
import com.yso.charp.mannager.dataBase.DBHelper;
import com.yso.charp.mannager.dataBase.DatabaseManager;


/**
 * Created by Admin on 16-Oct-17.
 */

public class CharpApplication extends Application
{

    private static Context msApplicationContext;

    @Override
    public void onCreate()
    {
        super.onCreate();

        msApplicationContext = getApplicationContext();
        PersistenceManager.initInstance(msApplicationContext);
        DatabaseManager.initializeInstance(new DBHelper());
    }

    public static Context getAppContext()
    {
        return msApplicationContext;
    }
}
