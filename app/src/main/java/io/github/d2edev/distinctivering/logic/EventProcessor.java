package io.github.d2edev.distinctivering.logic;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.os.IBinder;
import android.util.Log;

import io.github.d2edev.distinctivering.BuildConfig;
import io.github.d2edev.distinctivering.util.Utility;

/**
 * Created by d2e on 10.06.16.
 * Class wich implements logic on receiving calls.
 * Currently I cannot understand the reason why log states audio manager STREAM_RING and ringer mode
 * setting were set to max, phone doesn't make a sound.
 * So working logic will be:
 * first call from any allowed number sets volume up and starts service that waits for defined time
 * (one minute) during which all incoming calls ring, then mutes the ringer;
 */

public class EventProcessor implements IncomingCallListener, TimerService.TimerServiceListener {
    public static final String TAG = "TAG_" + EventProcessor.class.getSimpleName();

    private static EventProcessor instance;
    private boolean mBound;
    private boolean mTimeWindowOpened;
    private TimerService timerService;
    private Context mContext;

    private ServiceConnection mTimerConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "onServiceConnected");
            }
            TimerService.TimeServiceBinder binder = (TimerService.TimeServiceBinder) service;
            timerService = binder.getService();
            timerService.setTimerServiceListener(EventProcessor.this);
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBound = false;
        }
    };

    private EventProcessor() {

    }

    public static EventProcessor getInstance() {
        if (instance == null) {
            instance = new EventProcessor();
        }
        return instance;
    }


    @Override
    public void onIncomingCall(Context context, String number) {
        AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        int volCurrent = am.getStreamVolume(AudioManager.STREAM_RING);
        int volMax = am.getStreamMaxVolume(AudioManager.STREAM_RING);
        int ringerMode = am.getRingerMode();
        if (Utility.isDistinctiveRingEnabled(context)) {
            if (Utility.isNUmberInList(context, number)) {
                if (!mTimeWindowOpened) {
                    mTimeWindowOpened = true;
                    Utility.saveRingerModel(context, ringerMode);
                    Utility.saveVolumeLevel(context, volCurrent);
                    am.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                    am.setStreamVolume(AudioManager.STREAM_RING, volMax, 0);
                    Intent startTimer = new Intent(context, TimerService.class);
                    context.getApplicationContext().startService(startTimer);
                    Intent bindTimer = new Intent(context,TimerService.class);
                    context.getApplicationContext().bindService(bindTimer, mTimerConnection,Context.BIND_AUTO_CREATE);
                }
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "DR enabled, call from " + number +
                            " volume was " + volCurrent +
                            " now set to " + am.getStreamVolume(AudioManager.STREAM_RING) +
                            " ringer mode was " + ringerMode +
                            " now set to " + am.getRingerMode()
                    );
                }
            } else {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "Number " + number + " not in list!");
                }
            }
        } else {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "DR disabled!");
            }
        }

    }

    @Override
    public void onCallEnded(Context context) {
        mContext = context.getApplicationContext();
        Log.d(TAG, "call ended");

    }

    @Override
    public void timeCycle(int percentDone) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "EventProcessor: timeCycle percent " + percentDone);
        }
        if (percentDone == 100) {
            AudioManager am = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
            am.setRingerMode(Utility.getSavedRingerMode(mContext));
            am.setStreamVolume(AudioManager.STREAM_RING, Utility.getSavedVolumeLevel(mContext), 0);
            mTimeWindowOpened = false;
            if (BuildConfig.DEBUG) {
                int val = am.getStreamVolume(AudioManager.STREAM_RING);
                int mode = am.getRingerMode();
                Log.d(TAG, " call ended, volume set to " + val + " mode set to " + mode);
            }
        }
    }
}
