package com.yso.charp.mannager.SQL;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.yso.charp.model.ChatMessage;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by orlik on 03/12/2017.
 */

public class MessagesDBHandler extends SQLiteOpenHelper
{
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_MESSAGES = "messages";
    private static final String TABLE_CHILD_LIST_ID = "id";
    private static final String TABLE_CHILD_LIST_FROM = "fromUser";
    private static final String TABLE_CHILD_LIST_PHONE = "phone";
    private static final String TABLE_CHILD_LIST_TEXT = "messageText";
    private static final String TABLE_CHILD_LIST_TIME = "messageTime";
    private static final String TABLE_CHILD_LIST_BASE64 = "mBase64Image";

    private static final String[] COLUMNS = {TABLE_CHILD_LIST_ID, TABLE_CHILD_LIST_FROM, TABLE_CHILD_LIST_PHONE, TABLE_CHILD_LIST_TEXT, TABLE_CHILD_LIST_TIME, TABLE_CHILD_LIST_BASE64};

    public MessagesDBHandler(Context context)
    {
        super(context, TABLE_MESSAGES, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        String CREATE_MESSAGES_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_MESSAGES + "("
                + TABLE_CHILD_LIST_ID + " TEXT NOT NULL,"
                + TABLE_CHILD_LIST_FROM + " TEXT NOT NULL,"
                + TABLE_CHILD_LIST_PHONE + " TEXT NOT NULL,"
                + TABLE_CHILD_LIST_TEXT + " TEXT,"
                + TABLE_CHILD_LIST_TIME + " INTEGER,"
                + TABLE_CHILD_LIST_BASE64 + " TEXT);";

        db.execSQL(CREATE_MESSAGES_TABLE);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MESSAGES);

        this.onCreate(db);
    }

    public void addChatMessage(String id, String from, ChatMessage chatMessage)
    {
        Log.d("addChatMessage", chatMessage.getMessageText());

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(TABLE_CHILD_LIST_ID, id);
        values.put(TABLE_CHILD_LIST_FROM, from);
        values.put(TABLE_CHILD_LIST_PHONE, chatMessage.getMessageUser());
        values.put(TABLE_CHILD_LIST_TEXT, chatMessage.getMessageText());
        values.put(TABLE_CHILD_LIST_TIME, String.valueOf(chatMessage.getMessageTime()));
        values.put(TABLE_CHILD_LIST_BASE64, chatMessage.getBase64Image());

        db.insert(TABLE_MESSAGES, null, values);
        db.close();
    }

    public ChatMessage getChatMessage(String id)
    {
        SQLiteDatabase db = this.getReadableDatabase();

        @SuppressLint ("Recycle") Cursor cursor = db.query(TABLE_MESSAGES, COLUMNS, " id = ?", new String[]{id}, null, null, null, null);

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

    public void deleteChildItem(String phone)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_MESSAGES, TABLE_CHILD_LIST_PHONE + " = ?", new String[]{phone});
        db.close();
    }

    public List<ChatMessage> getAllChatList()
    {
        List<ChatMessage> chatMessages = new LinkedList<>();

        String query = "SELECT  * FROM " + TABLE_MESSAGES;
        SQLiteDatabase db = this.getWritableDatabase();
        @SuppressLint ("Recycle") Cursor cursor = db.rawQuery(query, null);

        ChatMessage chatMessage  = null;
        if (cursor.moveToFirst())
        {
            do
            {
                chatMessage = new ChatMessage();
                chatMessage.setMessageUser(getStringByColumName(cursor, TABLE_CHILD_LIST_PHONE));
                chatMessage.setMessageText(getStringByColumName(cursor, TABLE_CHILD_LIST_TEXT));
                chatMessage.setMessageTime(getLongByColumName(cursor, TABLE_CHILD_LIST_TIME));
                chatMessage.setBase64Image(getStringByColumName(cursor, TABLE_CHILD_LIST_BASE64));

                chatMessages.add(chatMessage);
            } while (cursor.moveToNext());
        }
        return chatMessages;
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
