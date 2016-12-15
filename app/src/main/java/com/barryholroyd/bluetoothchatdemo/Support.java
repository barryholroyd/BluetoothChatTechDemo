package com.barryholroyd.bluetoothchatdemo;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.barryholroyd.bluetoothchatdemo.dialog.ErrorDialog;

/**
 * Created by Barry on 12/13/2016.
 */

public class Support {
    public static void log(String msg) {
        Log.v("BLUETOOTH_DEMO", msg);
    }

    public static void in(String name) {
        log("IN:  " + name);
    }

    public static void out(String name) {
        log("OUT: " + name);
    }

    public static void userMessage(Activity a, String msg) {
        Toast t = Toast.makeText(a, msg, Toast.LENGTH_LONG);
        t.show();
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
