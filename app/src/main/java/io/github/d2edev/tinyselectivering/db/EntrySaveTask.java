package io.github.d2edev.tinyselectivering.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;

import io.github.d2edev.tinyselectivering.logic.DataSetWatcher;
import io.github.d2edev.tinyselectivering.util.Utility;

/**
 * Created by d2e on 17.06.16.
 */

public class EntrySaveTask extends AsyncTask<Bundle, Void, Void> {
    public static final String TAG = "TAG_" + EntrySaveTask.class.getSimpleName();
    private Context mContext;
    private DataSetWatcher mDataSetWatcher;
    private boolean mDataChangeSuccess;
    private int mAllTaskQty;
    private int mDoneTaskQty;


    public EntrySaveTask(Context context) {
        this.mContext = context;
    }

    @Override
    protected Void doInBackground(Bundle... params) {
        String firstName = null;
        String lastName = null;
        Parcelable parcelable;
        String selection = null;
        String[] selectionArgs;
        Bitmap bitmap;
        long idPerson;
        String number;
        Log.d(TAG, "doInBackground: ");
        mAllTaskQty = params.length;
        for (int i = 0; i < mAllTaskQty; i++) {

            //first check if number already exists
            number = params[i].getString(DataContract.KEY_NUMBER);
            Uri reqNumUri = DataContract.PhoneNumber.buildPhoneNumberUriByValue(number);
            Cursor numCursor = mContext.getContentResolver().query(
                    reqNumUri,
                    null,
                    null,
                    null,
                    null);
            if (numCursor != null && numCursor.getCount() > 0) {
                //skip the rest of current cycle as phone number already exists
                //so we won't add record
                numCursor.close();
                continue;

            }
            numCursor.close();
            idPerson = -1;
            //check if person with provided first and last names already exists
            //first define selection criteria
            //three cases possible:
            //only last name exists
            if (!params[i].containsKey(DataContract.KEY_FIRST_NAME)) {
                lastName = params[i].getString(DataContract.KEY_LAST_NAME);
                selection = DataContract.Person.COLUMN_LAST_NAME + "=?";
                selectionArgs = new String[]{lastName};
                //only first name exists
            } else if (!params[i].containsKey(DataContract.KEY_LAST_NAME)) {
                firstName = params[i].getString(DataContract.KEY_FIRST_NAME);
                selection = DataContract.Person.COLUMN_FIRST_NAME + "=?";
                selectionArgs = new String[]{firstName};
            } else {
                firstName = params[i].getString(DataContract.KEY_FIRST_NAME);
                lastName = params[i].getString(DataContract.KEY_LAST_NAME);
                selection = DataContract.Person.COLUMN_FIRST_NAME + "=? AND " + DataContract.Person.COLUMN_LAST_NAME + "=?";
                selectionArgs = new String[]{firstName, lastName};
            }
            //query provider using selection
            Cursor cursor = mContext.getContentResolver()
                    .query(
                            DataContract.Person.CONTENT_URI,
                            null,
                            selection,
                            selectionArgs,
                            null);
            if (cursor != null && cursor.getCount() > 0) {
                //if person already exists
                cursor.moveToFirst();
                //get person ID to be ready to save number
                idPerson = cursor.getLong(0);
            } else {
                //otherwise save person
                ContentValues personRecord = new ContentValues();
                //save first name if exists
                if (!TextUtils.isEmpty(firstName)) {
                    personRecord.put(DataContract.Person.COLUMN_FIRST_NAME, firstName);
                }
                //save last name if exists
                if (!TextUtils.isEmpty(lastName)) {
                    personRecord.put(DataContract.Person.COLUMN_LAST_NAME, lastName);
                }
                Uri newPersonUri = mContext.getContentResolver()
                        .insert(DataContract.Person.CONTENT_URI, personRecord);
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
                parcelable = params[i].getParcelable(DataContract.KEY_IMAGE_BITMAP);
                if (parcelable != null && parcelable instanceof Bitmap) {
                    bitmap = (Bitmap) parcelable;
                    String picPath = mContext.getDir(Utility.PIC_DIR, Context.MODE_PRIVATE).getPath()
                            + File.separator
                            + lastName + "_" + firstName + "_" + idPerson + Utility.EXT;
                    //save pic
                    Utility.storeImageasPNG(bitmap, picPath);
                    //update record with pic path
                    selection = DataContract.Person._ID + "=?";
                    selectionArgs = new String[]{"" + idPerson};
                    ContentValues personRecord = new ContentValues();
                    personRecord.put(DataContract.Person.COLUMN_PIC_PATH, picPath);
                    mContext.getContentResolver().update(DataContract.Person.CONTENT_URI, personRecord, selection, selectionArgs);
                }

                //prepare and save phone data
                ContentValues phoneRecord = new ContentValues();
                phoneRecord.put(DataContract.PhoneNumber.COLUMN_NUMBER, number);
                phoneRecord.put(DataContract.PhoneNumber.COLUMN_KEY_PERSON, idPerson);
                mContext.getContentResolver().insert(DataContract.PhoneNumber.CONTENT_URI, phoneRecord);
            }
            mDoneTaskQty++;
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        if (mDataSetWatcher != null)
            mDataSetWatcher.dataSetChanged(mAllTaskQty == mDoneTaskQty);
    }

    public void setDataSetWatcher(DataSetWatcher mDataSetWatcher) {
        this.mDataSetWatcher = mDataSetWatcher;
    }
}
