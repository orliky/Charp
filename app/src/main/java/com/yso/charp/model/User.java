package com.yso.charp.model;

/**
 * Created by Admin on 19-Nov-17.
 */

public class User
{
    private String name;
    private String phone;
    private String created;
    private String signedIn;
    private String UID;

    public User(String name, String phone, String UID) {
        this.name = name;
        this.phone = phone;
        this.UID = UID;
    }

    public User(){

    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getPhone()
    {
        return phone;
    }

    public void setPhone(String phone)
    {
        this.phone = phone;
    }

    public String getCreated()
    {
        return created;
    }

    public void setCreated(String created)
    {
        this.created = created;
    }

    public String getSignedIn()
    {
        return signedIn;
    }

    public void setSignedIn(String signedIn)
    {
        this.signedIn = signedIn;
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
