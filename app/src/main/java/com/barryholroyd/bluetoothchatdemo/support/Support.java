package com.barryholroyd.bluetoothchatdemo.support;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import java.util.Locale;

/**
 * General static support methods.
 */
public class Support {
    private static Toaster toaster = null;
    private static String appLabel = null;

    /** Initialization */
    private static void init() {
        synchronized (Support.class){
            if (toaster == null) {
                toaster = new Toaster();
            }
            Context ac = ActivityTracker.getAppContext();
            if (appLabel == null) {
                PackageManager pm = ac.getPackageManager();
                try {
                    ApplicationInfo ai = pm.getApplicationInfo(ac.getPackageName(), 0);
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
        Activity a = ActivityTracker.getActivity();
        if (a == null)
            throw new SupportException("Fatal Error: " + msg);

        ErrorDialog
            .newInstance("Fatal Error", msg)
            .show(a.getFragmentManager(), "error_dialog");

        // Wait forever; ErrorDialog will force the user to exit the app.
        Object o = new Object();
        //noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized (o) {
            try { o.wait(); }
            catch ( InterruptedException ie ) {
                Support.log("Interrupted exception.");
                System.exit(2);
            }
        }
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
        log(msg);
        if (toaster == null)
            init();
        Toaster.display(msg);
    }
}

/** Support-specific exception. */
class SupportException extends RuntimeException
{
    SupportException(String msg) {
        super(msg);
        Support.log(String.format(Locale.US, "*** Support Exception: %s", msg));
    }
}