package io.github.d2edev.distinctivering.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import io.github.d2edev.distinctivering.BuildConfig;
import io.github.d2edev.distinctivering.R;
import io.github.d2edev.distinctivering.db.DataContract;

/**
 * Created by d2e on 10.06.16.
 */

public class Utility {
    private static final String TAG = "TAG_" + Utility.class.getSimpleName();
    public static final String KEY_ENABLE_DISTINCTIVE_RING = "enable_distinctive_ring";
    public static final String KEY_PREVIOUS_RING_VOLUME_LEVEL = "prev_ring_vol_level";
    public static final String KEY_FIRST_LAUNCH = "is_first_launch";
    public static final String PIC_DIR = "pics";
    public static final String PIC_DEFAULT_NAME = "ic_person_default.png";
    public static final String EXT = ".png";
    public static final String KEY_SORT_INDEX = "list_sort_index";
    public static final String KEY_SORT_ORDER = "key_sort_order";
    public static final int SORT_BY_FIRST_NAME = 0;
    public static final int SORT_BY_LAST_NAME = 1;
    public static final int SORT_BY_NUMBER = 2;
    public static final String KEY_RINGER_MODE = "key_ringer_mode";
    //below should be in sync with name="pref_key_show_on_startup" in strings.xml
    private static final String KEY_SHOW_STARTUP_MESSAGE = "key_show_on_startup";
    //below should be in sync with name="pref_key_time_window" in strings.xml
    private static final String KEY_TIME_WINDOW = "key_timewindow";
    public static final int TIME_WINDOW_DEFAULT_VALUE = 30;

