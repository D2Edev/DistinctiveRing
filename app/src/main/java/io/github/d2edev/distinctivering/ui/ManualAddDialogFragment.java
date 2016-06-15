package io.github.d2edev.distinctivering.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import io.github.d2edev.distinctivering.R;
import io.github.d2edev.distinctivering.db.DataContract;

/**
 * Created by d2e on 13.06.16.
 */

public class ManualAddDialogFragment extends DialogFragment {
    public static final String TAG = "TAG_ManualAddDialog";
    public static final int MIN_TEXT_LENGTH = 1;
    public static final int MIN_NUM_LENGTH = 10;
    private Button mPositiveButton;
    private View mDialogView;
    private EditText mFirstNameInput;
    private EditText mLastNameInput;
    private EditText mNumbetInput;
    private boolean bFirstNameOK;
    private boolean bSecondNameOK;
    private boolean bNumberOK;


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        //get inflanter and inflate custom layout
        // Pass null as the parent view because its going in the dialog layout
        LayoutInflater inflater = getActivity().getLayoutInflater();
        mDialogView = inflater.inflate(R.layout.dialog_manual_add, null);
        dialogBuilder
                .setView(mDialogView)
                .setPositiveButton(R.string.dialog_add_entry, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        addEntry();
                    }
                })
                .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //just close dialog
                        Log.d(TAG, "onClick: negative");
                        ManualAddDialogFragment.this.getDialog().cancel();
                    }
                });

        return dialogBuilder.create();
    }

    private void addEntry() {
        //TODO implement data save through AsyncTaskLoader
        long idPerson = -1;
        String selection = DataContract.Person.COLUMN_FIRST_NAME + "=? AND " + DataContract.Person.COLUMN_LAST_NAME + "=?";
        String firstName = String.valueOf(mFirstNameInput.getText());
        String lastName = String.valueOf(mLastNameInput.getText());
        String[] selectionArgs = new String[]{firstName, lastName};


        Cursor cursor = getContext().getContentResolver().query(DataContract.Person.CONTENT_URI, null, selection, selectionArgs, null);
        if (cursor!=null||cursor.getCount()>0) {
            cursor.moveToFirst();
            //TODO check getting error here
            idPerson = cursor.getLong(0);
        }else{
            ContentValues personRecord =new ContentValues();
            personRecord.put(DataContract.Person.COLUMN_FIRST_NAME,firstName);
            personRecord.put(DataContract.Person.COLUMN_LAST_NAME,lastName);
//            TODO implement pic path provision
//            personRecord.put(DataContract.Person.COLUMN_PIC_PATH,picPath);
            Uri newPersonUri=getContext().getContentResolver().insert(DataContract.Person.CONTENT_URI,personRecord);
            try{
            idPerson=Long.parseLong(DataContract.Person.getPersonIdFromUri(newPersonUri));

            }catch (NumberFormatException e){
                Log.e(TAG, "addEntry: invalid parse data from uri " + newPersonUri  );
            }
        }

        if(idPerson>-1){
            ContentValues phoneRecord = new ContentValues();
            phoneRecord.put(DataContract.PhoneNumber.COLUMN_NUMBER, String.valueOf(mNumbetInput.getText()));
            phoneRecord.put(DataContract.PhoneNumber.COLUMN_KEY_PERSON, idPerson);
            phoneRecord.put(DataContract.PhoneNumber.COLUMN_NUMBER, String.valueOf(mNumbetInput.getText()));
            getContext().getContentResolver().insert(DataContract.PhoneNumber.CONTENT_URI, phoneRecord);
        }

    }


    @Override
    public void onResume() {
        mPositiveButton = ((AlertDialog) this.getDialog()).getButton(AlertDialog.BUTTON_POSITIVE);
        mPositiveButton.setEnabled(false);
        mFirstNameInput = (EditText) mDialogView.findViewById(R.id.dialog_first_name);
        mLastNameInput = (EditText) mDialogView.findViewById(R.id.dialog_second_name);
        mNumbetInput = (EditText) mDialogView.findViewById(R.id.dialog_number);
        mFirstNameInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() >= MIN_TEXT_LENGTH) {
                    bFirstNameOK = true;
                    mPositiveButton.setEnabled(bFirstNameOK && bSecondNameOK && bNumberOK);
                } else {
                    mPositiveButton.setEnabled(false);
                }
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
                    mPositiveButton.setEnabled(bFirstNameOK && bSecondNameOK && bNumberOK);
                } else {
                    mPositiveButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        mNumbetInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() >= MIN_NUM_LENGTH) {
                    bNumberOK = true;
                    mPositiveButton.setEnabled(bFirstNameOK && bSecondNameOK && bNumberOK);
                } else {
                    mPositiveButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

}
