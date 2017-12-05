package com.yso.charp.mannager.dataBase;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.util.Log;

import com.yso.charp.model.User;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by orlik on 05/12/2017.
 */

public class UserRepo {
    private User user;

    public UserRepo() {

        user = new User();

    }

    public static String createTable(){
        return "CREATE TABLE IF NOT EXISTS " + User.TABLE + "("
                + User.KEY_PHONE + " TEXT NOT NULL,"
                + User.KEY_NAME + " TEXT NOT NULL,"
                + User.KEY_UID + " TEXT NOT NULL);";
    }

    public void insert(User user) {
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();

        ContentValues values = new ContentValues();
        values.put(User.KEY_PHONE, user.getPhone());
        values.put(User.KEY_NAME, user.getName());
        values.put(User.KEY_UID, user.getUID());

        db.insert(User.TABLE, null, values);
        DatabaseManager.getInstance().closeDatabase();
    }

    public void delete() {
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
        db.delete(User.TABLE,null,null);
        DatabaseManager.getInstance().closeDatabase();
    }

    public void deleteByPhone(String phone)
    {
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
        db.delete(User.TABLE, User.KEY_PHONE + " = ?", new String[]{phone});
        DatabaseManager.getInstance().closeDatabase();
    }

    public User getByPhone(String phone)
    {
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
//        " phone = ?"
        @SuppressLint("Recycle") Cursor cursor = db.query(User.TABLE, User.COLUMNS, User.KEY_PHONE + " = ?", new String[]{phone}, null, null, null, null);

        if (cursor.moveToFirst())
        {
            return getUser(cursor);
        }
        return null;
    }

    @NonNull
    private User getUser(Cursor cursor) {
        User user;
        user = new User();
        user.setPhone(DatabaseManager.getStringByColumName(cursor, User.KEY_PHONE));
        user.setName(DatabaseManager.getStringByColumName(cursor, User.KEY_NAME));
        user.setUID(DatabaseManager.getStringByColumName(cursor, User.KEY_UID));
        return user;
    }

    public List<User> getAll()
    {
        List<User> users = new LinkedList<>();

        String query = "SELECT  * FROM " + User.TABLE;
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
        @SuppressLint ("Recycle") Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst())
        {
            do
            {
                users.add(getUser(cursor));
            } while (cursor.moveToNext());
        }

        Log.d("getAllUsers()", users.toString());

        return users;
    }

    public int update(User user)
    {
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();

        ContentValues values = new ContentValues();
        values.put(User.KEY_NAME, user.getName());
        values.put(User.KEY_PHONE, user.getPhone());

        int i = db.update(User.TABLE, values, User.KEY_PHONE + " = ?", new String[]{user.getPhone()});

        DatabaseManager.getInstance().closeDatabase();

        return i;

    }
}
