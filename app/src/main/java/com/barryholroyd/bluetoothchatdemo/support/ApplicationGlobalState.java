package com.barryholroyd.bluetoothchatdemo.support;

import android.app.Application;
import android.bluetooth.BluetoothSocket;

/**
 * Extended Application class used to save state across components.
 * <p>
 *     This is used to allow the main Activity to pass an existing socket
 *     to the chat Activity when the background SelectConnectionListener thread accepts
 *     an incoming connection request.
 */

public class ApplicationGlobalState extends Application
{
    /**
     * Bluetooth socket to be passed from SelectActivity to ChatActivity.
     * BluetoothSockets are not parcelable, so they can't be passed as Intent extras.
     */
    private BluetoothSocket btSocket;

    /** True after the first instance of SelectActivity has been created and initialized. */
    private boolean appInitialized = false;

    /** True when the app "server" is listening for incoming connections. */
    private static boolean serverRunning = false; // DEL:

    /*
     * Getters and setters.
     */
    public BluetoothSocket getBtSocket() { return btSocket; }
    public void setBtSocket(BluetoothSocket _btSocket) { btSocket = _btSocket; }

    public boolean isAppInitialized()  { return appInitialized; }
    public void setAppInitialized() { appInitialized = true; }

    // DEL:
    public boolean isServerRunning() { return serverRunning; }
    public void setServerRunning(@SuppressWarnings("SameParameterValue") boolean b) { serverRunning = b; }
}
