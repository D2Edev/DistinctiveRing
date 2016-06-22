package io.github.d2edev.distinctivering.logic;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;

/**
 * Created by d2e on 10.06.16.
 */

public class PhoneEventReceiver extends BroadcastReceiver {
    public static final String TAG="TAG_"+PhoneEventReceiver.class.getSimpleName();

    private IncomingCallListener incomingCallListener;

    PhoneEventReceiver() {
        incomingCallListener = EventProcessor.getInstance();
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.hasExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
                ||intent.hasExtra(TelephonyManager.EXTRA_STATE_IDLE)){
            String phoneNr = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Service.TELEPHONY_SERVICE);
            switch (tm.getCallState()) {
                case TelephonyManager.CALL_STATE_RINGING: {
                    incomingCallListener.onIncomingCall(context, phoneNr);
                    Log.d(TAG, "onReceive: "+ phoneNr);
                    break;
                }
                case TelephonyManager.CALL_STATE_IDLE: {
                    incomingCallListener.onCallEnded(context);

                    break;
                }
            }
        }


    }
}
