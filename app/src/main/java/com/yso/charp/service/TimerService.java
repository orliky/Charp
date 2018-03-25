package com.yso.charp.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;

import java.util.Timer;
import java.util.TimerTask;

@SuppressLint ("Registered")
public class TimerService extends Service
{
    private final IBinder binder = new LocalBinder();
    private static Timer timer = new Timer();
    private Context ctx;
    private TimerServiceListener serviceCallbacks;
    private MainTask mMainTask;

    public class LocalBinder extends Binder
    {
        public TimerService getService()
        {
            return TimerService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent)
    {
        return binder;
    }

    public void onCreate()
    {
        super.onCreate();
        ctx = this;
        startService();
    }

    private void startService()
    {
        mMainTask = new MainTask();
        timer.scheduleAtFixedRate(mMainTask, 0, 1000);
    }

    private class MainTask extends TimerTask
    {
        public void run()
        {
            toastHandler.sendEmptyMessage(0);
        }
    }

    @SuppressLint ("HandlerLeak")
    private final Handler toastHandler = new Handler()
    {
        final int i = 0;
        final int[] timeCounter = {59};
        @Override
        public void handleMessage(Message msg)
        {
            if (serviceCallbacks != null)
            {
                if (timeCounter[0] == i)
                {
                    serviceCallbacks.onTick(null);
                    timer.cancel();
                    mMainTask.cancel();
                }
                else
                {
                    String time = String.valueOf(timeCounter[0]);
                    if (time.length() == 1)
                    {
                        time = "0" + time;
                    }
                    serviceCallbacks.onTick("00:" + time);
                }
                timeCounter[0]--;
            }
        }
    };

    public interface TimerServiceListener
    {
        void onTick(String time);
    }

    public void setCallbacks(TimerServiceListener callbacks)
    {
        serviceCallbacks = callbacks;
    }
}
