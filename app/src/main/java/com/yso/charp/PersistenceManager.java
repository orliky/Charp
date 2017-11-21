package com.yso.charp;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;

public class PersistenceManager
{

    private static final String LOG_TAG = PersistenceManager.class.getSimpleName();

    private static final String PREF_LOGGED_IN = "pref.LOGGED_IN";

    private static PersistenceManager msInstance;
    private Gson mGson;

    public static PersistenceManager initInstance(Context context)
    {
        if (msInstance == null)
        {
            msInstance = new PersistenceManager(context);
        }
        return msInstance;
    }

    public static PersistenceManager getInstance()
    {
        if (msInstance == null)
        {
            Log.e(LOG_TAG, "getInstance(), fail, PersistenceManager is not init");
        }
        return msInstance;
    }

    private PersistenceManager(Context context)
    {
        SecurePreferences.initInstance(context);
        mGson = new Gson();
    }

    public void setIsLoggedIn(boolean selected)
    {
        SecurePreferences.getInstance().setBoolean(PREF_LOGGED_IN, selected);
    }

    public boolean isLoggedIn()
    {
        return SecurePreferences.getInstance().getBoolean(PREF_LOGGED_IN, false);
    }
}
