package io.github.d2edev.distinctivering.logic;

import android.content.Context;
import android.media.AudioManager;
import android.widget.Toast;

import io.github.d2edev.distinctivering.util.Utility;

/**
 * Created by d2e on 10.06.16.
 */

public class EventProcessor implements IncomingCallListener {

    private static EventProcessor instance;

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
        if (Utility.isDistinctiveRingEnabled(context)) {
            if (Utility.isNUmberInList(context, number)) {
                Utility.saveVolumeLevel(context, volCurrent);
                am.setStreamVolume(AudioManager.STREAM_RING, volMax, 0);
                int val = am.getStreamVolume(AudioManager.STREAM_RING);
                Toast.makeText(context, "DR enabled, call from " + number + " volume was " + volCurrent + " now set to " + val, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(context, "Number " + number + " not in list!", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(context, "DR disabled!", Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public void onCallEnded(Context context) {
        AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (Utility.isDistinctiveRingEnabled(context)) {
            am.setStreamVolume(AudioManager.STREAM_RING, Utility.getSavedVolumeLevel(context), 0);
            int val = am.getStreamVolume(AudioManager.STREAM_RING);
            Toast.makeText(context, " call ended, volume set to " + val, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(context, "call ended", Toast.LENGTH_LONG).show();
        }
    }
}
