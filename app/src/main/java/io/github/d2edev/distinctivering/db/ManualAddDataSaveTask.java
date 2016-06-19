package io.github.d2edev.distinctivering.db;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;

import java.io.File;

import io.github.d2edev.distinctivering.logic.DataSetWatcher;
import io.github.d2edev.distinctivering.util.Utility;

/**
 * Created by d2e on 17.06.16.
 */

public class ManualAddDataSaveTask extends AsyncTask<Bundle, Void, Void> {
    public static final String TAG = "TAG" + ManualAddDataSaveTask.class.getSimpleName();
    public static final String KEY_FIRST_NAME = "kfm";
    public static final String KEY_LAST_NAME = "klm";
    public static final String KEY_NUMBER = "kn";
    public static final String KEY_IMAGE_URI = "kiu";
    private Context context;
    private DataSetWatcher dataSetWatcher;

    public void setContext(Context context) {
        this.context = context;
    }

    public ManualAddDataSaveTask(Context context) {
        this.context = context;
    }

    @Override
    protected Void doInBackground(Bundle... params) {
        String firstName;
        String lastName;
        Parcelable parcelable;
        String selection = DataContract.Person.COLUMN_FIRST_NAME + "=? AND " + DataContract.Person.COLUMN_LAST_NAME + "=?";
        String[] selectionArgs;
        Bitmap bitmap;
        long idPerson;
        String number;
        Log.d(TAG, "doInBackground: ");
        for (int i = 0; i < params.length; i++) {

            idPerson = -1;
            //check if person with provided first and last names already exists
            //first define seleletion criteria
            firstName = params[i].getString(KEY_FIRST_NAME);
            lastName = params[i].getString(KEY_LAST_NAME);
            selectionArgs = new String[]{firstName, lastName};

            //query provider using selection
            Cursor cursor = context.getContentResolver().query(DataContract.Person.CONTENT_URI, null, selection, selectionArgs, null);
            if (cursor != null && cursor.getCount() > 0) {
                //if person already exists
                cursor.moveToFirst();
                //get person ID to be ready to save number
                idPerson = cursor.getLong(0);
            } else {
                //otherwise save person
                ContentValues personRecord = new ContentValues();
                personRecord.put(DataContract.Person.COLUMN_FIRST_NAME, firstName);
                personRecord.put(DataContract.Person.COLUMN_LAST_NAME, lastName);
                Uri newPersonUri = context.getContentResolver().insert(DataContract.Person.CONTENT_URI, personRecord);
                try {
                    //and get ID from saved entity to be ready to save number
                    idPerson = Long.parseLong(DataContract.Person.getPersonIdFromUri(newPersonUri));


                } catch (NumberFormatException e) {
                    Log.e(TAG, "addEntry: invalid parse data from uri " + newPersonUri);
                }
            }
            //if person data exists or already is saved
            if (idPerson > -1) {

                //if pic data provided
                parcelable = params[i].getParcelable(KEY_IMAGE_URI);
                if (parcelable != null && parcelable instanceof Bitmap) {
                    bitmap = (Bitmap) parcelable;
                    String picPath = context.getDir(Utility.PIC_DIR, Context.MODE_PRIVATE).getPath()
                            + File.separator
                            + lastName + "_" + firstName + "_" + idPerson + Utility.EXT;
                    //save pic
                    Utility.storeImageasPNG(bitmap, picPath);
                    //update record with pic path
                    selection = DataContract.Person._ID + "=?";
                    selectionArgs = new String[]{"" + idPerson};
                    ContentValues personRecord = new ContentValues();
                    personRecord.put(DataContract.Person.COLUMN_PIC_PATH, picPath);
                    context.getContentResolver().update(DataContract.Person.CONTENT_URI, personRecord, selection, selectionArgs);
                }

                //prepare and save phone data
                number = params[i].getString(KEY_NUMBER);
                ContentValues phoneRecord = new ContentValues();
                phoneRecord.put(DataContract.PhoneNumber.COLUMN_NUMBER, number);
                phoneRecord.put(DataContract.PhoneNumber.COLUMN_KEY_PERSON, idPerson);
                context.getContentResolver().insert(DataContract.PhoneNumber.CONTENT_URI, phoneRecord);
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        if (dataSetWatcher != null) dataSetWatcher.dataSetChanged();
    }

    public void setDataSetWatcher(DataSetWatcher dataSetWatcher) {
        this.dataSetWatcher = dataSetWatcher;
    }
}
