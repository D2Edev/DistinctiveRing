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
import android.widget.ListView;
import android.widget.Toast;

import io.github.d2edev.distinctivering.R;
import io.github.d2edev.distinctivering.adapters.MainListAdapter;
import io.github.d2edev.distinctivering.db.DataContract;
import io.github.d2edev.distinctivering.db.DataDBHelper;


public class MainListFragment extends Fragment {
public static final String TAG="TAG_MainListFragment";
    private ListView mListView;


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
        View rootView = inflater.inflate(R.layout.fragment_main_list, container, false);
        mListView= (ListView) rootView.findViewById(R.id.listview_allowed_numbers);
        Cursor cursor=getActivity().getContentResolver().query(DataContract.All.CONTENT_URI,null,null,null,null);
        Log.d(TAG, "onCreateView: cursorcount" + cursor.getCount());
        MainListAdapter mainListAdapter=new MainListAdapter(getContext(),cursor,0);
        mListView.setAdapter(mainListAdapter);

        return rootView;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        getActivity().getMenuInflater().inflate(R.menu.main_list_fragment,menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_main_add_item:{
                break;
            }
            case R.id.action_main_delete_item:{
                break;
            }
            case R.id.action_main_add_item_manual:{
                showManualAddDialog();
                break;
            }
            case R.id.action_help:{
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }



    private void showManualAddDialog() {
        DialogFragment manualAddDialogFragment = new ManualAddDialogFragment();
        manualAddDialogFragment.show(getActivity().getSupportFragmentManager(),ManualAddDialogFragment.TAG);


    }



}
