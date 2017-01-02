package com.barryholroyd.bluetoothchatdemo.support;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;

/**
 * Dialog that informs the user of a fatal error. The only option the user has
 * at this point is to exit the application by pressing the "Exit" button.
 */
public class FatalErrorDialog extends DialogFragment {
    public static FatalErrorDialog newInstance(String title, String msg) {
        FatalErrorDialog frag = new FatalErrorDialog();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putString("msg", msg);
        frag.setArguments(args);
        return frag;
    }

    /** Create the dialog. */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String title = getArguments().getString("title");
        String msg = getArguments().getString("msg");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title)
                .setMessage(msg)
                .setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        System.exit(1);
                    }
                });
        return builder.create();
    }
}
