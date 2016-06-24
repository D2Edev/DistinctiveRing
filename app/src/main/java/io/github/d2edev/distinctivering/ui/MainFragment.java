package io.github.d2edev.distinctivering.ui;


import android.app.Activity;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

import io.github.d2edev.distinctivering.R;
import io.github.d2edev.distinctivering.adapters.NameNumPicListAdapter;
import io.github.d2edev.distinctivering.db.DataContract;
import io.github.d2edev.distinctivering.logic.DataSetWatcher;
import io.github.d2edev.distinctivering.util.Utility;


public class MainFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, DataSetWatcher {
    public static final String TAG = "TAG_" + MainFragment.class.getSimpleName();
    public static final String KEY_SORT_ORDER = "kso";
    public static final int ALLOWEDLIST_CURSOR_LOADER = 0;
    public static final int REQUEST_SELECT_PHONE_NUMBER = 101;
    private FloatingActionButton mFab;
    private String[] mSortBy;
    private String[] mSortOrder;
    private ListView mListView;
    private TextView mHeaderTextSortBy;
    private TextView mHeaderSortOrder;
    private int mSortTypeIndex;
    private boolean mSortAsc;
    private boolean mHasRecords;
    private NameNumPicListAdapter mAdapter;
    private BasicActionsListener basicActionsListener;


    public void setBasicActionsListener(BasicActionsListener basicActionsListener) {
        this.basicActionsListener = basicActionsListener;
    }

    public MainFragment() {
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
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        mFab = (FloatingActionButton) rootView.findViewById(R.id.fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeDistinctiveRingSettings();

            }
        });
        mFab.setImageResource(Utility.isDistinctiveRingEnabled(getActivity()) ? R.drawable.ic_volume_up_white : R.drawable.ic_volume_off_white);
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

        mListView.addHeaderView(headerView);


        mAdapter = new NameNumPicListAdapter(getContext(), null, 0);
        mListView.setAdapter(mAdapter);

        return rootView;
    }


    //process click
    private void changeDistinctiveRingSettings() {
        if (Utility.isDistinctiveRingEnabled(getActivity())) {

            Utility.setDistinctiveRingEnabled(getActivity(), false);
            mFab.setImageResource(R.drawable.ic_volume_off_white);

        } else {
            Utility.setDistinctiveRingEnabled(getActivity(), true);
            mFab.setImageResource(R.drawable.ic_volume_up_white);

        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        rebuildList();
        super.onActivityCreated(savedInstanceState);
    }

    private void headerSortOrderClicked() {
        Log.d(TAG, "order clicked");
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
        Log.d(TAG, "criteria clicked " + mSortTypeIndex);
        mHeaderTextSortBy.setText(mSortBy[mSortTypeIndex]);
        rebuildList();
    }

    private void rebuildList() {
        String sortOrder = Utility.getSortColumnName(mSortTypeIndex) + (mSortAsc ? " ASC" : " DESC");
        mAdapter.setNameNativeOrder(mSortTypeIndex == Utility.SORT_BY_LAST_NAME ? false : true);
        Bundle bundle = new Bundle();
        bundle.putString(KEY_SORT_ORDER, sortOrder);
        if (getLoaderManager().getLoader(ALLOWEDLIST_CURSOR_LOADER) != null)
            getLoaderManager().destroyLoader(ALLOWEDLIST_CURSOR_LOADER);
        getLoaderManager().initLoader(ALLOWEDLIST_CURSOR_LOADER, bundle, this);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        getActivity().getMenuInflater().inflate(R.menu.main_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_main_add_item: {
                //TODO process add from contact book
                showContactsToPick();
                break;
            }
            case R.id.action_main_delete_item: {
                if (mHasRecords) {
                    if (basicActionsListener != null) basicActionsListener.callDeleteUI();

                } else {
                    Toast.makeText(getActivity(), getString(R.string.no_records), Toast.LENGTH_SHORT).show();
                }
                break;
            }
            case R.id.action_main_add_item_manual: {
                showManualAddDialog();
                break;
            }
            case R.id.action_help: {
                //TODO code help screen
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void showContactsToPick() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_SELECT_PHONE_NUMBER);
        }
    }


    private void showManualAddDialog() {
        DialogFragment manualAddDialogFragment = new ManualAddDialogFragment();
        manualAddDialogFragment.show(getActivity().getSupportFragmentManager(), ManualAddDialogFragment.TAG);


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
        mHasRecords = cursor.getCount() > 0 ? true : false;

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    @Override
    public void onPause() {
        Utility.setSortTypeIndex(getActivity(), mSortTypeIndex);
        Utility.setSortOrderAscending(getContext(), mSortAsc);
        super.onPause();
    }


    @Override
    public void dataSetChanged() {
        Log.d(TAG, "dataSetChanged: ");
        String sortOrder = Utility.getSortColumnName(mSortTypeIndex) + (mSortAsc ? " ASC" : " DESC");
        Bundle bundle = new Bundle();
        bundle.putString(KEY_SORT_ORDER, sortOrder);
        getLoaderManager().restartLoader(ALLOWEDLIST_CURSOR_LOADER, bundle, this);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SELECT_PHONE_NUMBER && resultCode == Activity.RESULT_OK) {
            // Get the URI and query the content provider for the phone number
            Uri contactUri = data.getData();
            String[] projection = new String[]{
                    ContactsContract.CommonDataKinds.Phone.NUMBER, //index=0
                    ContactsContract.CommonDataKinds.Photo.PHOTO_THUMBNAIL_URI,//index=1
                    ContactsContract.Data.CONTACT_ID //index=2
            };

            Log.d(TAG, "onActivityResult: uri " + contactUri);
            Cursor contactCursor = getActivity().getContentResolver().query(contactUri, projection,
                    null, null, null);
            // If the cursor returned is valid, get the phone number


            if (contactCursor != null & contactCursor.moveToFirst()) {
                String number = "";
                String firstName = "";
                String lastName = "";
                Bitmap contactBitmap = null;
                number = contactCursor.getString(0);
                contactBitmap = Utility.decodeSampledBitmapFromUri(Uri.parse(contactCursor.getString(1)), getActivity(), 50, 30);
                Drawable drawable = null;
                if (contactBitmap != null) {
                    drawable = new BitmapDrawable(getResources(), contactBitmap);
                }
                long contactId = contactCursor.getLong(2);
                Uri baseContactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);
                Uri dataUri = Uri.withAppendedPath(baseContactUri, ContactsContract.Contacts.Data.CONTENT_DIRECTORY);
                Cursor nameCursor = getActivity().getContentResolver().query(
                        dataUri,
                        null,
                        ContactsContract.RawContacts.Data.MIMETYPE + "=?",
                        new String[]{ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE},
                        null);
                if (nameCursor != null && nameCursor.moveToFirst()) {
                    firstName = nameCursor.getString(nameCursor.getColumnIndex(ContactsContract.RawContacts.Data.DATA2));
                    lastName = nameCursor.getString(nameCursor.getColumnIndex(ContactsContract.RawContacts.Data.DATA3));
                    Log.d(TAG, "onActivityResult: " + firstName + " " + lastName);
                    nameCursor.close();
                }


                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                if (drawable == null) {
                    builder.setIcon(R.drawable.ic_person_green);
                } else {
                    builder.setIcon(drawable);
                }
                builder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder
                        .setTitle("New Contact")
                        .setMessage("Name: " + firstName
                                + " " + lastName
                                + "\nNumber: " + number)
                        .setCancelable(false);
                builder.create().show();

                // Do something with the phone number
            contactCursor.close();
            }

        }

    }
}
