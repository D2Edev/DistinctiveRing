package io.github.d2edev.distinctivering;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;


import io.github.d2edev.distinctivering.db.DataContract;
import io.github.d2edev.distinctivering.db.DataDBHelper;

import static org.junit.Assert.*;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */

@RunWith(AndroidJUnit4.class)
public class TestProvider {
    public static final String TAG = "TAG_" + TestProvider.class.getSimpleName();
    private Context appContext = InstrumentationRegistry.getTargetContext();
    private DataDBHelper dbHelper;


    @Before
    public void setUp() {
        dbHelper = new DataDBHelper(appContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        dbHelper.reinit(db);
        db.close();


    }

    @Test
    public void queryEmptyPerson() {
        Cursor cursor = appContext.getContentResolver().query(DataContract.Person.CONTENT_URI,null,null,null,null);
        if(cursor!=null){
        assertEquals(cursor.getCount(),0);
        }
    }

    @Test
    public void addPersonAndQuery(){
        ContentValues personData = new ContentValues();
        personData.put(DataContract.Person.COLUMN_FIRST_NAME, "FirstName" );
        personData.put(DataContract.Person.COLUMN_LAST_NAME, "LastName" );
        personData.put(DataContract.Person.COLUMN_PIC_PATH, "/pic/003.jpg" );
        //add data
        Uri retURI =appContext.getContentResolver().insert(DataContract.Person.CONTENT_URI,personData);
        //check add is ok
        assertNotNull(retURI);
        //check data integtity
        Cursor cursor=appContext.getContentResolver().query(retURI,null,null,null,null);
        assertNotNull(cursor);
        assertTrue(cursor.moveToFirst());
        assertEquals(cursor.getString(1),"FirstName" );
        assertEquals(cursor.getString(2),"LastName" );
        assertEquals(cursor.getString(3),"/pic/003.jpg" );
        //check helper func
        assertTrue(Long.parseLong(DataContract.Person.getPersonIdFromUri(retURI))>0);
        cursor.close();
        //check delete works ok
        assertEquals(appContext.getContentResolver().delete(retURI,null,null),1);
        //generate 3 stub values
        personData = new ContentValues();
        personData.put(DataContract.Person.COLUMN_FIRST_NAME, "John" );
        personData.put(DataContract.Person.COLUMN_LAST_NAME, "Smith" );
        personData.put(DataContract.Person.COLUMN_PIC_PATH, "/pic/003.jpg" );
        appContext.getContentResolver().insert(DataContract.Person.CONTENT_URI,personData);
        personData=new ContentValues();
        personData.put(DataContract.Person.COLUMN_FIRST_NAME, "Peter" );
        personData.put(DataContract.Person.COLUMN_LAST_NAME, "Green" );
        personData.put(DataContract.Person.COLUMN_PIC_PATH, "/pic/006.jpg" );
        appContext.getContentResolver().insert(DataContract.Person.CONTENT_URI,personData);
        personData=new ContentValues();
        personData.put(DataContract.Person.COLUMN_FIRST_NAME, "John" );
        personData.put(DataContract.Person.COLUMN_LAST_NAME, "Lennon" );
        personData.put(DataContract.Person.COLUMN_PIC_PATH, "/pic/004.jpg" );
        appContext.getContentResolver().insert(DataContract.Person.CONTENT_URI,personData);
        //check person
        Cursor personCursor=appContext.getContentResolver().query(DataContract.Person.CONTENT_URI,null,null,null,null);
        //should not be null
        assertNotNull(personCursor);
        //should contain 2 records
        assertTrue(personCursor.getCount()==3);
        personCursor.close();
        String mySelection=DataContract.Person.COLUMN_FIRST_NAME+"=?";
        String []mySelectionArgs = new String[]{"Peter"};
        personCursor=appContext.getContentResolver().query(DataContract.Person.CONTENT_URI,null,mySelection,mySelectionArgs,null);
        //should not be null
        assertNotNull(personCursor);
        //should contain 1 record
        assertTrue(personCursor.getCount()==1);
        personCursor.close();
        mySelection=DataContract.Person.COLUMN_FIRST_NAME+"=? AND " + DataContract.Person.COLUMN_LAST_NAME +"=?";
        mySelectionArgs = new String[]{"John","Lennon"};
        personCursor=appContext.getContentResolver().query(DataContract.Person.CONTENT_URI,null,mySelection,mySelectionArgs,null);
        //should not be null
        assertNotNull(personCursor);
        //should contain 1 record
        assertTrue(personCursor.getCount()==1);
        personCursor.close();
        //check delete works ok
        assertEquals(appContext.getContentResolver().delete(DataContract.Person.CONTENT_URI,null,null),3);

    }


    @Test
    public void addNumberAndQuery(){
        Uri retPersonUri;
        Uri retNumberUri;
        Uri checkUri;
        long id;
        ContentValues personData;
        ContentValues numberData;
        Cursor checkCursor;
        //generate 3 stub values
        personData = new ContentValues();
        personData.put(DataContract.Person.COLUMN_FIRST_NAME, "John" );
        personData.put(DataContract.Person.COLUMN_LAST_NAME, "Smith" );
        personData.put(DataContract.Person.COLUMN_PIC_PATH, "/pic/003.jpg" );
        //generate stub number for this person
        retPersonUri = appContext.getContentResolver().insert(DataContract.Person.CONTENT_URI,personData);
        id=Long.parseLong(DataContract.Person.getPersonIdFromUri(retPersonUri));
        numberData=new ContentValues();
        numberData.put(DataContract.PhoneNumber.COLUMN_KEY_PERSON,id);
        numberData.put(DataContract.PhoneNumber.COLUMN_NUMBER,"33344455566");
        retNumberUri=appContext.getContentResolver().insert(DataContract.PhoneNumber.CONTENT_URI,numberData);
        assertNotNull(retNumberUri);
        assertTrue(Long.parseLong(DataContract.PhoneNumber.getArgumentFromUri(retNumberUri))>0);
        personData=new ContentValues();
        personData.put(DataContract.Person.COLUMN_FIRST_NAME, "John" );
        personData.put(DataContract.Person.COLUMN_LAST_NAME, "Lennon" );
        personData.put(DataContract.Person.COLUMN_PIC_PATH, "/pic/004.jpg" );
        //generate stub number for this person
        retPersonUri = appContext.getContentResolver().insert(DataContract.Person.CONTENT_URI,personData);
        id=Long.parseLong(DataContract.Person.getPersonIdFromUri(retPersonUri));
        numberData=new ContentValues();
        numberData.put(DataContract.PhoneNumber.COLUMN_KEY_PERSON,id);
        numberData.put(DataContract.PhoneNumber.COLUMN_NUMBER,"33344455577");
        retNumberUri=appContext.getContentResolver().insert(DataContract.PhoneNumber.CONTENT_URI,numberData);
        assertNotNull(retNumberUri);
        assertTrue(Long.parseLong(DataContract.PhoneNumber.getArgumentFromUri(retNumberUri))>0);
        personData=new ContentValues();
        personData.put(DataContract.Person.COLUMN_FIRST_NAME, "Peter" );
        personData.put(DataContract.Person.COLUMN_LAST_NAME, "Green" );
        personData.put(DataContract.Person.COLUMN_PIC_PATH, "/pic/006.jpg" );
        appContext.getContentResolver().insert(DataContract.Person.CONTENT_URI,personData);
        //now we have two personds with numbers and one without
        checkCursor = appContext.getContentResolver().query(DataContract.PhoneNumber.CONTENT_URI,null,null,null,null);
        //cursor nit null?
        assertNotNull(checkCursor);
        //should have 2 rows
        assertTrue(checkCursor.getCount()==2);
        //moving is ok
        assertTrue(checkCursor.moveToFirst());
        //col 0 is id
        id=checkCursor.getLong(0);
        assertTrue(id>0);
        //moves ok
        assertTrue(checkCursor.moveToNext());
        //col 2 is number
        String number=checkCursor.getString(2);
        assertEquals(number,"33344455577");
        checkCursor.close();
        //check if provider correctly returns data for number ID uri
        checkUri=DataContract.PhoneNumber.buildPhoneNumberUriByID(id);
        checkCursor=appContext.getContentResolver().query(checkUri,null,null,null,null);
        //cursor nit null?
        assertNotNull(checkCursor);
        //should have 1 row
        assertTrue(checkCursor.getCount()==1);
        //moving is ok
        assertTrue(checkCursor.moveToFirst());
        //checks IDs
        assertEquals(id,checkCursor.getLong(0));
        checkCursor.close();
        //check if provider correctly returns data for number itself uri
        checkUri=DataContract.PhoneNumber.buildPhoneNumberUriByValue(number);
        checkCursor=appContext.getContentResolver().query(checkUri,null,null,null,null);
        //cursor nit null?
        assertNotNull(checkCursor);
        //should have 1 row
        assertTrue(checkCursor.getCount()==1);
        //moving is ok
        assertTrue(checkCursor.moveToFirst());
        //checks Numbers
        assertEquals("33344455577",checkCursor.getString(2));
        checkCursor.close();
        //check all records request
        checkCursor=appContext.getContentResolver().query(DataContract.All.CONTENT_URI,null,null,null,null);
        //cursor nit null?
        assertNotNull(checkCursor);
        //should have 2 rows
        assertTrue(checkCursor.getCount()==2);
        //moving is ok
        assertTrue(checkCursor.moveToFirst());
        //checks Numbers
    }




    @After
    public void setDown(){
        dbHelper.close();
    }
}