package io.github.d2edev.distinctivering.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by d2e on 10.06.16.
 */

public class Utility {
    public static final String KEY_ENABLE_DISTINCTIVE_RING="enable_distinctive_ring";
    public static final String KEY_PREVIOUS_RING_VOLUME_LEVEL="prev_ring_vol_level";

    /**
     * Helper method to set needed ring status in shared preferences
     * @param context Context from which method is called
     * @param value respective setting
     */
    public static void setDistinctiveRingEnabled(Context context, boolean value){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(KEY_ENABLE_DISTINCTIVE_RING,value).apply();

    }

    /**
     * Helper method to get ring status from shared preferences
     * @param context Context from which method is called
     * @return  respective setting or <b>false<b/> if key doesn't exist yet
     */
    public static boolean isDistinctiveRingEnabled(Context context){
        SharedPreferences sp=PreferenceManager.getDefaultSharedPreferences(context);
        return  sp.getBoolean(KEY_ENABLE_DISTINCTIVE_RING,false);
    }

    /**
     * Helper method to save ring volume level in shared preferences
     * @param context Context from which method is called
     * @param volCurrent respective volume level
     */
    public static void saveVolumeLevel(Context context, int volCurrent) {
        SharedPreferences sp=PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putInt(KEY_PREVIOUS_RING_VOLUME_LEVEL,volCurrent).apply();
    }

    /**
     * Helper method to retrive volume level from shared preferences
     * @param context Context from which method is called
     * @return  saved volume level or <b>zero<b/> if key doesn't exist yet
     */
    public static int getSavedVolumeLevel(Context context){
        SharedPreferences sp=PreferenceManager.getDefaultSharedPreferences(context);
        return  sp.getInt(KEY_PREVIOUS_RING_VOLUME_LEVEL,0);
    }

    public static boolean isNUmberInList(String number) {
        return "+380675721286".equals(number);
    }
}
