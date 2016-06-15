package io.github.d2edev.distinctivering.ui;


import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import io.github.d2edev.distinctivering.R;
import io.github.d2edev.distinctivering.db.DataContract;
import io.github.d2edev.distinctivering.db.DataDBHelper;


public class MainListFragment extends Fragment {
public static final String TAG="TAG_MainListFragment";


    public MainListFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_main_list, container, false);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        getActivity().getMenuInflater().inflate(R.menu.main_list_fragment,menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_main_add_item:{
                Toast.makeText(getActivity(),getString(R.string.action_main_add_title),Toast.LENGTH_SHORT).show();
                stubAdd();
                break;
            }
            case R.id.action_main_delete_item:{
                Toast.makeText(getActivity(),getString(R.string.action_menu_main_delete_title),Toast.LENGTH_SHORT).show();
                break;
            }
            case R.id.action_main_add_item_manual:{
                Toast.makeText(getActivity(),getString(R.string.action_add_manual_title),Toast.LENGTH_SHORT).show();
                showManualAddDialog();
                break;
            }
            case R.id.action_help:{
                Toast.makeText(getActivity(),getString(R.string.action_menu_main_help_title),Toast.LENGTH_SHORT).show();
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void stubAdd() {
        DataDBHelper dbHelper = new DataDBHelper(getActivity());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cvPersons = new ContentValues();
        ContentValues cvPersons1 = new ContentValues();
        cvPersons.put(DataContract.Person.COLUMN_FIRST_NAME, "Kote");
        cvPersons.put(DataContract.Person.COLUMN_LAST_NAME, "Maharadze");
        cvPersons1.put(DataContract.Person.COLUMN_FIRST_NAME, "Dab");
        cvPersons1.put(DataContract.Person.COLUMN_LAST_NAME, "Bunnkle");
        db.insert(DataContract.Person.TABLE_NAME,null,cvPersons);
        db.insert(DataContract.Person.TABLE_NAME,null,cvPersons1);
        Cursor c = db.query(
                DataContract.Person.TABLE_NAME,
                null,
                DataContract.Person.COLUMN_FIRST_NAME+"=? AND "
                        + DataContract.Person.COLUMN_LAST_NAME + "=?",
                new String[]{"Kote","Maharadze"},
                null,
                null,
                null
        );
        int recId=0;
        if(c.getCount()>0){
            c.moveToLast();
            recId=c.getInt(0);
        }
        Toast.makeText(getActivity(),""+recId,Toast.LENGTH_SHORT).show();
        ContentValues phoneVal= new ContentValues();
       phoneVal.put(DataContract.PhoneNumber.COLUMN_KEY_PERSON,recId);
        phoneVal.put(DataContract.PhoneNumber.COLUMN_NUMBER,"1234567890");
        db.insert(DataContract.PhoneNumber.TABLE_NAME,null,phoneVal);

        db.close();
        db=dbHelper.getReadableDatabase();
      String myQuery = "SELECT "
                + DataContract.Person.COLUMN_FIRST_NAME + ", "
                + DataContract.Person.COLUMN_LAST_NAME + ", "
                + DataContract.Person.COLUMN_PIC_PATH + ", "
                + DataContract.PhoneNumber.COLUMN_NUMBER + ", "
                + DataContract.Person.TABLE_NAME + "." + DataContract.Person._ID+ ", "
                + DataContract.PhoneNumber.TABLE_NAME + "." + DataContract.PhoneNumber._ID
                + " FROM " + DataContract.Person.TABLE_NAME
                + " INNER JOIN " + DataContract.PhoneNumber.TABLE_NAME
                + " ON " + DataContract.Person.TABLE_NAME + "." + DataContract.Person._ID
                + " = " + DataContract.PhoneNumber.TABLE_NAME + "."+ DataContract.PhoneNumber.COLUMN_KEY_PERSON;

        Cursor cursor = db.rawQuery(myQuery,null);
        Log.d(TAG, "stubAdd: " +myQuery);

        if(cursor.getCount()>0){
            cursor.moveToLast();
//            Toast.makeText(getActivity(),cursor.getString(0)+" "+cursor.getLong(1),Toast.LENGTH_SHORT).show();
            Toast.makeText(getActivity(),""+cursor.getColumnCount(),Toast.LENGTH_SHORT).show();
        }
        cursor.close();
        db.close();
    }

    private void showManualAddDialog() {
        DialogFragment manualAddDialogFragment = new ManualAddDialogFragment();
        manualAddDialogFragment.show(getActivity().getSupportFragmentManager(),ManualAddDialogFragment.TAG);


    }

    private boolean dialogInputOK(View dialogView) {

        return false;
    }


}
