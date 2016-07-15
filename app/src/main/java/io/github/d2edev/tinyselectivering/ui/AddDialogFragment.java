package io.github.d2edev.tinyselectivering.ui;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import io.github.d2edev.tinyselectivering.R;
import io.github.d2edev.tinyselectivering.db.DataContract;
import io.github.d2edev.tinyselectivering.db.EntrySaveTask;
import io.github.d2edev.tinyselectivering.logic.DataSetWatcher;
import io.github.d2edev.tinyselectivering.util.Utility;

/**
 * Implements adding new entry with name,number, picture
 * manually added or selected from contacts,
 * does needed check for cases number/name already exist
 * in db
 */

public class AddDialogFragment extends DialogFragment {
    public static final String TAG = "TAG_ManualAddDialog";
    public static final int MIN_TEXT_LENGTH = 1;
    public static final int MIN_NUM_LENGTH = 6;
    public static final int MANUAL_PIC_SELECTION = 1;
    private static final String KEY_OK_ENABLED = "OK_ENABLED";
    private static final int REQUEST_GRANT_READ_EXT_STORAGE = 101;
    private Button mPositiveButton;
    private View mDialogView;
    private EditText mFirstNameInput;
    private EditText mLastNameInput;
    private EditText mNumberInput;
    private ImageView mUserPic;
    private boolean bFirstNameOK;
    private boolean bSecondNameOK;
    private boolean bNumberOK;
    private boolean bShowDefaultPic = true;
    private Bitmap picBitmap;
    private Uri imageUri;
    private Bundle mBundle;





    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Log.d(TAG, "onCreateDialog: ");
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        //get inflanter and inflate custom layout
        LayoutInflater inflater = getActivity().getLayoutInflater();
        mDialogView = inflater.inflate(R.layout.dialog_add, null);
        dialogBuilder
                .setView(mDialogView)
                .setPositiveButton(R.string.dialog_add_entry, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //if OK clicked - save data
                        addEntry();
                    }
                })
                .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //if CANCEL clicked just close dialog
                        Log.d(TAG, "onClick: negative");
                        AddDialogFragment.this.getDialog().cancel();
                    }
                });

        return dialogBuilder.create();
    }

    private void addEntry() {
        Bundle bundle = new Bundle();
        if(mBundle!=null){
            bundle=mBundle;
        }else{
            String tmp = String.valueOf(mFirstNameInput.getText());
            //if first name is not empty - put to bundle
            if(!TextUtils.isEmpty(tmp)) {bundle.putString(DataContract.KEY_FIRST_NAME, tmp);}
            tmp=String.valueOf(mLastNameInput.getText());
            //same for last name
            if(!TextUtils.isEmpty(tmp)) { bundle.putString(DataContract.KEY_LAST_NAME,tmp );}
            //phone number is obligatory
            bundle.putString(DataContract.KEY_NUMBER, String.valueOf(mNumberInput.getText()));
            bundle.putParcelable(DataContract.KEY_IMAGE_BITMAP, picBitmap);
        }
        EntrySaveTask task = new EntrySaveTask(getActivity());
        task.setDataSetWatcher((DataSetWatcher) getActivity()
                .getSupportFragmentManager()
                .findFragmentByTag(MainFragment.TAG));
        task.execute(bundle);

    }

    public void setContactDataBundle(Bundle bundle){
        mBundle=bundle;
    }

    @Override
    public void onResume() {
        super.onResume();
        //get controls
        mPositiveButton = ((AlertDialog) this.getDialog()).getButton(AlertDialog.BUTTON_POSITIVE);
        mFirstNameInput = (EditText) mDialogView.findViewById(R.id.dialog_first_name);
        mLastNameInput = (EditText) mDialogView.findViewById(R.id.dialog_second_name);
        mNumberInput = (EditText) mDialogView.findViewById(R.id.dialog_number);
        mUserPic = (ImageView) mDialogView.findViewById(R.id.dialog_image);
        TextView header= (TextView) mDialogView.findViewById(R.id.dialog_header);
        //bundle presence indicates that dialog is shown as confirmation for data gained
        //from Contact PICK, not for manual input
        if(mBundle==null){
            header.setText(getString(R.string.manual_add_dialog_title));
            //show default pic if flag was not changed (means no custom pic set)
            if (bShowDefaultPic) mUserPic.setImageResource(R.drawable.ic_person_green);
            //enable or disable save button?
            checkOkButton();
        }else{
            header.setText(getString(R.string.contacts_add_dialog_title));
            //fill inputs with data from bundle and lock them
            //to prevent modifying
            if(mBundle.containsKey(DataContract.KEY_FIRST_NAME))mFirstNameInput
                    .setText(mBundle.getString(DataContract.KEY_FIRST_NAME));
            mFirstNameInput.setEnabled(false);
            if(mBundle.containsKey(DataContract.KEY_LAST_NAME))mLastNameInput
                    .setText(mBundle.getString(DataContract.KEY_LAST_NAME));
            mLastNameInput.setEnabled(false);
            mNumberInput.setText(mBundle.getString(DataContract.KEY_NUMBER));
            mNumberInput.setEnabled(false);
            mPositiveButton.setEnabled(true);
            //if bundle contains picture - use it for contact being processed
            if (mBundle.containsKey(DataContract.KEY_IMAGE_BITMAP)){
                Parcelable tmp=mBundle.getParcelable(DataContract.KEY_IMAGE_BITMAP);
                if(tmp instanceof Bitmap){
                    mUserPic.setImageBitmap((Bitmap) tmp);
                }
            }else{
                mUserPic.setImageResource(R.drawable.ic_person_green);
            }
        }

        mUserPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                supposePictureSelection();
            }
        });
        mFirstNameInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() >= MIN_TEXT_LENGTH) {
                    bFirstNameOK = true;
                } else {
                    bFirstNameOK = false;
                }
                checkOkButton();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });



        mLastNameInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() >= MIN_TEXT_LENGTH) {
                    bSecondNameOK = true;
                } else {
                    bSecondNameOK = false;
                }
                checkOkButton();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        mNumberInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() >= MIN_NUM_LENGTH) {
                    bNumberOK = true;
                } else {
                    bNumberOK = false;
                }
                checkOkButton();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void checkOkButton() {
        //either first or last name should be entered
        //number should be entered
        mPositiveButton.setEnabled((bFirstNameOK || bSecondNameOK) && bNumberOK);
    }

    private void supposePictureSelection() {
        //check permissions
        if(Utility.hasSystemPermission(getActivity(),Manifest.permission.READ_EXTERNAL_STORAGE)){
            //start implicit intent to get pic

            Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            if (pickPhoto.resolveActivity(getActivity().getPackageManager()) != null) {
                startActivityForResult(pickPhoto, MANUAL_PIC_SELECTION);
            } else {
                Toast.makeText(
                        getActivity(),
                        getString(R.string.no_pics_provider),
                        Toast.LENGTH_SHORT).show();
            }
        }else{
            AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
            builder
                    .setTitle(getString(R.string.perm_title_general))
                    .setMessage(getString(R.string.perm_read_ext_stor_desc))
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    })
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            requestPermissions(
                                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                    REQUEST_GRANT_READ_EXT_STORAGE
                            );
                        }
                    }).create().show();
        }

    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultIntent) {
        Log.d(TAG, "onActivityResult: ");
        super.onActivityResult(requestCode, resultCode, resultIntent);
        switch (requestCode) {
            //if we got result for pic selection
            case MANUAL_PIC_SELECTION: {
                //and it's ok
                if (resultCode == Activity.RESULT_OK) {
                    imageUri = resultIntent.getData();
                    Log.d(TAG, "onActivityResult: uri " + imageUri);
                    //downsample selected bitmap and show it as user pic
                    picBitmap = Utility.decodeSampledBitmapFromUri(imageUri, getActivity(), 50, 50);
                    if (picBitmap != null) {
                        Log.d(TAG, "onActivityResult: got bitmap h; "
                                + picBitmap.getHeight()
                                + " w:" + picBitmap.getWidth());
                        mUserPic.setImageBitmap(picBitmap);
                        bShowDefaultPic = false;
                    }
                } else {
                    //otherwise do nothing
                    Log.d(TAG, "onActivityResult: cancel");
                }
                break;
            }
            default: {
                //do nothing
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.d(TAG, "onSaveInstanceState: ");
        outState.putBoolean(KEY_OK_ENABLED, bShowDefaultPic);
        super.onSaveInstanceState(outState);
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        if (savedInstanceState.containsKey(KEY_OK_ENABLED)) {
            bShowDefaultPic = savedInstanceState.getBoolean(KEY_OK_ENABLED);
        }

        Log.d(TAG, "onViewCreated: ");
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onPause() {
        super.onPause();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode,permissions,grantResults);
        switch (requestCode){
            case REQUEST_GRANT_READ_EXT_STORAGE:{
                if(grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    supposePictureSelection();
                }else{
                    Toast.makeText(getActivity(), getString(R.string.perm_refused), Toast.LENGTH_SHORT).show();
                }
            }
        break;
        }
    }
}
