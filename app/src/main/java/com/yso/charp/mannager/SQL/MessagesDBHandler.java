package com.yso.charp.mannager.SQL;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.yso.charp.model.ChatMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by orlik on 03/12/2017.
 */

public class MessagesDBHandler extends SQLiteOpenHelper {


    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "messages.db"; // just use a name.


    public MessagesDBHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    private final String TABLE_LIST = "messages";
    private final String TABLE_LIST_PHONE = "phone";
//    private final String TABLE_LIST_NAME = "name";

    private final String TABLE_CHILD_LIST = "message";
    private final String TABLE_CHILD_LIST_PHONE = "phone";
    private final String TABLE_CHILD_LIST_TEXT = "messageText";
    private final String TABLE_CHILD_LIST_TIME = "messageTime";
    private final String TABLE_CHILD_LIST_BASE64 = "mBase64Image";

    @Override
    public void onCreate(SQLiteDatabase db) {

        String CREATE_MAIN_LIST_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_LIST + "("
                + TABLE_LIST_PHONE + " TEXT)";

        String CREATE_TABLE_CHILD_LIST = "CREATE TABLE IF NOT EXISTS " + TABLE_CHILD_LIST + "("
                + TABLE_CHILD_LIST_PHONE + " TEXT,"
                + TABLE_CHILD_LIST_TEXT + " TEXT,"
                + TABLE_CHILD_LIST_TIME + " INTEGER,"
                + TABLE_CHILD_LIST_BASE64 + " TEXT);";

        db.execSQL(CREATE_MAIN_LIST_TABLE);
        db.execSQL(CREATE_TABLE_CHILD_LIST);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LIST);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CHILD_LIST);

        onCreate(db);
    }


    public void addListItem(String parentPhone) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TABLE_LIST_PHONE, parentPhone);
        db.insert(TABLE_LIST, null, values);
        db.close();
    }

    public void addChildListItem(ChatMessage chatMessage) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TABLE_CHILD_LIST_PHONE, chatMessage.getMessageUser());
        values.put(TABLE_CHILD_LIST_TEXT, chatMessage.getMessageText());
        values.put(TABLE_CHILD_LIST_TIME, String.valueOf(chatMessage.getMessageTime()));
        values.put(TABLE_CHILD_LIST_BASE64, chatMessage.getBase64Image());
        db.insert(TABLE_CHILD_LIST, null, values);
        db.close();
    }

    public void deleteParentItem(String parentPhone) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_LIST, TABLE_LIST_PHONE + " = ?", new String[] { parentPhone });
        db.close();
    }

    public void deleteChildItem(String phone) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CHILD_LIST, TABLE_CHILD_LIST_PHONE + " = ?", new String[] { phone });
        db.close();
    }

    public List<ChatMessage> getAllParentListItem() {
        List<ChatMessage> result = null;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_LIST, null, null, null, null, null, null);

        while (cursor.moveToNext()) {
            result = new ArrayList<>();
            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setMessageUser(getStringByColumName(cursor, TABLE_CHILD_LIST_PHONE));
            chatMessage.setMessageText(getStringByColumName(cursor, TABLE_CHILD_LIST_TEXT));
            chatMessage.setMessageTime(getLongByColumName(cursor, TABLE_CHILD_LIST_TIME));
            chatMessage.setBase64Image(getStringByColumName(cursor, TABLE_CHILD_LIST_BASE64));
            result.add(chatMessage);
        }
        cursor.close();
        db.close();
        return result;
    }

    private int getIntByColumName(Cursor cursor, String tableColumn) {
        return cursor.getInt(cursor.getColumnIndex(tableColumn));
    }

    private long getLongByColumName(Cursor cursor, String tableColumn) {
        return cursor.getLong(cursor.getColumnIndex(tableColumn));
    }

    private double getDoubleByColumName(Cursor cursor, String tableColumn) {
        return cursor.getDouble(cursor.getColumnIndex(tableColumn));
    }


    private String getStringByColumName(Cursor cursor, String tableColumn) {
        return cursor.getString(cursor.getColumnIndex(tableColumn));
    }
}
