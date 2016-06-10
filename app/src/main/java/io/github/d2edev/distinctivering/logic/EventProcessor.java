package io.github.d2edev.distinctivering.logic;

import android.content.Context;
import android.media.AudioManager;
import android.widget.Toast;

import io.github.d2edev.distinctivering.util.Utility;

/**
 * Created by d2e on 10.06.16.
 */

public class EventProcessor implements IncomingCallListener{

    private static EventProcessor instance;

    private EventProcessor(){

    }

    public static EventProcessor getInstance() {
        if (instance==null){
            instance= new EventProcessor();
        }
        return  instance;
    }


    @Override
    public void onIncomingCall(Context context, String number) {
        if(Utility.isDistinctiveRingEnabled(context)){
            AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            int volCurrent=am.getStreamVolume(AudioManager.STREAM_RING);
            int volMax=am.getStreamMaxVolume(AudioManager.STREAM_RING);
            am.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
            am.setStreamVolume(AudioManager.STREAM_RING,volMax,0);
            Toast.makeText(context, "test: "+number+" volume "+volCurrent+"/"+volMax, Toast.LENGTH_LONG).show();
        }else{
            Toast.makeText(context, "DR disabled!", Toast.LENGTH_LONG).show();
        }

    }
}
