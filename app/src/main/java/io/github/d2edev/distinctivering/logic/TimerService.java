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

    public static final String TAG="TAG_"+TimerService.class.getSimpleName();
    public static final String KEY_TOTAL_TIME_SEC ="ktt";
    private static final long STEPS = 4;
    private static final long DEFAULT_TIME_SEC = 30;
    private static final long CYCLE_TIME_MSEC = 1000;
    private long mTotalTime;
    private final IBinder mTimeSeviceBinder = new TimeServiceBinder();
    private TimerServiceListener listener;


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
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onHandleIntent: service started");
        }
          mTotalTime= CYCLE_TIME_MSEC* intent.getLongExtra(KEY_TOTAL_TIME_SEC, DEFAULT_TIME_SEC);
        long cycleTime = mTotalTime/STEPS;
        long counter=0;
        while(counter<STEPS){
            counter++;
            try {
                Thread.sleep(cycleTime);
            } catch (InterruptedException e) {
                Log.e(TAG, "onHandleIntent: sleep", e);
            }
            if(listener!=null)listener.timeCycle((int) (counter*100/STEPS));
        }
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onHandleIntent: service stopped");
        }
        if(listener!=null)listener.timeCycle(100);

    }

    @Override
    public boolean onUnbind(Intent intent) {
        listener=null;
        return super.onUnbind(intent);
    }

    public class TimeServiceBinder extends Binder{
       public TimerService getService(){
            return TimerService.this;
        }
    }

    interface TimerServiceListener{
        void timeCycle(int percentDone);
    }
}
