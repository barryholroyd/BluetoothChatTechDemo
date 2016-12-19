package com.barryholroyd.bluetoothchatdemo.support;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.barryholroyd.bluetoothchatdemo.dialog.ErrorDialog;

/**
 * Created by Barry on 12/13/2016.
 */

public class Support {
    private static Toaster toaster = null;

    public static void init(Context c) {
        toaster = new Toaster(c);
    }

    public static void log(String msg) {
        Log.d("BLUETOOTH_DEMO", msg);
    }

    public static void userMessage(String msg) {
        if (toaster == null) {
            throw new IllegalStateException(
                    "Support.userMessage() called before Support.init().");
        }
        Toaster.display(msg);
    }

    public static void fatalError(Activity a, String msg) {
        ErrorDialog
                .newInstance("Fatal Error", msg)
                .show(a.getFragmentManager(), "error_dialog");
    }

    public static void startAFR(Activity a, String action, int requestCode) {
        Intent intent = new Intent(action);
        a.startActivityForResult(intent, requestCode);
    }

}
