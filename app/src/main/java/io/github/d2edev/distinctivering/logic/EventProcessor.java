package io.github.d2edev.distinctivering.logic;

import android.content.Context;
import android.media.AudioManager;
import android.util.Log;

import io.github.d2edev.distinctivering.util.Utility;

/**
 * Created by d2e on 10.06.16.
 */

public class EventProcessor implements IncomingCallListener {
    public static final String TAG="TAG_"+EventProcessor.class.getSimpleName();

    private static EventProcessor instance;
    private  boolean numFromList=false;

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
        int ringerMode =am.getRingerMode();
        if (Utility.isDistinctiveRingEnabled(context)) {
            if (Utility.isNUmberInList(context, number)) {
                numFromList=true;
                Utility.saveVolumeLevel(context, volCurrent);
                Utility.saveRingerModel(context, ringerMode);
                am.setStreamVolume(AudioManager.STREAM_RING, volMax, 0);
                am.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                int val = am.getStreamVolume(AudioManager.STREAM_RING);
                Log.d(TAG, "DR enabled, call from " + number + " volume was " + volCurrent + " now set to " + val+ " ringer mode was " + ringerMode);
            } else {
                Log.d(TAG, "Number " + number + " not in list!");
            }
        } else {
            Log.d(TAG, "DR disabled!");
        }

    }

    @Override
    public void onCallEnded(Context context) {
        AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (Utility.isDistinctiveRingEnabled(context)&&numFromList) {
            am.setStreamVolume(AudioManager.STREAM_RING, Utility.getSavedVolumeLevel(context), 0);
            int val = am.getStreamVolume(AudioManager.STREAM_RING);
            am.setRingerMode(Utility.getSavedRingerMode(context));
            int mode=am.getRingerMode();
            Log.d(TAG," call ended, volume set to " + val + " mode set to " + mode);
            numFromList=false;
        } else {
            Log.d(TAG, "call ended");
        }
    }
}
