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
    private static final boolean traceEnabled = true;

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
        String s = String.format(Locale.US, "Fatal Error: %s", msg);
        Support.error("*** " + s);
        // Get the currently running Activity.
        Activity a = ActivityTracker.getActivity();
        if (a == null)
            throw new SupportException(s);
        FatalErrorDialog
            .newInstance(msg)
            .show(a.getFragmentManager(), "error_dialog");
    }

    /** Return the app label as defined in the manifest. */
    public static String getAppLabel() {
        if (appLabel == null)
            init();
        return appLabel;
    }

    /** External access to logging -- turn off in production. */
    public static void trace(String msg) {
        if (traceEnabled)
            log(msg);
    }

    /** External access to logging -- leave on in production. */
    public static void error(String msg) {
        log(msg);
    }

    /**
     * Log an exception with a customized string.
     */
    public static void exception(String s, Exception e) {
        Support.error(String.format(Locale.US, "%s: %s", s, e.getMessage()));
    }
    /** Basic logging for the app. */
    private static void log(String msg) {
        Log.d("BLUETOOTH_CHAT_DEMO", msg);
    }

    /**
     * Send the user a message via a Toast, from either the foreground or background,
     * and log it.
     */
    public static void userMessage(String msg) {
        log("User message: " + msg);
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
        Support.exception("Support Exception", this);
    }
}