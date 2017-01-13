package com.barryholroyd.bluetoothchatdemo.support;

import android.app.Activity;
import android.bluetooth.BluetoothSocket;

/**
 * Interface defining global state to be maintained.
 */
public interface GlobalState {
    /**
     * BluetoothSocket to be passed from ChooserActivity to ChatActivity.
     * BluetoothSockets are not parcelable, so they can't be passed as Intent extras.
     */
    BluetoothSocket getBtChatSocket();
    void setBtChatSocket(BluetoothSocket _btChatSocket);

    /**
     * Reference to the "current" Activity.
     * <p>
     *     This should be set in each Activity's onResume() method, since that
     *     is where the Activity becomes "current" (i.e., becomes the top Activity
     *     in the task's back stack).
     **/
    Activity getCurrentActivity();
    void     setCurrentActivity(Activity a);
}
