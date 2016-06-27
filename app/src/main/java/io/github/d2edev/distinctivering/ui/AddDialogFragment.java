package io.github.d2edev.distinctivering.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import io.github.d2edev.distinctivering.R;
import io.github.d2edev.distinctivering.db.EntrySaveTask;
import io.github.d2edev.distinctivering.logic.DataSetWatcher;
import io.github.d2edev.distinctivering.util.Utility;

/**
 * Implements adding new entry with name,number, pictutre
 * manually added or selected from contacts,
 * does needed check for cases number/name already exist
 * in db
 */

public class AddDialogFragment extends DialogFragment {
    public static final String TAG = "TAG_ManualAddDialog";
    public static final int MIN_TEXT_LENGTH = 1;
    public static final int MIN_NUM_LENGTH = 10;
    public static final int MANUAL_PIC_SELECTION = 1;
    private static final String KEY_OK_ENABLED = "OK_ENABLED";
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


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Log.d(TAG, "onCreateDialog: ");
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        //get inflanter and inflate custom layout
        // Pass null as the parent view because we dont attach
        LayoutInflater inflater = getActivity().getLayoutInflater();
        mDialogView = inflater.inflate(R.layout.dialog_manual_add, null);
        dialogBuilder
                .setView(mDialogView)
                .setPositiveButton(R.string.dialog_add_entry, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //if OK cliced - save data
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
        bundle.putString(EntrySaveTask.KEY_FIRST_NAME, String.valueOf(mFirstNameInput.getText()));
        bundle.putString(EntrySaveTask.KEY_LAST_NAME, String.valueOf(mLastNameInput.getText()));
        bundle.putString(EntrySaveTask.KEY_NUMBER, String.valueOf(mNumberInput.getText()));
        bundle.putParcelable(EntrySaveTask.KEY_IMAGE_URI, picBitmap);
        EntrySaveTask task = new EntrySaveTask(getActivity());
        task.setmDataSetWatcher((DataSetWatcher) getActivity().getSupportFragmentManager().findFragmentByTag(MainFragment.TAG));
        task.execute(bundle);

    }


    @Override
    public void onResume() {
        //get controls
        mPositiveButton = ((AlertDialog) this.getDialog()).getButton(AlertDialog.BUTTON_POSITIVE);
        mFirstNameInput = (EditText) mDialogView.findViewById(R.id.dialog_first_name);
        mLastNameInput = (EditText) mDialogView.findViewById(R.id.dialog_second_name);
        mNumberInput = (EditText) mDialogView.findViewById(R.id.dialog_number);
        mUserPic = (ImageView) mDialogView.findViewById(R.id.dialog_image);
        //show default pic if flag was not changed for other pic
        if (bShowDefaultPic) mUserPic.setImageResource(R.drawable.ic_person_green);
        //enable or disable save button?
        checkOkButton();
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


        super.onResume();
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
        mPositiveButton.setEnabled(bFirstNameOK && bSecondNameOK && bNumberOK);
    }

    private void supposePictureSelection() {
        //start implicit intent to get pic
        Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickPhoto, MANUAL_PIC_SELECTION);
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
                        Log.d(TAG, "onActivityResult: got bitmap h; " + picBitmap.getHeight() + " w:" + picBitmap.getWidth());
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
}
