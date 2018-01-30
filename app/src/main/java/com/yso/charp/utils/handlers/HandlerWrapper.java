package com.yso.charp.utils.handlers;

import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;


/**
 * Wrap os Handler, and add onPaused and onResume option.
 */
public abstract class HandlerWrapper
{
    private static final String LOG_TAG = HandlerWrapper.class.getSimpleName();

    private Handler mHandler;

    protected abstract Looper getLooper();

    protected HandlerWrapper()
    {
        mHandler = createHandler();
    }

    protected HandlerWrapper(Callback callback)
    {
        mHandler = createHandler(callback);
    }

    public void resume()
    {
        if (mHandler == null)
        {
            mHandler = createHandler();
        }
    }

    @NonNull
    private Handler createHandler()
    {
        return new Handler(getLooper());
    }

    public void resume(Callback callback)
    {
        if (mHandler == null)
        {
            mHandler = createHandler(callback);
        }
    }

    @NonNull
    private Handler createHandler(Callback callback)
    {
        return new Handler(getLooper(), callback);
    }

    public void pause()
    {
        if (mHandler != null)
        {
            removeCallbacksAndMessages();
            mHandler = null;
        }
    }

    public void removeCallbacksAndMessages()
    {
        if (mHandler != null)
        {
            mHandler.removeCallbacksAndMessages(null);
        }
    }

    public boolean postDelayed(Runnable r, long delayMillis)
    {
        if (mHandler == null)
        {
            return false;
        }

        return mHandler.postDelayed(r, delayMillis);
    }

    public boolean post(Runnable r)
    {
        if (mHandler == null)
        {
            return false;
        }

        return mHandler.post(r);
    }

    public boolean runOnThisThread(Runnable r)
    {
        if (mHandler == null)
        {
            return false;
        }

        if (isOnCurrentThread())
        {
            r.run();
            return true;
        }

        return mHandler.post(r);

    }

    private boolean isOnCurrentThread()
    {
        return Thread.currentThread() == getLooper().getThread();
    }

    public boolean sendEmptyMessage(int what)
    {
        if (mHandler == null)
        {
            return false;
        }

        return mHandler.sendEmptyMessage(what);
    }

    public void removeMessages(int what)
    {
        if (mHandler != null)
        {
            mHandler.removeMessages(what);
        }
    }

    @Nullable
    public Message obtainMessage()
    {
        if (mHandler == null)
        {
            return null;
        }

        return mHandler.obtainMessage();
    }

    public boolean sendMessageDelayed(Message msg, long delayMillis)
    {
        if (mHandler == null)
        {
            return false;
        }

        return mHandler.sendMessageDelayed(msg, delayMillis);
    }

    public boolean sendEmptyMessageDelayed(int what, long delayMillis)
    {
        if (mHandler == null)
        {
            return false;
        }

        return mHandler.sendEmptyMessageDelayed(what, delayMillis);
    }

    public void removeCallbacks(Runnable r)
    {
        if (mHandler != null)
        {
            mHandler.removeCallbacks(r);
        }
    }
}
