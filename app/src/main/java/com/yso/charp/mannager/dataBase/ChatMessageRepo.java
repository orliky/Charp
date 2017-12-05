package com.yso.charp.mannager.dataBase;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.yso.charp.model.ChatMessage;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by orlik on 05/12/2017.
 */

public class ChatMessageRepo {

    private ChatMessage chatMessage;

    public ChatMessageRepo() {

        chatMessage = new ChatMessage();

    }

    public static String createTable(){
        return "CREATE TABLE IF NOT EXISTS " + ChatMessage.TABLE + "("
                + ChatMessage.KEY_ID + " TEXT NOT NULL,"
                + ChatMessage.KEY_FROM + " TEXT NOT NULL,"
                + ChatMessage.KEY_PHONE + " TEXT NOT NULL,"
                + ChatMessage.KEY_TEXT + " TEXT,"
                + ChatMessage.KEY_TIME + " INTEGER,"
                + ChatMessage.KEY_BASE64 + " TEXT);";
    }

    public void insert(String id, String from, ChatMessage chatMessage) {
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();

        ContentValues values = new ContentValues();
        values.put(ChatMessage.KEY_ID, id);
        values.put(ChatMessage.KEY_FROM, from);
        values.put(ChatMessage.KEY_PHONE, chatMessage.getMessageUser());
        values.put(ChatMessage.KEY_TEXT, chatMessage.getMessageText());
        values.put(ChatMessage.KEY_TIME, String.valueOf(chatMessage.getMessageTime()));
        values.put(ChatMessage.KEY_BASE64, chatMessage.getBase64Image());

        db.insert(ChatMessage.TABLE, null, values);
        DatabaseManager.getInstance().closeDatabase();
    }

    public void delete() {
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
        db.delete(ChatMessage.TABLE,null,null);
        DatabaseManager.getInstance().closeDatabase();
    }

    public ChatMessage getById(String id)
    {
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();

        @SuppressLint("Recycle") Cursor cursor = db.query(ChatMessage.TABLE, ChatMessage.COLUMNS, " id = ?", new String[]{id}, null, null, null, null);

        ChatMessage chatMessage = null;
        if (cursor.moveToFirst())
        {
            chatMessage = new ChatMessage();
            chatMessage.setMessageUser(DatabaseManager.getStringByColumName(cursor, ChatMessage.KEY_PHONE));
            chatMessage.setMessageText(DatabaseManager.getStringByColumName(cursor, ChatMessage.KEY_TEXT));
            chatMessage.setMessageTime(DatabaseManager.getLongByColumName(cursor, ChatMessage.KEY_TIME));
            chatMessage.setBase64Image(DatabaseManager.getStringByColumName(cursor, ChatMessage.KEY_BASE64));
        }
        return chatMessage;
    }

    public List<ChatMessage> getAll()
    {
        List<ChatMessage> chatMessages = new LinkedList<>();

        String query = "SELECT  * FROM " + ChatMessage.TABLE;
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
        @SuppressLint ("Recycle") Cursor cursor = db.rawQuery(query, null);

        ChatMessage chatMessage  = null;
        if (cursor.moveToFirst())
        {
            do
            {
                chatMessage = new ChatMessage();
                chatMessage.setMessageUser(DatabaseManager.getStringByColumName(cursor, ChatMessage.KEY_PHONE));
                chatMessage.setMessageText(DatabaseManager.getStringByColumName(cursor, ChatMessage.KEY_TEXT));
                chatMessage.setMessageTime(DatabaseManager.getLongByColumName(cursor, ChatMessage.KEY_TIME));
                chatMessage.setBase64Image(DatabaseManager.getStringByColumName(cursor, ChatMessage.KEY_BASE64));

                chatMessages.add(chatMessage);
            } while (cursor.moveToNext());
        }
        return chatMessages;
    }
}
