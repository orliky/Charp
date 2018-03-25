package com.yso.charp.mannager.dataBase;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import com.yso.charp.model.ChatMessage;

import java.util.Collections;
import java.util.Comparator;
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

    static String createTable() {
        return "CREATE TABLE IF NOT EXISTS " + ChatMessage.TABLE + "("
                + ChatMessage.KEY_ID + " TEXT NOT NULL,"
                + ChatMessage.KEY_TO + " TEXT NOT NULL,"
                + ChatMessage.KEY_PHONE + " TEXT NOT NULL,"
                + ChatMessage.KEY_TEXT + " TEXT,"
                + ChatMessage.KEY_TIME + " INTEGER,"
                + ChatMessage.KEY_BASE64 + " TEXT);";
    }

    public void insert(String id, String other, ChatMessage chatMessage) {
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();

        ContentValues values = new ContentValues();
        values.put(ChatMessage.KEY_ID, id);
        values.put(ChatMessage.KEY_TO, other);
        values.put(ChatMessage.KEY_PHONE, chatMessage.getMessageUser());
        values.put(ChatMessage.KEY_TEXT, chatMessage.getMessageText());
        values.put(ChatMessage.KEY_TIME, String.valueOf(chatMessage.getMessageTime()));
        values.put(ChatMessage.KEY_BASE64, chatMessage.getBase64Image());

        db.insert(ChatMessage.TABLE, null, values);
        DatabaseManager.getInstance().closeDatabase();
    }

    public void delete() {
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
        db.delete(ChatMessage.TABLE, null, null);
        DatabaseManager.getInstance().closeDatabase();
    }

    public ChatMessage getById(String id) {
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();

        @SuppressLint("Recycle") Cursor cursor = db.query(ChatMessage.TABLE, ChatMessage.COLUMNS, " " + ChatMessage.KEY_ID + " =? ", new String[]{id}, null, null, null, null);

        ChatMessage chatMessage = null;
        if (cursor.moveToFirst()) {
            chatMessage = new ChatMessage();
            chatMessage.setMessageUser(DatabaseManager.getStringByColumName(cursor, ChatMessage.KEY_PHONE));
            chatMessage.setMessageText(DatabaseManager.getStringByColumName(cursor, ChatMessage.KEY_TEXT));
            chatMessage.setMessageTime(DatabaseManager.getLongByColumName(cursor, ChatMessage.KEY_TIME));
            chatMessage.setBase64Image(DatabaseManager.getStringByColumName(cursor, ChatMessage.KEY_BASE64));
        }
        return chatMessage;
    }

    public List<ChatMessage> getAll() {
        List<ChatMessage> chatMessages = new LinkedList<>();

        String query = "SELECT  * FROM " + ChatMessage.TABLE;
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
        @SuppressLint("Recycle") Cursor cursor = db.rawQuery(query, null);

        ChatMessage chatMessage = null;
        if (cursor.moveToFirst()) {
            do {
                chatMessage = new ChatMessage();
                chatMessage.setMessageUser(DatabaseManager.getStringByColumName(cursor, ChatMessage.KEY_PHONE));
                chatMessage.setMessageText(DatabaseManager.getStringByColumName(cursor, ChatMessage.KEY_TEXT));
                chatMessage.setMessageTime(DatabaseManager.getLongByColumName(cursor, ChatMessage.KEY_TIME));
                chatMessage.setBase64Image(DatabaseManager.getStringByColumName(cursor, ChatMessage.KEY_BASE64));
                getImage(chatMessage);

                chatMessages.add(chatMessage);
            } while (cursor.moveToNext());
        }

        Collections.sort(chatMessages, new Comparator<ChatMessage>()
        {
            @Override
            public int compare(ChatMessage s1, ChatMessage s2)
            {
                return Long.compare(s1.getMessageTime(), s2.getMessageTime());
            }
        });

        return chatMessages;
    }

    public List<ChatMessage> getByChat(String currentUser, String otherUser) {
        List<ChatMessage> chatMessages = new LinkedList<>();
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
        Cursor cursor = db.query(ChatMessage.TABLE,
                ChatMessage.COLUMNS,
                " " + ChatMessage.KEY_PHONE + " =?" + " AND " + ChatMessage.KEY_TO + " =? ",
                new String[]{currentUser, otherUser},
                null, null, null);

        ChatMessage chatMessage = null;
        if (cursor.moveToFirst()) {
            do {
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

    public List<ChatMessage> getAllByChat(String currentUser, String otherUser)
    {
        List<ChatMessage> chatMessages = new LinkedList<>();
        chatMessages.addAll(getByChat(currentUser, otherUser));
        chatMessages.addAll(getByChat(otherUser, currentUser));
        for (ChatMessage chatMessage : chatMessages)
        {
            getImage(chatMessage);
        }
        Collections.sort(chatMessages, new Comparator<ChatMessage>()
        {
            @Override
            public int compare(ChatMessage s1, ChatMessage s2)
            {
                return Long.compare(s1.getMessageTime(), s2.getMessageTime());
            }
        });
        return chatMessages;
    }

    private void getImage(ChatMessage chatMessage)
    {
        if (chatMessage.getBase64Image() != null && !chatMessage.getBase64Image().equals(""))
        {
            byte[] imageBytes = Base64.decode(chatMessage.getBase64Image(), Base64.DEFAULT);
            Bitmap decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);

            chatMessage.setBitmap(decodedImage);
        }
    }
}
