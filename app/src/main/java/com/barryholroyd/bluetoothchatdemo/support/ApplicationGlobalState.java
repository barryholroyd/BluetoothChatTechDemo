package com.barryholroyd.bluetoothchatdemo.support;

import android.app.Application;
import android.bluetooth.BluetoothSocket;

/**
 * Extended Application class used to save state across components.
 * <p>
 *     This is used to allow the main Activity to pass an existing socket
 *     to the chat Activity when the background ChooserListener thread accepts
 *     an incoming connection request.
 */

public class ApplicationGlobalState extends Application
{
    /**
     * BluetoothSocket to be passed from ChooserActivity to ChatActivity.
     * BluetoothSockets are not parcelable, so they can't be passed as Intent extras.
     */
    private BluetoothSocket btChatSocket;

    /** True after the first instance of ChooserActivity has been created and initialized. */
    private boolean appInitialized = false;

    /*
     * Getters and setters.
     */
    public BluetoothSocket getBtChatSocket() { return btChatSocket; }
    public void setBtChatSocket(BluetoothSocket _btChatSocket) { btChatSocket = _btChatSocket; }

    public boolean isAppInitialized()  { return appInitialized; }
    public void setAppInitialized() { appInitialized = true; }
}
