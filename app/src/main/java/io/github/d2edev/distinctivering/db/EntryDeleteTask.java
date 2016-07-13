package io.github.d2edev.distinctivering.db;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.TextUtils;

import java.io.File;

import io.github.d2edev.distinctivering.logic.DataSetWatcher;


/**
 * Created by d2e on 19.06.16.
 */

public class EntryDeleteTask extends AsyncTask<Integer, Void, Integer> {

    private Context mContext;
    private DataSetWatcher dataSetWatcher;
    private int deletedRows;

    public EntryDeleteTask(Context mContext, DataSetWatcher dataSetWatcher) {
        this.mContext = mContext;
        this.dataSetWatcher = dataSetWatcher;
    }


    @Override
    protected Integer doInBackground(Integer... params) {
        for (int i = 0; i < params.length; i++) {
            //create phone address
            Uri phoneUri = DataContract.PhoneNumber.buildPhoneNumberUriByID(params[i]);
            //creates cursor based on phone address,gets person ID for phone number,
            long personID = -1;
            boolean lastNumForPerson = false;
            String[] projectionCID = new String[]{DataContract.PhoneNumber.COLUMN_KEY_PERSON};
            Cursor numCursor = mContext.getContentResolver().query(
                    phoneUri,
                    projectionCID,
                    null,
                    null, null);
            if (numCursor != null && numCursor.moveToFirst()) {
                personID = numCursor.getLong(0);
            }
            numCursor.close();
            //checks whether current number is the last for person
            String selection = DataContract.PhoneNumber.COLUMN_KEY_PERSON + "=?";
            String[] selectionArgs = new String[]{"" + personID};
            numCursor = mContext.getContentResolver().query(
                    DataContract.PhoneNumber.CONTENT_URI,
                    null,
                    selection,
                    selectionArgs,
                    null);
            if (numCursor.getCount() == 1) lastNumForPerson = true;
            numCursor.close();
            //deletes number record from phone table
            deletedRows += mContext.getContentResolver()
                    .delete(
                            phoneUri,
                            null,
                            null);
            //if deleted ok, and number was last - delete pic
            if (deletedRows > 0 && personID > -1 && lastNumForPerson) {
                Uri personURI = DataContract.Person.builPersonUri(personID);
                String[] projectionPicPath = new String[]{DataContract.Person.COLUMN_PIC_PATH};
                Cursor picPathCursor = mContext.getContentResolver().query(
                        personURI,
                        projectionPicPath,
                        null,
                        null,
                        null
                );
                if (picPathCursor != null && picPathCursor.moveToFirst()) {
                    String picPath = picPathCursor.getString(0);
                    if (!TextUtils.isEmpty(picPath)) {
                        File file = new File(picPath);
                        file.delete();
                    }
                }
                picPathCursor.close();
                mContext.getContentResolver().delete(personURI, null, null);
            }
        }


        return deletedRows;
    }

    @Override
    protected void onPostExecute(Integer integer) {
        if (deletedRows > 0 && dataSetWatcher != null) {
            dataSetWatcher.dataSetChanged(deletedRows > 0 ? true : false);
        }
    }
}
