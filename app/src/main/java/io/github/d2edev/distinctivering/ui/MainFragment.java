package io.github.d2edev.distinctivering.ui;


import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import io.github.d2edev.distinctivering.BuildConfig;
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
    private static final int REQUEST_SHOW_DR_NOTIFICATION = 201;
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


    //TODO finish settings - main help


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
        if(Utility.isDistinctiveRingEnabled(getActivity())){
            setNotificationOn();
            mFab.setImageResource(R.drawable.ic_volume_up_white);
        }else{
            mFab.setImageResource(R.drawable.ic_volume_off_white);
        }
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
        mListView.setEmptyView(rootView.findViewById(R.id.empty_view_text));

        return rootView;
    }


    //process click
    private void changeDistinctiveRingSettings() {

        //change mode to opposite to current
        //if currently enabled
        if (Utility.isDistinctiveRingEnabled(getActivity())) {
            //save "disable" to prefs
            Utility.setDistinctiveRingEnabled(getActivity(), false);
            //set "off image" to fab
            mFab.setImageResource(R.drawable.ic_volume_off_white);
            //remove notification
            unsetNotification();
        } else {
            //vice-versa to above
            Utility.setDistinctiveRingEnabled(getActivity(), true);
            mFab.setImageResource(R.drawable.ic_volume_up_white);
            setNotificationOn();

        }
    }

    private void unsetNotification() {
        //cancel notification
        NotificationManagerCompat.from(getActivity()).cancel(MainActivity.DR_ACTIVE_NOTIFY);
    }

    private void setNotificationOn() {
        //click on notification will start mainactivity
        Intent mainActivityIntent = new Intent(getActivity(), MainActivity.class);
        //wrap intent into PendingIntent
        PendingIntent notificationIntent = PendingIntent.getActivity(
                getActivity(),
                REQUEST_SHOW_DR_NOTIFICATION,
                mainActivityIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        //build notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getActivity())
                .setSmallIcon(R.drawable.ic_icon_notify)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.ring_enabled))
                .setContentIntent(notificationIntent);
        Notification notification = builder.build();
        //set flags so notification will be enabled unless "ring off" mode set
        notification.flags |= Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;
        //show
        NotificationManagerCompat mNotificationManager = NotificationManagerCompat.from(getActivity());
        mNotificationManager.notify(MainActivity.DR_ACTIVE_NOTIFY, notification);
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
            case R.id.action_main_settings:{
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "settings called: ");
                }
                if (basicActionsListener != null) basicActionsListener.callSettingsUI();
                break;
            }
            case R.id.action_main_add_item_manual: {
                showAddDialog(null);
                break;
            }
            case R.id.action_help: {
                showHelpDialog();
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }



    private void showHelpDialog() {
        AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
        View dialogView=getActivity().getLayoutInflater().inflate(R.layout.help_main,null,false);
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

    private void showContactsToPick() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_SELECT_PHONE_NUMBER);
        } else {
            Toast.makeText(getActivity(), getString(R.string.no_contacts_provider), Toast.LENGTH_SHORT).show();
        }
    }


    private void showAddDialog(Bundle bundle) {
        AddDialogFragment addDialogFragment = new AddDialogFragment();
        addDialogFragment.setContactDataBundle(bundle);
        addDialogFragment.show(getActivity().getSupportFragmentManager(), AddDialogFragment.TAG);


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
        //enable or disable Floating ActBar visibility and general DistRing settings depending on
        //recors existence
        if(mHasRecords){
            mFab.setVisibility(View.VISIBLE);
        }else{
            //save "disable" to prefs
            Utility.setDistinctiveRingEnabled(getActivity(), false);
            //set "off image" to fab
            mFab.setImageResource(R.drawable.ic_volume_off_white);
            //remove notification
            unsetNotification();
            mFab.setVisibility(View.GONE);
        }

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
    public void onResume() {
        super.onResume();
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onResume: " + Utility.getShowStartupMessage(getActivity()));
        }
        if(Utility.getShowStartupMessage(getActivity())){
            showStartDialog();
        }

    }

    private void showStartDialog() {
        AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
        View dialogView=getActivity().getLayoutInflater().inflate(R.layout.help_start,null,false);
        builder
                .setView(dialogView)
                .setCancelable(false)
                .setNegativeButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        Switch dialogSwitch= (Switch) dialogView.findViewById(R.id.scroll_dialog_switch);
        dialogSwitch.setChecked(Utility.getShowStartupMessage(getActivity()));
        dialogSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                    Utility.setShowStartupMessage(getActivity(),isChecked);

            }
        });
        builder.create().show();
    }

    @Override
    public void dataSetChanged(boolean succes) {
        Log.d(TAG, "dataSetChanged: " + succes);
        if (succes) {
            String sortOrder = Utility.getSortColumnName(mSortTypeIndex) + (mSortAsc ? " ASC" : " DESC");
            Bundle bundle = new Bundle();
            bundle.putString(KEY_SORT_ORDER, sortOrder);
            getLoaderManager().restartLoader(ALLOWEDLIST_CURSOR_LOADER, bundle, this);
        } else {
            Toast.makeText(getActivity(), getString(R.string.no_data_added), Toast.LENGTH_LONG).show();
        }


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        //TODO finalize logic to add contact from ContactBook
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_SELECT_PHONE_NUMBER: {
                    showConfirmationDialog(data);
                }
                default: //do nothing;
            }
        }
    }

    private void showConfirmationDialog(Intent data) {
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
            String picAddress = contactCursor.getString(1);
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
            if (picAddress != null) {
                contactBitmap = Utility.decodeSampledBitmapFromUri(Uri.parse(picAddress), getActivity(), 50, 30);
            }
            contactCursor.close();
            Bundle bundle = new Bundle();
            if (!TextUtils.isEmpty(firstName))
                bundle.putString(DataContract.KEY_FIRST_NAME, firstName);
            if (!TextUtils.isEmpty(lastName))
                bundle.putString(DataContract.KEY_LAST_NAME, lastName);
            bundle.putString(DataContract.KEY_NUMBER, number);
            if (contactBitmap != null)
                bundle.putParcelable(DataContract.KEY_IMAGE_BITMAP, contactBitmap);
            showAddDialog(bundle);
        }
    }


}