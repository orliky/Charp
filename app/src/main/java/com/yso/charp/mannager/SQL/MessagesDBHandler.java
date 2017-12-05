package com.yso.charp.mannager.SQL;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.yso.charp.model.ChatMessage;
import com.yso.charp.model.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by orlik on 03/12/2017.
 */

public class MessagesDBHandler extends SQLiteOpenHelper
{
    private static final int DATABASE_VERSION = 1;

//    private static final String TABLE_LIST = "messages";
//    private static final String TABLE_LIST_PHONE = "phone";

    private static final String TABLE_CHILD_LIST = "message";
    private static final String TABLE_CHILD_LIST_ID = "id";
    private static final String TABLE_CHILD_LIST_FROM = "fromUser";
    private static final String TABLE_CHILD_LIST_PHONE = "phone";
    private static final String TABLE_CHILD_LIST_TEXT = "messageText";
    private static final String TABLE_CHILD_LIST_TIME = "messageTime";
    private static final String TABLE_CHILD_LIST_BASE64 = "mBase64Image";

    private static final String[] COLUMNS = {TABLE_CHILD_LIST_ID, TABLE_CHILD_LIST_FROM, TABLE_CHILD_LIST_PHONE, TABLE_CHILD_LIST_TEXT, TABLE_CHILD_LIST_TIME, TABLE_CHILD_LIST_BASE64};

    public MessagesDBHandler(Context context)
    {
        super(context, TABLE_CHILD_LIST, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {

//        String CREATE_MAIN_LIST_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_LIST + "("
//                + TABLE_LIST_PHONE + " TEXT NOT NULL)";

        String CREATE_TABLE_CHILD_LIST = "CREATE TABLE IF NOT EXISTS " + TABLE_CHILD_LIST + "("
                + TABLE_CHILD_LIST_ID + " TEXT NOT NULL,"
                + TABLE_CHILD_LIST_FROM + " TEXT NOT NULL,"
                + TABLE_CHILD_LIST_PHONE + " TEXT NOT NULL,"
                + TABLE_CHILD_LIST_TEXT + " TEXT NOT NULL,"
                + TABLE_CHILD_LIST_TIME + " INTEGER,"
                + TABLE_CHILD_LIST_BASE64 + " TEXT NOT NULL);";

//        db.execSQL(CREATE_MAIN_LIST_TABLE);
        db.execSQL(CREATE_TABLE_CHILD_LIST);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
//        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LIST);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CHILD_LIST);

        onCreate(db);
    }


//    public void addListItem(String parentPhone)
//    {
//        Log.d("addListItem", parentPhone);
//
//        SQLiteDatabase db = this.getWritableDatabase();
//
//        ContentValues values = new ContentValues();
//        values.put(TABLE_LIST_PHONE, parentPhone);
//
//        db.insert(TABLE_LIST, null, values);
//        db.close();
//    }

    public void addChildListItem(String id, String from, ChatMessage chatMessage)
    {
        Log.d("addChildListItem", chatMessage.getMessageText());

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(TABLE_CHILD_LIST_ID, id);
        values.put(TABLE_CHILD_LIST_FROM, from);
        values.put(TABLE_CHILD_LIST_PHONE, chatMessage.getMessageUser());
        values.put(TABLE_CHILD_LIST_TEXT, chatMessage.getMessageText());
        values.put(TABLE_CHILD_LIST_TIME, String.valueOf(chatMessage.getMessageTime()));
        values.put(TABLE_CHILD_LIST_BASE64, chatMessage.getBase64Image());

        db.insert(TABLE_CHILD_LIST, null, values);
        db.close();
    }

    public ChatMessage getChatMessage(String id)
    {
        SQLiteDatabase db = this.getReadableDatabase();

        @SuppressLint ("Recycle") Cursor cursor = db.query(TABLE_CHILD_LIST, COLUMNS, " id = ?", new String[]{id}, null, null, null, null);

        ChatMessage chatMessage = null;
        if (cursor.moveToFirst())
        {
            chatMessage = new ChatMessage();
            assert cursor != null;
            chatMessage.setMessageUser(getStringByColumName(cursor, TABLE_CHILD_LIST_PHONE));
            chatMessage.setMessageText(getStringByColumName(cursor, TABLE_CHILD_LIST_TEXT));
            chatMessage.setMessageTime(getLongByColumName(cursor, TABLE_CHILD_LIST_TIME));
            chatMessage.setBase64Image(getStringByColumName(cursor, TABLE_CHILD_LIST_BASE64));

            Log.d("getChatMessage(" + id + ")", chatMessage.toString());
        }
        return chatMessage;
    }

//    public void deleteParentItem(String parentPhone)
//    {
//        SQLiteDatabase db = this.getWritableDatabase();
//        db.delete(TABLE_LIST, TABLE_LIST_PHONE + " = ?", new String[]{parentPhone});
//        db.close();
//    }

    public void deleteChildItem(String phone)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CHILD_LIST, TABLE_CHILD_LIST_PHONE + " = ?", new String[]{phone});
        db.close();
    }

    public List<ChatMessage> getAllChatList()
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_CHILD_LIST, null, null, null, null, null, null);

        List<ChatMessage> result = null;
        if (cursor.moveToFirst())
        {
            do
            {
                result = new ArrayList<>();
                ChatMessage chatMessage = new ChatMessage();
                chatMessage.setMessageUser(getStringByColumName(cursor, TABLE_CHILD_LIST_PHONE));
                chatMessage.setMessageText(getStringByColumName(cursor, TABLE_CHILD_LIST_TEXT));
                chatMessage.setMessageTime(getLongByColumName(cursor, TABLE_CHILD_LIST_TIME));
                chatMessage.setBase64Image(getStringByColumName(cursor, TABLE_CHILD_LIST_BASE64));

                result.add(chatMessage);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return result;
    }

    private int getIntByColumName(Cursor cursor, String tableColumn)
    {
        return cursor.getInt(cursor.getColumnIndex(tableColumn));
    }

    private long getLongByColumName(Cursor cursor, String tableColumn)
    {
        return cursor.getLong(cursor.getColumnIndex(tableColumn));
    }

    private double getDoubleByColumName(Cursor cursor, String tableColumn)
    {
        return cursor.getDouble(cursor.getColumnIndex(tableColumn));
    }

    private String getStringByColumName(Cursor cursor, String tableColumn)
    {
        return cursor.getString(cursor.getColumnIndex(tableColumn));
    }
}
