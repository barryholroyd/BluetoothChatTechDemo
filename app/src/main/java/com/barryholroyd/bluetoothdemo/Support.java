package com.barryholroyd.bluetoothdemo;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.barryholroyd.bluetoothdemo.dialog.ErrorDialog;

/**
 * Created by Barry on 12/13/2016.
 */

public class Support {
    public static void log(String msg) {
        Log.v("BLUETOOTH_ DEMO", msg);
    }

    public static void userError(Activity a, String msg) {
        Toast t = Toast.makeText(a, msg, Toast.LENGTH_LONG);
        t.show();
    }

    public static void userFatalError(Activity a, String msg) {
        ErrorDialog
                .newInstance("Fatal Error", msg)
                .show(a.getFragmentManager(), "error_dialog");
    }
}
