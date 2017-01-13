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
    public static void init(Context c) {
        Context ac = c.getApplicationContext();
        if (toaster == null) {
            toaster = new Toaster(ac);
        }
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

    /** Display a Dialog to the user indicating a fatal error, then exit the app. */
    public static void fatalError(Activity a, String msg) {
        if (a == null) {
            String s = String.format(Locale.US, "Fatal error: %s", msg);
            throw new SupportException(s);
        }
        FatalErrorDialog.newInstance(msg).show(a.getFragmentManager(), "error_dialog");
    }

    /** Return the app label as defined in the manifest. */
    public static String getAppLabel() { return appLabel; }

    /** External access to logging -- turn off in production. */
    public static void trace(String msg) {
        if (traceEnabled)
            log(msg);
    }

    public static void tmp(String msg) { trace(msg); } // DEL: when no longer needed.

    /** External access to logging -- leave on in production. */
    public static void error(String msg) {
        log(msg);
    }

    /**
     * Log an exception with a customized string.
     */
    public static void exception(String s, Exception e) {
        Support.error(String.format(Locale.US, "%s: [%s -> %s]",
                s, e.getClass().getSimpleName(), e.getMessage()));
    }
    /** Basic logging for the app. */
    private static void log(String msg) {
        Log.d("BLUETOOTH_CHAT_DEMO", msg);
    }

    /**
     * Send the user a message via a Toast, from either the foreground or background,
     * and log it. The display will be for a short duration.
     */
    public static void userMessageShort(String msg) {
        log("User message: " + msg);
        Toaster.displayShort(msg);
    }

    /**
     * Send the user a message via a Toast, from either the foreground or background,
     * and log it. The display will be for a long duration.
     */
    public static void userMessageLong(String msg) {
        log("User message: " + msg);
        Toaster.displayLong(msg);
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