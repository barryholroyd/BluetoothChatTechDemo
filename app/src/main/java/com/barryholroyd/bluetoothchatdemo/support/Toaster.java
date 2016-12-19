package com.barryholroyd.bluetoothchatdemo.support;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.Gravity;
import android.widget.Toast;

import static android.widget.Toast.LENGTH_LONG;

/**
 * Helper class to display toasts from either the foreground or the background.
 * <p>
 * Each successive toast is positioned slightly below the previous toast; after five
 * toasts, it starts from the top again.
 */
public class Toaster {
    /** Distance in pixels between top edge of successive toasts. */
    private static final int YINC = 100;

    /** Maximum distance to offset before restarting at the top again. */
    private static final int YMAX  = YINC * 5;

    /** Current offset from the top position. */
    private static int offset = 0;

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
     *
     * @param c any valid Context instance.
     */
    Toaster(Context c) {
        final Context ac = c.getApplicationContext();
        mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message message) {
//    TBD: restore            offset = (offset > YMAX) ? 0 : offset + YINC;
                offset = 10; // DEL:
                String msg = (String) message.obj;
                final Toast toast = Toast.makeText(ac, msg, LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
        };
    }

    /**
     * Display a pop-up message to the user.
     * <p>
     *     This can be called from either the foreground or the background.
     *
     * @param msg message to be displayed.
     */
    static int counter = 1; // DEL:
    public static void display(String msg) {
        msg = String.format("%d. %s", counter++, msg); // DEL:
        if (mHandler == null) {
            throw new IllegalStateException(
                    "Toaster.display() called before Toaster.init().");
        }
        Message message = mHandler.obtainMessage(TOAST, msg);
        message.sendToTarget();
    }
}