    /**
     * Helper method to set needed ring status in shared preferences
     *
     * @param context Context from which method is called
     * @param value   respective app_settings
     */
    public static void setDistinctiveRingEnabled(Context context, boolean value) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(KEY_ENABLE_DISTINCTIVE_RING, value).apply();

    }

    /**
     * Helper method to get ring status from shared preferences
     *
     * @param context Context from which method is called
     * @return respective app_settings or <b>false<b/> if key doesn't exist yet
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

    public static boolean isNUmberInList(Context context, String number) {
        boolean result = false;
        Cursor cursor = context.getContentResolver()
                .query(
                        DataContract.PhoneNumber.CONTENT_URI,
                        new String[]{DataContract.PhoneNumber.COLUMN_NUMBER},
                        null,
                        null,
                        null);
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            do {
//                Log.d(TAG, "isNUmberInList: cursor position" + cursor.getPosition());
                if (PhoneNumberUtils.compare(cursor.getString(0), number)) {
                    result = true;
                    break;
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        return result;


    }


    /**
     * Helper method called once to make preparations:
     * create dir to store user pics, put there default user pic
     *
     * @param context Context from which method is called
     */
    public static void firstLaunchPreparations(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        boolean isFirstLaunch = sp.getBoolean(KEY_FIRST_LAUNCH, true);
        if (isFirstLaunch) {
            File fileDir = context.getDir(PIC_DIR, Context.MODE_PRIVATE);
            String defaultPicPath = fileDir.getPath() + File.separator + PIC_DEFAULT_NAME;
            Bitmap defPic = BitmapFactory.decodeResource(context.getResources(), R.raw.ic_person_green);
            sp.edit()
                    .putInt(KEY_TIME_WINDOW,TIME_WINDOW_DEFAULT_VALUE)
                    .putBoolean(KEY_SHOW_STARTUP_MESSAGE, true)
                    .putBoolean(KEY_ENABLE_DISTINCTIVE_RING, false)
                    .putBoolean(KEY_FIRST_LAUNCH, false)
                    .apply();
            storeImageasPNG(defPic, defaultPicPath);
        }
    }

    /**
     * Helper method to set image which ImagView shows
     *
     * @param imageView        image view to set image to
     * @param pathToPicFile    path to image file which we use to show
     * @param defaultResouceId resource ID for default image which is shown if file is absent,
     *                         corrupted, etc
     */
    public static void setImage(ImageView imageView, String pathToPicFile, int defaultResouceId) {
        //if path is empty or null put default image
        if (pathToPicFile == null || pathToPicFile.equals("")) {
            imageView.setImageResource(defaultResouceId);
            return;
        }
        //if null returned due to some reason - put default image otherwise put pic
        Bitmap bitmap = decodeSampledBitmapFromFile(pathToPicFile, 50, 50);
        if (bitmap == null) {
            imageView.setImageResource(defaultResouceId);
        } else {
            imageView.setImageBitmap(bitmap);
        }


    }

    /**
     * Helper method to get sampled bitmap form provided URI
     *
     * @param uri       locates source bitmap
     * @param context   Context from which method is called
     * @param reqWidth  int, is required width in pixels (should be above 0)
     * @param reqHeight int, is required height in pixels (should be above 0)
     * @return sampled Bitmap or null
     */

    public static Bitmap decodeSampledBitmapFromUri(Uri uri, Context context, int reqWidth, int reqHeight) {
        Bitmap bitmap = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        InputStream input = null;
        byte[] imageArray = null;

        try {
            input = context.getContentResolver().openInputStream(uri);
            imageArray = inputStreamToByteArray(input);
            if (imageArray != null && imageArray.length > 0) {
                bitmap = BitmapFactory.decodeByteArray(imageArray, 0, imageArray.length, options);
                options.inSampleSize = Utility.calculateInSampleSize(options, reqWidth, reqHeight);
                options.inJustDecodeBounds = false;
                bitmap = BitmapFactory.decodeByteArray(imageArray, 0, imageArray.length, options);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return bitmap;
        }


    }

    /**
     * Helper method to get sampled bitmap form provided file path
     *
     * @param filePath  String, locates source file
     * @param reqWidth  int, is required width in pixels (should be above 0)
     * @param reqHeight int, is required height in pixels (should be above 0)
     * @return sampled Bitmap or null
     */
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

    /**
     * Helper method to get ratio by which bitmap should be downsampled to have closest pixel
     * dimension mathch to requested numbers
     *
     * @param options   BitmapFactory.Options which provide info on current bitmap size
     * @param reqWidth  int, is required width in pixels (should be above 0)
     * @param reqHeight int, is required height in pixels (should be above 0)
     * @return sampled Bitmap or null
     */
    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
//        Log.d(TAG, "calculateInSampleSize: h:" + height + " w:" + width);
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
//        Log.d(TAG, "calculateInSampleSize: " + inSampleSize);
        return inSampleSize;
    }


    /**
     * Helper method to store provided bitmap to provided file path as PNG format
     *
     * @param bitmapImage Bitmap which caontains image
     * @param fileName    String which contains path to file to be saved, incl filename and extension
     * @return true is save is ok, otherwise false
     */
    public static boolean storeImageasPNG(Bitmap bitmapImage, String fileName) {
        FileOutputStream fos = null;
        File pictureFile = new File(fileName);
        if (pictureFile == null) {
            Log.d(TAG,
                    "Error creating media file, check storage permissions: ");// e.getMessage());
            return false;
        }
        try {

            fos = new FileOutputStream(pictureFile);
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 90, fos);
        } catch (FileNotFoundException e) {
            Log.d(TAG, "File not found: " + e.getMessage());
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                Log.d(TAG, "Stream error: " + e.getMessage());
            }

        }
        return true;
    }

    /**
     * Helper method to get saved sort type index for allowed number list
     * 0=BY FIRST NAME
     * 1=BY SECOND NAME
     * 2=BY NUMBER
     *
     * @param context Context from which method is called
     * @return int as index
     */

    public static int getSortTypeIndex(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getInt(KEY_SORT_INDEX, 0);
    }

    public static void setSortTypeIndex(Context context, int index) {
        if (index < 0 || index > 2) index = 0;
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putInt(KEY_SORT_INDEX, index).apply();
    }


    /**
     * Helper method to get column name to sort on
     * 0=BY FIRST NAME
     * 1=BY SECOND NAME
     * 2=BY NUMBER
     *
     * @param sortIndex int sort type index for allowed number list
     * @return String column name to sort on
     */
    public static String getSortColumnName(int sortIndex) {
        switch (sortIndex) {
            case SORT_BY_FIRST_NAME:
                return DataContract.Person.COLUMN_FIRST_NAME;
            case SORT_BY_LAST_NAME:
                return DataContract.Person.COLUMN_LAST_NAME;
            case SORT_BY_NUMBER:
                return DataContract.PhoneNumber.COLUMN_NUMBER;
        }
        return DataContract.Person.COLUMN_FIRST_NAME;
    }

    public static void setSortOrderAscending(Context context, boolean value) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(KEY_SORT_ORDER, value).apply();
    }

    public static boolean isSortOrderAscending(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(KEY_SORT_ORDER, true);
    }

    public static int getSortOrderIndex(boolean sortAsc) {
        return sortAsc ? 0 : 1;
    }

    public static String getAppVersion(Context context) {

        String version = "undefined";
        try {
            version = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } finally {
            return version;
        }
    }

    public static String getLastBuildTime(Context context) {
        String date = "undefined";
        try {
            ApplicationInfo ai = context.getPackageManager().getApplicationInfo(context.getPackageName(), 0);
            ZipFile zf = new ZipFile(ai.sourceDir);
            ZipEntry ze = zf.getEntry("classes.dex");
            long time = ze.getTime();
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yy HH:mm");
            date = sdf.format(new java.util.Date(time));
            zf.close();
        } catch (Exception e) {
        }
        return date;
    }

    public static void saveRingerMode(Context context, int ringerMode) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putInt(KEY_RINGER_MODE, ringerMode).apply();
    }

    public static int getSavedRingerMode(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getInt(KEY_RINGER_MODE, AudioManager.RINGER_MODE_SILENT);
    }

    public static byte[] inputStreamToByteArray(InputStream is) {

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] byteArray = null;
        byte[] data = new byte[512];
        try {
            while ((nRead = is.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();
            byteArray = buffer.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            return byteArray;
        }

    }

    public static boolean isTelephonyAvailable(Context context) {
        int type = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getPhoneType();
        if (type == TelephonyManager.PHONE_TYPE_NONE) {
            return false;
        } else {
            return true;
        }

    }

    public static void setShowStartupMessage(Context context, boolean show) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(KEY_SHOW_STARTUP_MESSAGE, show).apply();
    }

    public static boolean getShowStartupMessage(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(KEY_SHOW_STARTUP_MESSAGE, true);
    }

    public static boolean isNUmberInList_STUB(Context context, String number) {
        return true;
    }

    public static int getTimeWindow(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getInt(KEY_TIME_WINDOW, TIME_WINDOW_DEFAULT_VALUE);
    }
}
