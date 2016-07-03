package io.github.d2edev.distinctivering.logic;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;

import io.github.d2edev.distinctivering.BuildConfig;

/**
 * Created by d2e on 10.06.16.
 */


//receiver is subscribed to PHONE_STATE broadcasts in Manifest
public class PhoneEventReceiver extends BroadcastReceiver {
    public static final String TAG = "TAG_" + PhoneEventReceiver.class.getSimpleName();

    private IncomingCallListener incomingCallListener;

    PhoneEventReceiver() {
        //on class init we  connect to interface

        incomingCallListener = EventProcessor.getInstance();
    }


    @Override
    public void onReceive(Context context, Intent intent) {

        //on bcast receive we get instatnce of Tel Manager
        //and check call state
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Service.TELEPHONY_SERVICE);
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "getCallState" + tm.getCallState());
        }
        switch (tm.getCallState()) {
            case TelephonyManager.CALL_STATE_RINGING: {
                //in case of incoming call we check what's the incoming number
                //and if it makes sense - pass it to Event Processor through interface
                String phoneNr = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
                if (phoneNr != null && !phoneNr.equals("")) {
                    incomingCallListener.onIncomingCall(context, phoneNr);
                }
                break;
            }
            case TelephonyManager.CALL_STATE_IDLE: {
                incomingCallListener.onCallEnded(context);
                break;
            }
            case TelephonyManager.CALL_STATE_OFFHOOK:{
                incomingCallListener.onOffHook(context);
                break;
            }
        }
    }


}
