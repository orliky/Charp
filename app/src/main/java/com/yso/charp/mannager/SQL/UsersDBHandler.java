package com.yso.charp.mannager.SQL;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.yso.charp.model.User;

import java.util.LinkedList;
import java.util.List;

public class UsersDBHandler extends SQLiteOpenHelper
{

    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_USERS = "users";
    private static final String KEY_PHONE = "phone";
    private static final String KEY_NAME = "name";
    private static final String KEY_UID = "uid";

    private static final String[] COLUMNS = {KEY_PHONE, KEY_NAME, KEY_UID};

    public UsersDBHandler(Context context)
    {
        super(context, TABLE_USERS, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        String CREATE_CLIENT_USERS_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_USERS + "("
                + KEY_PHONE + " TEXT NOT NULL,"
                + KEY_NAME + " TEXT NOT NULL,"
                + KEY_UID + " TEXT NOT NULL);";

        db.execSQL(CREATE_CLIENT_USERS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);

        this.onCreate(db);
    }


    public void addUser(User user)
    {
        Log.d("addUser", user.toString());

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_PHONE, user.getPhone());
        values.put(KEY_NAME, user.getName());
        values.put(KEY_UID, user.getUID());

        db.insert(TABLE_USERS, null, values);
        db.close();
    }

    public User getUser(String phone)
    {
        SQLiteDatabase db = this.getReadableDatabase();

        @SuppressLint ("Recycle") Cursor cursor = db.query(TABLE_USERS, COLUMNS, " phone = ?", new String[]{phone}, null, null, null, null);

        User user = null;
        if (cursor.moveToFirst())
        {
            user = new User();
            assert cursor != null;
            user.setPhone(cursor.getString(0));
            user.setName(cursor.getString(1));
            user.setUID(cursor.getString(2));

            Log.d("getUser(" + phone + ")", user.toString());
        }
        else
        {
            Log.d("getUser(" + phone + ")", "cursor.moveToFirst() = " + cursor.moveToFirst());
        }

        return user;
    }

    public List<User> getAllUsers()
    {
        List<User> users = new LinkedList<>();

        String query = "SELECT  * FROM " + TABLE_USERS;
        SQLiteDatabase db = this.getWritableDatabase();
        @SuppressLint ("Recycle") Cursor cursor = db.rawQuery(query, null);

        User user = null;
        if (cursor.moveToFirst())
        {
            do
            {
                user = new User();
                user.setPhone(cursor.getString(0));
                user.setName(cursor.getString(1));
                user.setUID(cursor.getString(2));

                users.add(user);
            } while (cursor.moveToNext());
        }

        Log.d("getAllUsers()", users.toString());

        return users;
    }

    public int updateUser(User user)
    {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("name", user.getName());
        values.put("phone", user.getPhone());

        int i = db.update(TABLE_USERS, values, KEY_PHONE + " = ?", new String[]{user.getPhone()});

        db.close();

        return i;

    }

    public void deleteUser(User user)
    {

        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(TABLE_USERS, KEY_PHONE + " = ?", new String[]{user.getPhone()});

        db.close();

        Log.d("deleteUser", user.toString());

    }

    public void deleteAllUsers()
    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_USERS);
        db.close();
    }
}
