package com.yso.charp.model;

import android.graphics.Bitmap;

import java.util.Date;

/**
 * Created by Admin on 16-Nov-17.
 */

public class ChatMessage {

    private String messageText;
    private String messageUser;
    private long messageTime;
    private String mBase64Image;
    private Bitmap mBitmap;

    public ChatMessage(String messageText, String messageUser) {
        this.messageText = messageText;
        this.messageUser = messageUser;

        // Initialize to current time
        messageTime = new Date().getTime();
    }

    public ChatMessage(){

    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public String getMessageUser() {
        return messageUser;
    }

    public void setMessageUser(String messageUser) {
        this.messageUser = messageUser;
    }

    public long getMessageTime() {
        return messageTime;
    }

    public void setMessageTime(long messageTime) {
        this.messageTime = messageTime;
    }

    public String getBase64Image()
    {
        return mBase64Image;
    }

    public void setBase64Image(String base64Image)
    {
        mBase64Image = base64Image;
    }

    public Bitmap getBitmap()
    {
        return mBitmap;
    }

    public void setBitmap(Bitmap bitmap)
    {
        mBitmap = bitmap;
    }
}
