package com.barryholroyd.bluetoothdemo.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import com.barryholroyd.bluetoothdemo.Support;

import java.io.IOException;

import static com.barryholroyd.bluetoothdemo.bluetooth.BluetoothServer.MY_UUID;

/**
 * Bluetooth client implementation for chatting.
 */

public class BluetoothClient extends Thread
{
    private BluetoothSocket mSocket = null;
    private final BluetoothAdapter mBluetoothAdapter;

    public BluetoothClient(Activity a, BluetoothAdapter _mBluetoothAdapter, BluetoothDevice device) {
        mBluetoothAdapter = _mBluetoothAdapter;
        if ((mSocket != null) && mSocket.isConnected()) {
            Support.userMsg(a, "Dropping current connection...");
            closeSocket(mSocket);
        }
        try {
            mSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) { }
    }

    public void run() {
        // Cancel discovery if in progress.
        mBluetoothAdapter.cancelDiscovery();
        try {
            mSocket.connect();
        } catch (IOException connectException) {
            closeSocket(mSocket);
            return;
        }

        // Start communications.
        BluetoothComm.start(mSocket);
    }

    private void closeSocket(BluetoothSocket bs) {
        try   { bs.close(); }
        catch (IOException closeException) { }
    }

    public void cancel() {
        closeSocket(mSocket);
    }
}
