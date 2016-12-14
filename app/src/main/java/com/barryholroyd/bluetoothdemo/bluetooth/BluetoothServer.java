package com.barryholroyd.bluetoothdemo.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by Barry on 12/14/2016.
 */

public class BluetoothServer extends Thread
{
    public static final String NAME = "BluetoothDemo";
    public static final UUID MY_UUID = UUID.fromString(NAME);
    private final BluetoothServerSocket mmServerSocket;

    public BluetoothServer(BluetoothAdapter mBluetoothAdapter) {
        // Use a temporary object that is later assigned to mmServerSocket,
        // because mmServerSocket is final
        BluetoothServerSocket tmp = null;
        try {
            // MY_UUID is the app's UUID string, also used by the client code
            tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
        } catch (IOException e) { }
        mmServerSocket = tmp;
    }

    public void run() {
        BluetoothSocket socket = null;
        while (true) {
            try {
                socket = mmServerSocket.accept();
                if (socket != null) {
                    BluetoothComm.start(socket);
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
