package io.github.d2edev.distinctivering.logic;

import android.content.Context;

/**
 * Created by d2e on 10.06.16.
 */

public interface IncomingCallListener {
    void onIncomingCall(Context context, String number);

    void onCallEnded(Context context);
}
