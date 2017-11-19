package com.yso.charp;

import java.util.Date;

/**
 * Created by Admin on 19-Nov-17.
 */

public class User
{
    private String name;
    private String email;
    private String created;
    private String signedIn;
    private String UID;

    public User(String name, String email, String UID) {
        this.name = name;
        this.email = email;
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

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
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
