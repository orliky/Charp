package com.yso.charp.utils.handlers;

import android.os.Handler;
import android.os.Looper;

public class UiHandlerWrapper extends HandlerWrapper
{
    public UiHandlerWrapper()
    {
        super();
    }

    public UiHandlerWrapper(Handler.Callback callback)
    {
        super(callback);
    }

    @Override
    protected Looper getLooper()
    {
        return Looper.getMainLooper();
    }
}
