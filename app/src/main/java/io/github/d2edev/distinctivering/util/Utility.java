package io.github.d2edev.distinctivering.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import android.widget.ImageView;

import java.sql.Array;
import java.text.DecimalFormat;
import java.util.Arrays;

import io.github.d2edev.distinctivering.ui.MainActivity;

/**
 * Created by d2e on 10.06.16.
 */

public class Utility {
    public static final String KEY_ENABLE_DISTINCTIVE_RING = "enable_distinctive_ring";
    public static final String KEY_PREVIOUS_RING_VOLUME_LEVEL = "prev_ring_vol_level";
    public static final String KEY_FIRST_LAUNCH = "is_first_launch";
    public static final String PIC_DIR = "pics";

    /**
     * Helper method to set needed ring status in shared preferences
     *
     * @param context Context from which method is called
     * @param value   respective setting
     */
    public static void setDistinctiveRingEnabled(Context context, boolean value) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(KEY_ENABLE_DISTINCTIVE_RING, value).apply();

    }

    /**
     * Helper method to get ring status from shared preferences
     *
     * @param context Context from which method is called
     * @return respective setting or <b>false<b/> if key doesn't exist yet
     */
    public static boolean isDistinctiveRingEnabled(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(KEY_ENABLE_DISTINCTIVE_RING, false);
    }

    /**
     * Helper method to save ring volume level in shared preferences
     *
     * @param context    Context from which method is called
     * @param volCurrent respective volume level
     */
    public static void saveVolumeLevel(Context context, int volCurrent) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putInt(KEY_PREVIOUS_RING_VOLUME_LEVEL, volCurrent).apply();
    }

    /**
     * Helper method to retrive volume level from shared preferences
     *
     * @param context Context from which method is called
     * @return saved volume level or <b>zero<b/> if key doesn't exist yet
     */
    public static int getSavedVolumeLevel(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getInt(KEY_PREVIOUS_RING_VOLUME_LEVEL, 0);
    }

    public static boolean isNUmberInList(String number) {
        return "+380675721286".equals(number);
    }

    public static void firstLaunchPreparations(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        boolean isFirstLaunch = sp.getBoolean(KEY_FIRST_LAUNCH, true);
        if (isFirstLaunch) {
            context.getDir(PIC_DIR, Context.MODE_PRIVATE);
            sp.edit().putBoolean(KEY_FIRST_LAUNCH, false).apply();
        }
    }


    public static void setImage(ImageView imageView, String pathToPicFile, int defaultResouceId) {

        //if path is empty or null put default image
        if (pathToPicFile.equals(null) || pathToPicFile.equals("")) {
            imageView.setImageResource(defaultResouceId);
            return;
        }

        //if null returned due to some reson - put default image otherwise put pic
        Bitmap bitmap = decodeSampledBitmapFromFile(pathToPicFile, 50, 50);
        if (bitmap == null) {
            imageView.setImageResource(defaultResouceId);
        } else {
            imageView.setImageBitmap(bitmap);
        }


    }

    public static Bitmap decodeSampledBitmapFromFile(String filePath, int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filePath, options);
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }
}
