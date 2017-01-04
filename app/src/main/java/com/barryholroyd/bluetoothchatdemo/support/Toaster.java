package com.barryholroyd.bluetoothchatdemo.support;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.Gravity;
import android.widget.Toast;

import static android.widget.Toast.LENGTH_LONG;
import static android.widget.Toast.LENGTH_SHORT;

/**
 * Helper class to display toasts from either the foreground or the background.
 */
class Toaster {
    /** Message command: TOAST */
    private static final int TOAST = 1;

    /** Handler for the UI thread. */
    private static Handler mHandler = null;

    /**
     * Toaster constructor.
     * <p>
     *     Create a Handler to tbe main thread so that we can use the
     *     Toaster from background threads.
     */
    Toaster() {
        mHandler = new Handler(Looper.getMainLooper()) {
            /**
             * Create and show a toast (on the main thread).
             * @param message contains the text to be displayed in the toast.
             */
            @Override
            public void handleMessage(Message message) {
                Context ac = ActivityTracker.getAppContext();
                if (message.what == TOAST) {
                    String msg = (String) message.obj;
                    final Toast toast = Toast.makeText(ac, msg, LENGTH_SHORT);
                    toast.setGravity(Gravity.BOTTOM, 0, 0);
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
