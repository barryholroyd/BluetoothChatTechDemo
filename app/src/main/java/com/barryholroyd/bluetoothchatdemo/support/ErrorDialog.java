package com.barryholroyd.bluetoothchatdemo.support;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;

/**
 * Created by Barry on 12/13/2016.
 */

public class ErrorDialog extends DialogFragment {
    public static ErrorDialog newInstance(String title, String msg) {
        ErrorDialog frag = new ErrorDialog();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putString("msg", msg);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String title = getArguments().getString("title");
        String msg = getArguments().getString("msg");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title)
                .setMessage(msg)
                .setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        System.exit(1); // exit the app TBD: test this
                    }
                });
        return builder.create();
    }
}
