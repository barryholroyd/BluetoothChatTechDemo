package com.barryholroyd.bluetoothchatdemo.support;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.barryholroyd.bluetoothchatdemo.dialog.ErrorDialog;

/**
 * General static support methods.
 */
public class Support {
    private static Toaster toaster = null;
    private static String appLabel = null;

    public static final String BUNDLE_KEY_BTDEVICE = "com.barryholroyd.bluetoothchatdemo.BTDEVICE";

    /** Initialization */
    private static void init() {
        synchronized (Support.class){
            Context c = ActivityTracker.getAppContext();
            if (c == null)
                throw new SupportException("No Context available.");
            if (toaster == null) {
                toaster = new Toaster(c);
            }
            if (appLabel == null) {
                PackageManager pm = c.getPackageManager();
                try {
                    ApplicationInfo ai = pm.getApplicationInfo(c.getPackageName(), 0);
                    appLabel = (String) pm.getApplicationLabel(ai);
                }
                catch (PackageManager.NameNotFoundException nnfe) {
                    throw new SupportException("Could not get package name.");
                }
            }
        }
    }

    /** Display a Dialog to the user indicating a fatal error, then exit the app. */
    public static void fatalError(String msg) {
        // Get the currently running Activity.
        Activity a = ActivityTracker.getActivity(); // TBD: finish this section
        if (a == null)
            throw new SupportException("Fatal Error: " + msg);
        ErrorDialog
            .newInstance("Fatal Error", msg)
            .show(a.getFragmentManager(), "error_dialog");
    }

    /** Return the app label as defined in the manifest. */
    public static String getAppLabel() {
        if (appLabel == null)
            init();
        return appLabel;
    }

    /** Basic logging for the app. */
    public static void log(String msg) {
        Log.d("BLUETOOTH_CHAT_DEMO", msg);
    }

    /**
     * Send the user a message via a Toast, from either the foreground or background,
     * and log it.
     */
    public static void userMessage(String msg) {
        if (toaster == null)
            init();
        Toaster.display(msg);
        log(msg);
    }

    /** Utility wrapper for starting an Activity which returns a result. */
    public static void startAFR(Activity a, String action, int requestCode) {
        Intent intent = new Intent(action);
        a.startActivityForResult(intent, requestCode);
    }
}

/** Support-specific exception. */
class SupportException extends RuntimeException
{
    SupportException(String msg) {
        super(msg);
    }
}