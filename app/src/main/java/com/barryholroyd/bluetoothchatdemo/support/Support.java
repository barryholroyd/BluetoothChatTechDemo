package com.barryholroyd.bluetoothchatdemo.support;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Toast;

import com.barryholroyd.bluetoothchatdemo.MainActivity;
import com.barryholroyd.bluetoothchatdemo.dialog.ErrorDialog;

import java.lang.ref.WeakReference;

/**
 * General static support methods.
 */
public class Support {
    private static Toaster toaster = null;
    private static String appLabel = null;
    private WeakReference<Activity> wra;

    public static final String BUNDLE_KEY_BTDEVICE = "com.barryholroyd.bluetoothchatdemo.BTDEVICE";

    public Support(WeakReference<Activity> _wra) {
        wra = _wra;
    }

    /** Initialization */
    public static void init(Context c) {
        toaster = new Toaster(c);
        PackageManager pm = c.getPackageManager();
        try {
            ApplicationInfo ai = pm.getApplicationInfo(c.getPackageName(), 0);
            appLabel = (String) pm.getApplicationLabel(ai);
        }
        catch (PackageManager.NameNotFoundException nnfe) {
            log("Could not get package name.");
            throw new MissingPackageName("Could not get package name.");
        }
    }

    /** Display a Dialog to the user indicating a fatal error, then exit the app. */
    public static void fatalError(String msg) {
        // Get the currently running Activity.
        Activity a = ActivityTracker.get();
        if (a == null) {
            throw new FatalErrorException("Fatal Error: " + msg);
        }

        ErrorDialog
                .newInstance("Fatal Error", msg)
                .show(a.getFragmentManager(), "error_dialog");
    }

    /** Return the app label as defined in the manifest. */
    public static String getAppLabel() { return appLabel; }

    /** Basic logging for the app. */
    public static void log(String msg) {
        Log.d("BLUETOOTH_CHAT_DEMO", msg);
    }

    /**
     * Send the user a message via a Toast, from either the foreground or background,
     * and log it.
     */
    public static void userMessage(String msg) {
        if (toaster == null) {
            throw new IllegalStateException(
                    "Support.userMessage() called before Support.init().");
        }
        Toaster.display(msg);
        log(msg);
    }

    /** Utility wrapper for starting an Activity which returns a result. */
    public static void startAFR(Activity a, String action, int requestCode) {
        Intent intent = new Intent(action);
        a.startActivityForResult(intent, requestCode);
    }
}

/** Exception thrown if we could not obtain the package name. */
class MissingPackageName extends RuntimeException
{
    MissingPackageName(String msg) {
        super(msg);
    }
}

/** Exception thrown if we could not get the Activity so we could call finish(). */
class FatalErrorException extends RuntimeException
{
    FatalErrorException(String msg) {
        super(msg);
    }
}
