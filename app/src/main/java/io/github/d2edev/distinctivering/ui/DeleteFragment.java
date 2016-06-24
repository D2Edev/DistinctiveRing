package io.github.d2edev.distinctivering.ui;


import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Arrays;

import io.github.d2edev.distinctivering.R;
import io.github.d2edev.distinctivering.adapters.NameNumPicListAdapter;
import io.github.d2edev.distinctivering.db.DataContract;
import io.github.d2edev.distinctivering.db.NumberDeleteTask;
import io.github.d2edev.distinctivering.logic.DataSetWatcher;
import io.github.d2edev.distinctivering.util.Utility;


public class DeleteFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, DataSetWatcher{
    public static final String TAG = "TAG_" + DeleteFragment.class.getSimpleName();
    public static final String KEY_SORT_ORDER = "kso";
    public static final int LIST_CURSOR_LOADER = 0;
    private String[] mSortBy;
    private String[] mSortOrder;
    private ListView mListView;
    private TextView mHeaderTextSortBy;
    private TextView mHeaderSortOrder;
    private int mSortTypeIndex;
    private boolean mSortAsc;
    private NameNumPicListAdapter mAdapter;
    private FloatingActionButton mFab;
    private BasicActionsListener mBasicActionsListener;


//    TODO think about correct deleting persons with no numbers
//    TODO FAB enable/disable

    public DeleteFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mSortBy = getResources().getStringArray(R.array.sortBy);
        mSortOrder = getResources().getStringArray(R.array.sortOrder);
        mSortTypeIndex = Utility.getSortTypeIndex(getActivity());
        mSortAsc = Utility.isSortOrderAscending(getActivity());


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_delete, container, false);
        mFab = (FloatingActionButton) rootView.findViewById(R.id.fab);
        mFab.setEnabled(false);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteSelected();
            }
        });
        mListView = (ListView) rootView.findViewById(R.id.listview);
        ViewGroup headerView = (ViewGroup) inflater.inflate(R.layout.list_numbers_header_item, mListView, false);
        //header SortBy part init
        mHeaderTextSortBy = (TextView) headerView.findViewById(R.id.header_text_sort_by);
        mHeaderTextSortBy.setText(mSortBy[mSortTypeIndex]);
        mHeaderTextSortBy.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                headerSortByClicked();
            }
        });
        //header soerOrder part init
        mHeaderSortOrder = (TextView) headerView.findViewById(R.id.header_text_sort_order);
        mHeaderSortOrder.setText(mSortOrder[Utility.getSortOrderIndex(mSortAsc)]);
        mHeaderSortOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                headerSortOrderClicked();
            }
        });
        mAdapter = new NameNumPicListAdapter(getContext(), null, 0);
        mListView.setAdapter(mAdapter);
        mListView.addHeaderView(headerView);
        mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = mAdapter.getCursor();
                cursor.moveToPosition(position - 1);
                Integer thisID = new Integer(cursor.getInt(5));
                if (mAdapter.getSelectedNumIDs().contains(thisID)) {
                    mAdapter.getSelectedNumIDs().remove(thisID);
                } else {
                    mAdapter.getSelectedNumIDs().add(thisID);
                }
                mFab.setEnabled(!mAdapter.getSelectedNumIDs().isEmpty());
                mAdapter.notifyDataSetChanged();

            }
        });


        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        rebuildList();
        super.onActivityCreated(savedInstanceState);
    }

    private void headerSortOrderClicked() {
        mSortAsc = mSortAsc ? false : true;
        mHeaderSortOrder.setText(mSortOrder[Utility.getSortOrderIndex(mSortAsc)]);
        rebuildList();

    }

    private void headerSortByClicked() {
        if (mSortTypeIndex == 2) {
            mSortTypeIndex = 0;
        } else {
            mSortTypeIndex++;
        }
        mHeaderTextSortBy.setText(mSortBy[mSortTypeIndex]);
        rebuildList();
    }

    private void rebuildList() {
        String sortOrder = Utility.getSortColumnName(mSortTypeIndex) + (mSortAsc ? " ASC" : " DESC");
        mAdapter.setNameNativeOrder(mSortTypeIndex == Utility.SORT_BY_LAST_NAME?false:true);
        Bundle bundle = new Bundle();
        bundle.putString(KEY_SORT_ORDER, sortOrder);
        if (getLoaderManager().getLoader(LIST_CURSOR_LOADER) != null)
            getLoaderManager().destroyLoader(LIST_CURSOR_LOADER);
        getLoaderManager().initLoader(LIST_CURSOR_LOADER, bundle, this);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        getActivity().getMenuInflater().inflate(R.menu.delete_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {


            case R.id.action_help: {
//                TODO code help screen
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }



    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String sortSQL = null;
        if (args != null && args.containsKey(KEY_SORT_ORDER))
            sortSQL = args.getString(KEY_SORT_ORDER);
        return new CursorLoader(getActivity(), DataContract.All.CONTENT_URI, null, null, null, sortSQL);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mAdapter.swapCursor(cursor);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    @Override
    public void onPause() {
        Utility.setSortTypeIndex(getActivity(), mSortTypeIndex);
        Utility.setSortOrderAscending(getActivity(), mSortAsc);
        super.onPause();
    }


    @Override
    public void dataSetChanged() {
        Log.d(TAG, "dataSetChanged: ");
        String sortOrder = Utility.getSortColumnName(mSortTypeIndex) + (mSortAsc ? " ASC" : " DESC");
        Bundle bundle = new Bundle();
        bundle.putString(KEY_SORT_ORDER, sortOrder);
        getLoaderManager().restartLoader(LIST_CURSOR_LOADER, bundle, this);

    }


    public void deleteSelected() {
        NumberDeleteTask numberDeleteTask = new NumberDeleteTask(getActivity(), this);
        Integer[] numIDs = new Integer[]{};
        numIDs= mAdapter.getSelectedNumIDs().toArray(numIDs);
        Log.d(TAG, "onClick: "+ Arrays.toString(numIDs)+ " " + mAdapter.getSelectedNumIDs());
        numberDeleteTask.execute(numIDs);
    }

    public void setBasicActionsListener(BasicActionsListener basicActionsListener) {
        mBasicActionsListener = basicActionsListener;
    }
}
