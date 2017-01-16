package com.barryholroyd.bluetoothchattechdemo.support;

import android.app.Activity;
import android.app.Application;
import android.bluetooth.BluetoothSocket;

/**
 * Extended Application class used to save state across components.
 */

public class ApplicationGlobalState extends Application implements GlobalState
{
    private BluetoothSocket btChatSocket;
    private Activity currentActivity = null;
    private long mainThreadId;

    @Override public BluetoothSocket getBtChatSocket() { return btChatSocket; }
    @Override public void setBtChatSocket(BluetoothSocket _btChatSocket) {
        btChatSocket = _btChatSocket;
    }

    @Override public Activity getCurrentActivity() { return currentActivity; }
    @Override public void     setCurrentActivity(Activity a) { currentActivity = a; }

    // Must call setMainThreadId() from the main thread. Only need to call once.
    @Override public long getMainThreadId() { return mainThreadId; }
    @Override public void setMainThreadId() { mainThreadId = Thread.currentThread().getId(); }
}
