package com.barryholroyd.bluetoothchatdemo.support;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.Gravity;
import android.widget.Toast;

import java.util.Locale;

import static android.widget.Toast.LENGTH_LONG;

/**
 * Helper class to display toasts from either the foreground or the background.
 */
public class Toaster {
    /** Message command: TOAST */
    private static final int TOAST = 1;

    /** Handler for the UI thread. */
    private static Handler mHandler = null;

    /**
     * Toaster constructor.
     * <p>
     *     We get the application Context here (as opposed to passing it in) so that
     *     we are sure we have a Context instance not bound by the lifecycle of a
     *     particular Activity instance.
     * <p>
     *     We also create a Handler to tbe main thread so that we can use the
     *     Toaster from background threads.
     */
    Toaster() {
        mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message message) {
                Context ac = ActivityTracker.getAppContext();
                if (message.what == TOAST) {
                    String msg = (String) message.obj;
                    final Toast toast = Toast.makeText(ac, msg, LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
                else {
                    String msg = String.format(Locale.US,
                            "TBD: ***** Unexpected message type in Toaster: %d.",
                            message.what);
                    Support.log(msg);
                    final Toast toast = Toast.makeText(ac, msg, LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
            }
        };
    }

    /**
     * Display a pop-up message to the user from either the background or the foreground.
     *
     * @param msg message to be displayed.
     */
    public static void display(String msg) {
        if (mHandler == null) {
            throw new IllegalStateException(
                    "Toaster.display() called before Toaster.init().");
        }
        Message message = mHandler.obtainMessage(TOAST, msg);
        message.sendToTarget();
    }
}
