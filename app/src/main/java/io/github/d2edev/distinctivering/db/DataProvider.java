package io.github.d2edev.distinctivering.db;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;

/**
 * Created by d2e on 14.06.16.
 */

public class DataProvider extends ContentProvider {
    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();

    static final int ALL_NUMBERS = 100;
    static final int NUMBER_BY_ID = 101;
    static final int NUMBER_BY_VALUE = 102;
    static final int ALL_PERSONS = 200;
    static final int PERSON_BY_ID = 201;
    static final int ALL_PERSONS_WITH_NUMBERS = 300;

    private DataDBHelper mDBHelper;


    private static UriMatcher buildUriMatcher() {
        //matcher with default match
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = DataContract.CONTENT_AUTHORITY;
        //match for content://<authority>/number/ - all numbers listed
        matcher.addURI(authority, DataContract.PATH_NUMBER, ALL_NUMBERS);
        //match for content://<authority>/number/id/<#ID> - selected number listed by ID
        matcher.addURI(authority, DataContract.PATH_NUMBER + "/" + DataContract.PhoneNumber.BY_ID + "/#", NUMBER_BY_ID);
        //match for content://<authority>/number/value/<#VALUE> - selected number listed by VALUE
        matcher.addURI(authority, DataContract.PATH_NUMBER + "/" + DataContract.PhoneNumber.BY_VALUE + "/*", NUMBER_BY_VALUE);
        //match for content://<authority>/person/ - all persons listed
        matcher.addURI(authority, DataContract.PATH_PERSON, ALL_PERSONS);
        //match for content://<authority>/person/<#ID> - selected person listed
        matcher.addURI(authority, DataContract.PATH_PERSON + "/#", PERSON_BY_ID);
        //match for content://<authority>/all/ - all persons with numbers listed
        matcher.addURI(authority, DataContract.PATH_ALL, ALL_PERSONS_WITH_NUMBERS);
        return matcher;
    }


    @Override
    public boolean onCreate() {
        mDBHelper = new DataDBHelper(getContext());
        return true;
    }


    // Here's the switch statement that, given a URI, will determine what kind of request it is,
    // and query the database accordingly.
    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor cursor = null;
        switch (sUriMatcher.match(uri)) {
            case ALL_PERSONS: {
                cursor = getPersonCursor(projection, selection, selectionArgs, sortOrder);
                break;
            }
            case PERSON_BY_ID: {
                String mySelection = DataContract.Person._ID + "=?";
                String[] mySelectionArgs = new String[]{DataContract.Person.getPersonIdFromUri(uri)};
                cursor = getPersonCursor(projection, mySelection, mySelectionArgs, sortOrder);
                break;
            }
            case ALL_NUMBERS: {
                cursor = getNumberCursor(projection, selection, selectionArgs, sortOrder);
                break;
            }
            case NUMBER_BY_ID: {
                String mySelection = DataContract.PhoneNumber._ID + "=?";
                String[] mySelectionArgs = new String[]{DataContract.PhoneNumber.getArgumentFromUri(uri)};
                cursor = getNumberCursor(projection, mySelection, mySelectionArgs, sortOrder);
                break;
            }
            case NUMBER_BY_VALUE: {
                String mySelection = DataContract.PhoneNumber.COLUMN_NUMBER + "=?";
                String[] mySelectionArgs = new String[]{DataContract.PhoneNumber.getArgumentFromUri(uri)};
                cursor = getNumberCursor(projection, mySelection, mySelectionArgs, sortOrder);
                break;
            }
            case ALL_PERSONS_WITH_NUMBERS: {
                cursor = getAllCursor(projection, selection, selectionArgs, sortOrder);
                break;
            }
            default:
                throw new UnsupportedOperationException("Query: unknown uri " + uri);
        }


