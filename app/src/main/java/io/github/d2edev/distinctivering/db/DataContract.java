package io.github.d2edev.distinctivering.db;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by d2e on 10.06.16.
 */

public class DataContract {
    // name for the entire content provider which needs to be unique on the device.
    public static final String CONTENT_AUTHORITY = "io.github.d2edev.distinctivering";
    // using CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
    // the content provider.
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    //paths (appended to base content URI for possible URI's)
    public static final String PATH_NUMBER = "number";
    public static final String PATH_PERSON = "person";
    public static final String PATH_ALL = "all";

    public static final class Person implements BaseColumns{
        //base URI for person
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_PERSON).build();
        //definition for base MIME type for a content: URI containing a Cursor of zero or more items.
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PERSON;
        //definition for base MIME type for a content: URI containing a Cursor of a single item.
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PERSON;
        //defining table name and columns
        public static final String TABLE_NAME="person";
        public static final String COLUMN_FIRST_NAME="first_name";
        public static final String COLUMN_LAST_NAME="last_name";
        public static final String COLUMN_PIC_PATH="pic_path";

        public static Uri builPersonUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static String getPersonIdFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }
    }

    public static final class PhoneNumber implements BaseColumns{
        //base URI for person
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_NUMBER).build();
        //definition for base MIME type for a content: URI containing a Cursor of zero or more items.
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_NUMBER;
        //definition for base MIME type for a content: URI containing a Cursor of a single item.
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_NUMBER;
        //path segment for getting selected number by ID
        public static final String BY_ID="id";
        //path segment for getting selected number by VALUE
        public static final String BY_VALUE="value";

        public static final String TABLE_NAME="phone_number";
        public static final String COLUMN_NUMBER ="number";
        public static final String COLUMN_KEY_PERSON="id_person";

        public static Uri buildPhoneNumberUriByID(long id) {
            Uri idURI=CONTENT_URI.buildUpon().appendPath(BY_ID).build();
            return ContentUris.withAppendedId(idURI, id);
        }

        public static Uri buildPhoneNumberUriByValue(long id) {
            Uri valueURI=CONTENT_URI.buildUpon().appendPath(BY_VALUE).build();
            return ContentUris.withAppendedId(valueURI, id);
        }

        //gets argument from URI, whether it is a number id (".../number/id/<arg>")
        // or value (".../number/id/<value>")
        public static String getArgumentFromUri(Uri uri) {
            return uri.getPathSegments().get(2);
        }
    }

    public  static  final class All{
        //base URI for all
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_ALL).build();
        //definition for base MIME type for a content: URI containing a Cursor of zero or more items.
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ALL;
    }


}
