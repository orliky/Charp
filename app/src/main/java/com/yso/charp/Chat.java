package com.yso.charp;

/**
 * Created by Admin on 19-Nov-17.
 */

public class Chat
{
    private String name;
    private String UID;

    public Chat(String name, String UID) {
        this.name = name;
        this.UID = UID;
    }

    public Chat(){

    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getUID()
    {
        return UID;
    }

    public void setUID(String UID)
    {
        this.UID = UID;
    }
}
