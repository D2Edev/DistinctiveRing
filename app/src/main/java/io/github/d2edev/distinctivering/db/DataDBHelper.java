package io.github.d2edev.distinctivering.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by d2e on 12.06.16.
 */

public class DataDBHelper extends SQLiteOpenHelper{

    //should update this on any change on data structure
    public static final int DATABASE_VERSION=1;

    //filename to keep db data
    public static final String DATABASE_NAME="TinyDistRing.db";

    public DataDBHelper(Context context){
        super(context,DATABASE_NAME,null,DATABASE_VERSION);
    }

    //Table to keep person data in
    private static final String SQL_CREATE_TABLE_PERSON="CREATE TABLE " +
            DataContract.Person.TABLE_NAME + " ("+
            DataContract.Person._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"+
            DataContract.Person.COLUMN_FIRST_NAME + " TEXT NOT NULL, "+
            DataContract.Person.COLUMN_LAST_NAME + " TEXT NOT NULL, "+
            DataContract.Person.COLUMN_PIC_PATH + " TEXT "+
            ");";
    //Table to keep phone numbers. Each number should relate to corresponding Person record
    //number should be unique
    private static final String SQL_CREATE_TABLE_PHONE_NUMBER="CREATE TABLE " +
            DataContract.PhoneNumber.TABLE_NAME + " ("+
            DataContract.PhoneNumber._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "+
            DataContract.PhoneNumber.COLUMN_KEY_PERSON + " INTEGER NOT NULL, "+
            DataContract.PhoneNumber.COLUMN_NUMBER + " INTEGER UNIQUE NOT NULL, "+
            "FOREIGN KEY (" + DataContract.PhoneNumber.COLUMN_KEY_PERSON+
            " ) REFERENCES " + DataContract.Person.TABLE_NAME + " ("+
            DataContract.Person._ID+ " )"+
            ");";



    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TABLE_PERSON);
        db.execSQL(SQL_CREATE_TABLE_PHONE_NUMBER);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + DataContract.PhoneNumber.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DataContract.Person.TABLE_NAME);
        onCreate(db);
    }
}
