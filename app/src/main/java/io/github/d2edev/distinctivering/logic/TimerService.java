package io.github.d2edev.distinctivering.logic;

import android.app.IntentService;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import io.github.d2edev.distinctivering.BuildConfig;

/**
 * Created by d2e on 01.07.16.
 */
public class TimerService extends IntentService {

    public static final String TAG = "TAG_" + TimerService.class.getSimpleName();
    public static final String KEY_RUNNING_TIME_SEC = "ktt";
    private static final long STEPS = 10;
    private static final int DEFAULT_TIME_SEC = 30;
    private static final long CYCLE_TIME_MSEC = 1000;
    private long mTotalTime;
    private final IBinder mTimeSeviceBinder = new TimeServiceBinder();
    private TimerServiceListener listener;
    private boolean active = true;
    private boolean paused;

    public boolean isActive() {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "isActive: ");
        }
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isPaused() {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "isPaused: ");
        }
        return paused;
    }

    public synchronized void setPaused(boolean paused) {
        this.paused = paused;
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "setPaused: " + paused);
        }
        notify();
    }

    public TimerService() {
        super(TAG);
    }

    public void setTimerServiceListener(TimerServiceListener listener) {
        this.listener = listener;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mTimeSeviceBinder;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        active = true;
        mTotalTime = CYCLE_TIME_MSEC * intent.getIntExtra(KEY_RUNNING_TIME_SEC, DEFAULT_TIME_SEC);
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onHandleIntent: service started for " + mTotalTime/CYCLE_TIME_MSEC+ "secs.");
        }
        long cycleTime = mTotalTime / STEPS;
        long counter = 0;
        //adding steps to couter until done or until active
        while (counter < STEPS && active) {
            //freeze if got pause signal
            synchronized (this) {
                while (paused) {
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "Timer: paused in main cycle: ");
                    }
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        Log.e(TAG, "onHandleIntent: wait", e);
                    }
                }
            }
            counter++;
            try {
                Thread.sleep(cycleTime);
            } catch (InterruptedException e) {
                Log.e(TAG, "onHandleIntent: sleep", e);
            }
            if (listener != null) listener.timeCycle((int) (counter * 100 / STEPS));
        }
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onHandleIntent: service stopped");
        }
        if (listener != null) listener.timeCycle(100);
        active = false;

    }

    @Override
    public boolean onUnbind(Intent intent) {
        listener = null;
        return true;
    }

    public class TimeServiceBinder extends Binder {
        public TimerService getService() {
            return TimerService.this;
        }
    }

    interface TimerServiceListener {
        void timeCycle(int percentDone);
    }


}
