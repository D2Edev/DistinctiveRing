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
 * Class which implements logic on receiving calls.
 * Currently I cannot understand the reason why log states audio manager STREAM_RING and ringer mode
 * app_settings were set to max, phone doesn't make a sound.
 * So working logic will be:
 * if Distinctive ring enabled:
 * a)after first call from any allowed number ("trigger" call) ends we set volume up and mode to NORMAL
 * and start service that waits for defined time window (i.e. one minute) during which any incoming
 * call rings loud.
 * b)After defined timeout ends (100% timeout done) ringer volume and mode settings return to initial
 * ones
 * c)if call was picked we suspend timer service until call ends, then continue countdown
 * (future work)if call was picked, we sign-off the service, app_settings timeout complete 100, which sets ringer
 * volume and mode settings return to initial immediately
 */

public class EventProcessor implements IncomingCallListener, TimerService.TimerServiceListener {
    public static final String TAG = "TAG_" + EventProcessor.class.getSimpleName();

    private static EventProcessor instance;
    private boolean mBound;
    private boolean mTimeWindowOpened;
    private TimerService timerService;
    private Context mContext;
    private static final int MAX_PERCENT = 100;
    public static final int BORDER_VOL_PRECENT = 50;

    private ServiceConnection mTimerConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "onServiceConnected");
            }
            //gets service instance and sets EventProcessor as listener to timer events
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

    //make it singleton
    public static EventProcessor getInstance() {
        if (instance == null) {
            instance = new EventProcessor();
        }
        return instance;
    }


    @Override
    public void onIncomingCall(Context context, String number) {
        //it make sense to continue only if feature is enabled
        if (Utility.isDistinctiveRingEnabled(context)) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "DR enabled, call from " + number);
            }
            //if incoming call is from listed number - continue
            if (Utility.isNUmberInList(context, number)) {
                //first or follow-up call
                //if first - save sound settings and set flag that time window is open
                if (!mTimeWindowOpened) {
                    mTimeWindowOpened = true;
                    AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                    int volCurrent = am.getStreamVolume(AudioManager.STREAM_RING);
                    int ringerMode = am.getRingerMode();
                    Utility.saveRingerMode(context, ringerMode);
                    Utility.saveVolumeLevel(context, volCurrent);
                } else {
                    //follow-up call - need to pause time service
                    if (timerService != null && timerService.isActive()) {
                        if (BuildConfig.DEBUG) {
                            Log.d(TAG, "Pausing timer service: ");
                        }
                        timerService.setPaused(true);
                    }
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
    public void onOffHook(Context context) {
        //TODO clarify offhook logic
//        if (mTimeWindowOpened) {
//            //almost like stopping service
//            timerService.setActive(false);
//        }
//        if (BuildConfig.DEBUG) {
//            Log.d(TAG, "off hook");
//        }

    }

    @Override
    public void onCallEnded(Context context) {
        mContext = context.getApplicationContext();
        //mTimeWindowOpened=true means we just ended 1st, "trigger" call from trusted(allowed) number
        //or any call in allowed timeframe
        if (mTimeWindowOpened) {
            //if service is started (means it's end of follow-up call)
            //we should resume countdown
            if (timerService != null && timerService.isActive()) {
                if (timerService.isPaused()) {
                    timerService.setPaused(false);
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "unpausing timer service: ");
                    }
                }
            } else {
                //set vol to max and start service
                //thin bind to it to be able listening to service events
                //or managing sevice
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "Starting timer service: ");
                }
                AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                int volMax = am.getStreamMaxVolume(AudioManager.STREAM_RING);
                //2 lines below just for check
                int vol = Utility.getSavedVolumeLevel(mContext);
                int mode = Utility.getSavedRingerMode(mContext);
                //end check
                am.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                am.setStreamVolume(AudioManager.STREAM_RING, volMax, 0);
                Intent startTimerService = new Intent(context, TimerService.class);
                int sec = Utility.getTimeWindow(mContext);
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "pref time window, sec: " + sec);
                }
                startTimerService.putExtra(TimerService.KEY_RUNNING_TIME_SEC, sec);
                mContext.startService(startTimerService);
                Intent bindTimer = new Intent(context, TimerService.class);
                mContext.bindService(bindTimer, mTimerConnection, Context.BIND_AUTO_CREATE);
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "DR enabled, window opened, volume was " + vol +
                            " now set to " + am.getStreamVolume(AudioManager.STREAM_RING) +
                            " ringer mode was " + mode +
                            " now set to " + am.getRingerMode()
                    );
                }
            }
        }
    }

    //listens to progress done, at 100% restore "silent" values and set flag that time window
    //is closed
    @Override
    public void timeCycle(int percentDone) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "EventProcessor: timeCycle percent " + percentDone);
        }
        if (percentDone == 100) {
            AudioManager am = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
            int vol = Utility.getSavedVolumeLevel(mContext);
            int mode = Utility.getSavedRingerMode(mContext);
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "saved values: ringer mode-" + mode + ", ring volume-" + vol);
            }
            am.setStreamVolume(AudioManager.STREAM_RING, vol, 0);
            am.setRingerMode(mode);
            mTimeWindowOpened = false;
            if (BuildConfig.DEBUG) {
                vol = am.getStreamVolume(AudioManager.STREAM_RING);
                mode = am.getRingerMode();
                Log.d(TAG, " call ended, volume set to " + vol + " mode set to " + mode);
            }
        }
    }
}
