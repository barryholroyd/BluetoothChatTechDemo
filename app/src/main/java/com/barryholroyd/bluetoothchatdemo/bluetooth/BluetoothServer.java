package com.barryholroyd.bluetoothchatdemo.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;

import com.barryholroyd.bluetoothchatdemo.support.Support;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by Barry on 12/14/2016.
 */

public class BluetoothServer extends Thread
{
    // UUID generated at: https://www.uuidgenerator.net/
    public static final String NAME = "BluetoothDemo";
    public static final UUID MY_UUID = UUID.fromString("bb303707-5a56-4536-8d07-7ead8264f6b9");
    private final BluetoothServerSocket mmServerSocket;

    public BluetoothServer(Activity a, BluetoothAdapter mBluetoothAdapter) {
        // Use a temporary object that is later assigned to mmServerSocket,
        // because mmServerSocket is final
        BluetoothServerSocket tmp = null;
        try {
            // MY_UUID is the app's UUID string, also used by the client code
            tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
        } catch (IOException e) {
            Support.fatalError(a, "Failed to get Bluetooth server socket.");
        }
        mmServerSocket = tmp;
    }

    public void run() {
        BluetoothSocket mSocket;
        while (true) {
            try {
                mSocket = mmServerSocket.accept();
                if (mSocket != null) {
                    (new BluetoothComm("SERVER", mSocket)).start();
                    mmServerSocket.close();
                    break;
                }
            } catch (IOException e) {
                break;
            }
        }
    }

    public void cancel() {
        try {
            mmServerSocket.close();
        } catch (IOException e) { }
    }
}
