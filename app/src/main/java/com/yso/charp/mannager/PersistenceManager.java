package com.yso.charp.mannager;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.yso.charp.model.User;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;

public class PersistenceManager
{

    private static final String LOG_TAG = PersistenceManager.class.getSimpleName();

    private static final String PREF_LOGGED_IN = "pref.LOGGED_IN";
    private static final String PREF_ARRAY_LIST = "pref.ARRAY_LIST";

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

    public void setUsersMap(HashMap<String, User> hashMap)
    {
        SecurePreferences.getInstance().setString(PREF_ARRAY_LIST, mGson.toJson(hashMap));
    }

    public HashMap<String, User> getUsersMap()
    {
        String branchMapDataString = SecurePreferences.getInstance().getString(PREF_ARRAY_LIST, mGson.toJson(new ArrayList<>()));
        Type listType = new TypeToken<HashMap<String, User>>()
        {}.getType();

        return mGson.fromJson(branchMapDataString, listType);
    }
}
