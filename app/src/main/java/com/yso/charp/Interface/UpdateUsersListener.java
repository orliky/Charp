package com.yso.charp.Interface;

import com.yso.charp.model.User;

import java.util.HashMap;

/**
 * Created by Admin on 26-Nov-17.
 */

public interface UpdateUsersListener
{
    void onDataChange(HashMap<String, User> data);
}
