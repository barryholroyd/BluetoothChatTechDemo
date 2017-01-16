package com.barryholroyd.bluetoothchattechdemo.support;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.Gravity;
import android.widget.Toast;

import static android.widget.Toast.LENGTH_LONG;
import static android.widget.Toast.LENGTH_SHORT;

/**
 * Helper class to displayShort toasts from either the foreground or the background.
 */
class Toaster {
    /** Toaster message: display a short Toast. */
    private static final int TOASTER_SHORT = 0;

    /** Toaster message: display a long Toast. */
    private static final int TOASTER_LONG  = 1;

    /** Handler for the UI thread. */
    private static Handler mHandler = null;

    /**
     * Toaster constructor.
     * <p>
     *     Create a Handler to tbe main thread so that we can use the
     *     Toaster from background threads.
     *
     * @param c standard Context
     */
    Toaster(Context c) {
        // Make sure we have the Application's Context.
        final Context ac = c.getApplicationContext();
        mHandler = new Handler(Looper.getMainLooper()) {
            /**
             * Create and show a toast (on the main thread).
             * @param message contains the text to be displayed in the toast.
             */
            @Override
            public void handleMessage(Message message) {
                String msg = (String) message.obj;
                showToast(ac, msg, message.what);
            }
        };
    }

    private static void showToast(Context c, String msg, int msgId) {
        int len = LENGTH_SHORT;
        switch (msgId) {
            case TOASTER_SHORT: len = LENGTH_SHORT; break;
            case TOASTER_LONG:  len = LENGTH_LONG;  break;
        }
        final Toast toast = Toast.makeText(c, msg, len);
        toast.setGravity(Gravity.BOTTOM, 0, 100);
        toast.show();
    }

    /**
     * Display a pop-up message to the user from either the background or the foreground.
     *
     * @param msg message to be displayed.
     */
    private static void display(int msgId, String msg) {
        /*
         * For performance reasons, we use a current Activity if we are in the already on the
         * main thread. Displaying a toast from a background thread can entail a delay of a
         * few seconds since the request gets put on the main thread's message queue.
         */
        if (Thread.currentThread().getId() == Support.getGlobalState().getMainThreadId()) {
            Activity a = Support.getGlobalState().getCurrentActivity();
            if (a != null) {
                showToast(a, msg, msgId);
                return;
            }
        }
        if (mHandler == null) {
            throw new IllegalStateException(
                    "Toaster.displayShort() called before Toaster.init().");
        }
        Message message = mHandler.obtainMessage(msgId, msg);
        message.sendToTarget();
    }

    /** Display a "short" Toast. */
    static void displayShort(String msg) { display(TOASTER_SHORT, msg); }

    /** Display a "long" Toast. */
    static void displayLong(String msg) { display(TOASTER_LONG, msg); }

}
