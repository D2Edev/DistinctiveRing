package io.github.d2edev.distinctivering.logic;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;

/**
 * Created by d2e on 10.06.16.
 */

public class PhoneEventReceiver extends BroadcastReceiver {

    private IncomingCallListener incomingCallListener;

    PhoneEventReceiver() {
        incomingCallListener = EventProcessor.getInstance();
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Service.TELEPHONY_SERVICE);
        String phoneNr = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
        switch (tm.getCallState()) {
            case TelephonyManager.CALL_STATE_RINGING: {
                incomingCallListener.onIncomingCall(context, phoneNr);
                break;
            }
            case TelephonyManager.CALL_STATE_IDLE: {
                incomingCallListener.onCallEnded(context);
                break;
            }
        }

    }
}
