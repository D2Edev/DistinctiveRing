package io.github.d2edev.distinctivering;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;

/**
 * Created by d2e on 13.06.16.
 */

public class ManualAddDialog extends DialogFragment {
    public static final String TAG = "TAG_ManualAddDialog";


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        //get inflanter and inflate custom layout
        // Pass null as the parent view because its going in the dialog layout
        LayoutInflater inflater = getActivity().getLayoutInflater();
        dialogBuilder
                .setView(inflater.inflate(R.layout.dialog_manual_add, null))
                .setPositiveButton(R.string.add_entry, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (inputVerified()) {
                            addEntry();
                        }
                    }
                })
                .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //just close dialog
                        ManualAddDialog.this.getDialog().cancel();
                    }
                });
        return dialogBuilder.create();
    }

    private void addEntry() {

    }

    private boolean inputVerified() {

        return false;
    }
}
