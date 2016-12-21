package com.barryholroyd.bluetoothchatdemo.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;

import com.barryholroyd.bluetoothchatdemo.support.Support;

import java.io.IOException;
import java.util.UUID;

/**
 * Bluetooth "chat" server implementation.
 * <p>
 *     Runs in a background thread.
 */

public class BluetoothServer extends Thread
{
    // app name used to connect
    public static final String SERVICE_NAME = "BluetoothChatDemo";

    // UUID generated at: https://www.uuidgenerator.net/
    public static final UUID MY_UUID = UUID.fromString("bb303707-5a56-4536-8d07-7ead8264f6b9");
    private final BluetoothServerSocket mServerSocket;

    public BluetoothServer(Activity a, BluetoothAdapter mBluetoothAdapter) {
        // Use a temporary object that is later assigned to mServerSocket,
        // because mServerSocket is final
        // TBD: tmp not needed?
        BluetoothServerSocket tmp = null;
        try {
            // MY_UUID is the app's UUID string, also used by the client code
            tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(SERVICE_NAME, MY_UUID);
        } catch (IOException e) {
            Support.fatalError(a, "Failed to get Bluetooth server socket.");
        }
        mServerSocket = tmp;
    }

    /**
     * Accept a connection from a remove Bluetooth client and the pass it to BluetoothComm
     * to run the chat session.
     */
    public void run() {
        BluetoothSocket mSocket;
        while (true) {
            try {
                mSocket = mServerSocket.accept();
                if (mSocket != null) {
                    (new BluetoothComm("SERVER", mSocket)).start();
                    mServerSocket.close();
                    break;
                }
            } catch (IOException e) {
                break;
            }
        }
    }
}
