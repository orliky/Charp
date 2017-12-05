package com.yso.charp.mannager.dataBase;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by orlik on 05/12/2017.
 */

public class DatabaseManager {
    private Integer mOpenCounter = 0;

    private static DatabaseManager instance;
    private static SQLiteOpenHelper mDatabaseHelper;
    private SQLiteDatabase mDatabase;

    public static synchronized void initializeInstance(SQLiteOpenHelper helper) {
        if (instance == null) {
            instance = new DatabaseManager();
            mDatabaseHelper = helper;
        }
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException(DatabaseManager.class.getSimpleName() +
                    " is not initialized, call initializeInstance(..) method first.");
        }

        return instance;
    }

    synchronized SQLiteDatabase openDatabase() {
        mOpenCounter+=1;
        if(mOpenCounter == 1) {
            // Opening new database
            mDatabase = mDatabaseHelper.getWritableDatabase();
        }
        return mDatabase;
    }

    synchronized void closeDatabase() {
        mOpenCounter-=1;
        if(mOpenCounter == 0) {
            // Closing database
            mDatabase.close();

        }
    }

    static int getIntByColumName(Cursor cursor, String tableColumn)
    {
        return cursor.getInt(cursor.getColumnIndex(tableColumn));
    }

    static long getLongByColumName(Cursor cursor, String tableColumn)
    {
        return cursor.getLong(cursor.getColumnIndex(tableColumn));
    }

    static double getDoubleByColumName(Cursor cursor, String tableColumn)
    {
        return cursor.getDouble(cursor.getColumnIndex(tableColumn));
    }

    static String getStringByColumName(Cursor cursor, String tableColumn)
    {
        return cursor.getString(cursor.getColumnIndex(tableColumn));
    }
}
