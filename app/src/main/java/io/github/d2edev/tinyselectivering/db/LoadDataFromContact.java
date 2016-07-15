package io.github.d2edev.tinyselectivering.db;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

/**
 * Created by d2e on 27.06.16.
 */
public class LoadDataFromContact extends AsyncTaskLoader<Bundle> {
    private Uri mContactURI;
    public LoadDataFromContact(Context context) {
        super(context);
    }



    @Override
    public Bundle loadInBackground() {
        return null;
    }
}