        return cursor;
    }


    //method of getting phone number data from db
    private Cursor getNumberCursor(String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor cursor = null;
        cursor = mDBHelper.getReadableDatabase().query(
                DataContract.PhoneNumber.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
        return cursor;
    }

    //method of getting person data from db
    private Cursor getPersonCursor(String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor cursor = null;
        cursor = mDBHelper.getReadableDatabase().query(
                DataContract.Person.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
        return cursor;
    }

    //method of getting all data from db
    private Cursor getAllCursor(String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor cursor = null;
        String sqlQuery = DataDBHelper.SQL_QUERY_ALL_PERSONS_NUMBERS;
        //check if sort order is added and it's more or less ok
        if (sortOrder != null && !sortOrder.equals("")) sqlQuery = sqlQuery + " ORDER BY " + sortOrder;
        cursor = mDBHelper.getReadableDatabase().rawQuery(sqlQuery, selectionArgs);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        // Use the Uri Matcher to determine what kind of URI this is.
        switch (sUriMatcher.match(uri)) {
            case ALL_NUMBERS:
                return DataContract.PhoneNumber.CONTENT_TYPE;
            case NUMBER_BY_ID:
                return DataContract.PhoneNumber.CONTENT_ITEM_TYPE;
            case NUMBER_BY_VALUE:
                return DataContract.PhoneNumber.CONTENT_ITEM_TYPE;
            case ALL_PERSONS:
                return DataContract.Person.CONTENT_TYPE;
            case PERSON_BY_ID:
                return DataContract.Person.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("GetType: unknown uri " + uri);
        }

    }


    //insert is supported only for "base" URIs as IDs are defined by DB
    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mDBHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri = null;

        long id = 0;
        switch (sUriMatcher.match(uri)) {
            case ALL_PERSONS: {
                id = db.insert(DataContract.Person.TABLE_NAME, null, values);
                if (id > 0) {
                    returnUri = DataContract.Person.builPersonUri(id);
                } else {
                    throw new SQLException("Failed to insert data for " + uri);
                }
                break;
            }
            case ALL_NUMBERS: {
                id = db.insert(DataContract.PhoneNumber.TABLE_NAME, null, values);
                if (id > 0) {
                    returnUri = DataContract.PhoneNumber.buildPhoneNumberUriByID(id);
                } else {
                    throw new SQLException("Failed to insert data for " + uri);
                }
                break;
            }
            default:
                throw new UnsupportedOperationException("Insert: unknown uri " + uri);
        }
        if (id > 0) getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    //delete work fo all URIs
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mDBHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted = 0;

        switch (match) {
            case ALL_PERSONS: {
                rowsDeleted = db.delete(DataContract.Person.TABLE_NAME, selection, selectionArgs);
                break;
            }
            case PERSON_BY_ID: {
                String mySelection = DataContract.Person._ID + "=?";
                String[] mySelectioArgs = new String[]{DataContract.Person.getPersonIdFromUri(uri)};
                rowsDeleted = db.delete(DataContract.Person.TABLE_NAME, mySelection, mySelectioArgs);
                break;
            }
            case ALL_NUMBERS: {
                rowsDeleted = db.delete(DataContract.PhoneNumber.TABLE_NAME, selection, selectionArgs);
                break;
            }
            case NUMBER_BY_ID: {
                String mySelection = DataContract.PhoneNumber._ID + "=?";
                String[] mySelectioArgs = new String[]{DataContract.PhoneNumber.getArgumentFromUri(uri)};
                rowsDeleted = db.delete(DataContract.PhoneNumber.TABLE_NAME, mySelection, mySelectioArgs);
                break;
            }
            case NUMBER_BY_VALUE: {
                String mySelection = DataContract.PhoneNumber.COLUMN_NUMBER + "=?";
                String[] mySelectioArgs = new String[]{DataContract.PhoneNumber.getArgumentFromUri(uri)};
                rowsDeleted = db.delete(DataContract.PhoneNumber.TABLE_NAME, mySelection, mySelectioArgs);
                break;
            }
            default:
                throw new UnsupportedOperationException("Delete: unknown uri " + uri);
        }
        //notify only in case deletion really occurs
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    //Update works for "base" URIs as well for value-based number URI (as we can't update IDs)
    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mDBHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated = 0;

        switch (match) {
            case ALL_PERSONS: {
                rowsUpdated = db.update(DataContract.Person.TABLE_NAME, values, selection, selectionArgs);
                break;
            }
            case ALL_NUMBERS: {
                rowsUpdated = db.update(DataContract.PhoneNumber.TABLE_NAME, values, selection, selectionArgs);
                break;
            }
            case NUMBER_BY_VALUE: {
                String mySelection = DataContract.PhoneNumber.COLUMN_NUMBER + "=?";
                String[] mySelectioArgs = new String[]{DataContract.PhoneNumber.getArgumentFromUri(uri)};
                rowsUpdated = db.update(DataContract.PhoneNumber.TABLE_NAME, values, mySelection, mySelectioArgs);
                break;
            }
            default:
                throw new UnsupportedOperationException("Update: unknown uri" + uri);
        }
        //notify only in case update really occurs
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }



    @Override
    public void shutdown() {
        mDBHelper.close();
        super.shutdown();
    }
}
