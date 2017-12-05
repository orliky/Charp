package com.yso.charp.mannager.dataBase;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.yso.charp.CharpApplication;
import com.yso.charp.model.ChatMessage;
import com.yso.charp.model.User;

public class DBHelper extends SQLiteOpenHelper
{

    //version number.
    private static final int DATABASE_VERSION = 1;
    // Database Name
    private static final String DATABASE_NAME = "charp.db";
    private static final String TAG = DBHelper.class.getSimpleName();

    public DBHelper( ) {
        super(CharpApplication.getAppContext(), DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //All necessary tables you like to create will create here
        db.execSQL(ChatMessageRepo.createTable());
        db.execSQL(UserRepo.createTable());
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, String.format("SQLiteDatabase.onUpgrade(%d -> %d)", oldVersion, newVersion));

        // Drop table if existed, all data will be gone!!!
        db.execSQL("DROP TABLE IF EXISTS " + ChatMessage.TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + User.TABLE);
        onCreate(db);
    }
}
