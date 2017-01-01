package com.barryholroyd.bluetoothchatdemo;

import android.app.Application;
import android.bluetooth.BluetoothSocket;

/**
 * Extended Application class used to save state across components.
 * <p>
 *     This is used to allow the main Activity to pass an existing socket
 *     to the chat Activity when the background BluetoothServer thread accepts
 *     an incoming connection request.
 */

public class ApplicationGlobalState extends Application
{
    /** Bluetooth socket to be passed from MainActivity to ChatActivity. */
    private BluetoothSocket btSocket;

    /** True after the first instance of MainActivity has been created and initialized. */
    private boolean appInitialized = false;

    /** True when the app "server" is listening for incoming connections. */
    static boolean serverRunning = false;

    /*
     * Getters and setters.
     */
    public BluetoothSocket getBtSocket() { return btSocket; }
    public void setBtSocket(BluetoothSocket _btSocket) { btSocket = _btSocket; }

    public boolean isAppInitialized()  { return appInitialized; }
    public void setAppInitialized() { appInitialized = true; }

    public boolean isServerRunning() { return serverRunning; }
    public void setServerRunning(boolean b) { serverRunning = b; }
}
