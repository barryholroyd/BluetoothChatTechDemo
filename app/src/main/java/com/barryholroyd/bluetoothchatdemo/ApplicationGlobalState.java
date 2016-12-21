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
    private BluetoothSocket btSocket;
    public BluetoothSocket getBtSocket() { return btSocket; }
    public void setBtSocket(BluetoothSocket _btSocket) { btSocket = _btSocket; }
}
