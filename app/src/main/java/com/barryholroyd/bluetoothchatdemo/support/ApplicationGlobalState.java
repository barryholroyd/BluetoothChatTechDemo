package com.barryholroyd.bluetoothchatdemo.support;

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

    public BluetoothSocket getBtChatSocket() { return btChatSocket; }
    public void setBtChatSocket(BluetoothSocket _btChatSocket) { btChatSocket = _btChatSocket; }

    public Activity getCurrentActivity() { return currentActivity; }
    public void     setCurrentActivity(Activity a) { currentActivity = a; }
}
