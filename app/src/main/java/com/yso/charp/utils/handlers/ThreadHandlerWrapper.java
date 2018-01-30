package com.yso.charp.utils.handlers;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

public class ThreadHandlerWrapper extends HandlerWrapper
{
    private static int msThreadId;
    private HandlerThread mHandlerThread;

    public ThreadHandlerWrapper()
    {
        super();
    }

    public ThreadHandlerWrapper(Handler.Callback callback)
    {
        super(callback);
    }

    @Override
    protected Looper getLooper()
    {
        if(mHandlerThread == null)
        {
            mHandlerThread = new HandlerThread(ThreadHandlerWrapper.class.getSimpleName() + ++msThreadId);
            mHandlerThread.start();
        }

        return mHandlerThread.getLooper();
    }

    @Override
    public void pause()
    {
        super.pause();

        if(mHandlerThread != null)
        {
            mHandlerThread.quit();
            mHandlerThread = null;
        }
    }
}
