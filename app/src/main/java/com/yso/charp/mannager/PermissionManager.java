package com.yso.charp.mannager;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.RequiresApi;

/**
 * Created by Admin on 02-Jan-18.
 */

public class PermissionManager
{
    public static final int PERMISSIONS_REQUEST_READ_CONTACTS = 101;
    public static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 102;
    private static String[] PERMISSIONS_CONTACT = {Manifest.permission.READ_CONTACTS};
    private static String[] PERMISSIONS_STORAGE = {Manifest.permission.READ_EXTERNAL_STORAGE};

    @RequiresApi (api = Build.VERSION_CODES.M)
    public static boolean checkStoragePermission(Activity activity)
    {
        if (activity.checkSelfPermission(PERMISSIONS_STORAGE[0]) > PackageManager.PERMISSION_GRANTED)
        {
            return true;
        }
        else
        {
            activity.requestPermissions(PERMISSIONS_STORAGE, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
            return false;
        }
    }

    @RequiresApi (api = Build.VERSION_CODES.M)
    public static boolean checkContactPermission(Activity activity)
    {
        if (activity.checkSelfPermission(PERMISSIONS_CONTACT[0]) > PackageManager.PERMISSION_GRANTED)
        {
            return true;
        }
        else
        {
            activity.requestPermissions(PERMISSIONS_CONTACT, PERMISSIONS_REQUEST_READ_CONTACTS);
            return false;
        }
    }
}
