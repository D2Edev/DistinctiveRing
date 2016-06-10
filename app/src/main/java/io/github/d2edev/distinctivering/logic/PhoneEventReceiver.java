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

    @Override
    public void onReceive(Context context, Intent intent) {
        TelephonyManager tm= (TelephonyManager) context.getSystemService(Service.TELEPHONY_SERVICE);
        switch (tm.getCallState()){
            case TelephonyManager.CALL_STATE_RINGING:{

            }
        }

    }
}
