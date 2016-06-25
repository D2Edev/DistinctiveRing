package io.github.d2edev.distinctivering.db;

import android.content.Context;
import android.os.AsyncTask;

import io.github.d2edev.distinctivering.logic.DataSetWatcher;


/**
 * Created by d2e on 19.06.16.
 */

public class NumberDeleteTask extends AsyncTask<Integer, Void, Integer> {

    private Context context;
    private DataSetWatcher dataSetWatcher;
    private int deletedRows;

    public NumberDeleteTask(Context context, DataSetWatcher dataSetWatcher) {
        this.context = context;
        this.dataSetWatcher = dataSetWatcher;
    }


    @Override
    protected Integer doInBackground(Integer... params) {
        for (int i = 0; i < params.length; i++) {
            deletedRows += context.getContentResolver()
                    .delete(
                            DataContract.PhoneNumber.buildPhoneNumberUriByID(params[i]),
                            null,
                            null);
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
