package io.github.d2edev.distinctivering.ui;


import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
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
import io.github.d2edev.distinctivering.db.EntryDeleteTask;
import io.github.d2edev.distinctivering.logic.DataSetWatcher;
import io.github.d2edev.distinctivering.util.Utility;


public class DeleteFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, DataSetWatcher {
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


    public DeleteFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        //prepare to operati with sorting
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
        //set FAB action
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteSelected();
            }
        });
        //get listview
        mListView = (ListView) rootView.findViewById(R.id.listview);
        //header SortBy part init
        ViewGroup headerView = (ViewGroup) inflater.inflate(R.layout.list_numbers_header_item, mListView, false);
        mHeaderTextSortBy = (TextView) headerView.findViewById(R.id.header_text_sort_by);
        mHeaderTextSortBy.setText(mSortBy[mSortTypeIndex]);
        mHeaderTextSortBy.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                headerSortByClicked();
            }
        });
        //header sortOrder part init
        mHeaderSortOrder = (TextView) headerView.findViewById(R.id.header_text_sort_order);
        mHeaderSortOrder.setText(mSortOrder[Utility.getSortOrderIndex(mSortAsc)]);
        mHeaderSortOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                headerSortOrderClicked();
            }
        });
        //adapter init and set
        mAdapter = new NameNumPicListAdapter(getContext(), null, 0);
        mListView.setAdapter(mAdapter);
        mListView.addHeaderView(headerView);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = mAdapter.getCursor();
                //because of header (list positions qty gets +1)
                cursor.moveToPosition(position - 1);
                Integer thisID = cursor.getInt(5);
                //logic implementation - click one selects, next deselects and so on
                if (mAdapter.getSelectedNumIDs().contains(thisID)) {
                    mAdapter.getSelectedNumIDs().remove(thisID);
                } else {
                    mAdapter.getSelectedNumIDs().add(thisID);
                }
                verifyFabAccess();
                mAdapter.notifyDataSetChanged();

            }
        });
        mListView.setEmptyView(rootView.findViewById(R.id.empty_view_text));
        return rootView;
    }

    //hides or shows FAB depending on available recodrs SELECTED
    private void verifyFabAccess() {
        if (mAdapter.getSelectedNumIDs().isEmpty()) {
            mFab.setVisibility(View.GONE);
        } else {
            if (mFab.getVisibility() == View.GONE) mFab.setVisibility(View.VISIBLE);
        }
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

    //rebuilds list based on sort order
    private void rebuildList() {
        //defines sort expression and puts it as argument
        String sortOrder = Utility.getSortColumnName(mSortTypeIndex) + (mSortAsc ? " ASC" : " DESC");
        Bundle bundle = new Bundle();
        bundle.putString(KEY_SORT_ORDER, sortOrder);
        //sets name presentation
        mAdapter.setNameNativeOrder(mSortTypeIndex == Utility.SORT_BY_LAST_NAME ? false : true);
        //rebuilds loader
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
                showHelpDialog();
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void showHelpDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View dialogView = getActivity().getLayoutInflater().inflate(R.layout.help_delete, null, false);
        builder
                .setCancelable(false)
                .setNegativeButton(getString(R.string.dialog_ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .setView(dialogView);
        builder.create().show();
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
            verifyFabAccess();
        if (cursor.getCount() > 0) {
        } else {
            Utility.setDistinctiveRingEnabled(getActivity(), false);
            NotificationManagerCompat.from(getActivity()).cancel(MainActivity.DR_ACTIVE_NOTIFY);

        }

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

//called after selected entries were deleted
    @Override
    public void dataSetChanged(boolean success) {
        //nullify list of selected IDs in adapter
        mAdapter.getSelectedNumIDs().clear();
        Log.d(TAG, "dataSetChanged: ");
        String sortOrder = Utility.getSortColumnName(mSortTypeIndex) + (mSortAsc ? " ASC" : " DESC");
        Bundle bundle = new Bundle();
        bundle.putString(KEY_SORT_ORDER, sortOrder);
        getLoaderManager().restartLoader(LIST_CURSOR_LOADER, bundle, this);

    }

    //forms and launches delete async task
    public void deleteSelected() {
        EntryDeleteTask entryDeleteTask = new EntryDeleteTask(getActivity(), this);
        Integer[] numIDs = new Integer[]{};
        numIDs = mAdapter.getSelectedNumIDs().toArray(numIDs);
        Log.d(TAG, "onClick: " + Arrays.toString(numIDs) + " " + mAdapter.getSelectedNumIDs());
        entryDeleteTask.execute(numIDs);
    }

}
