package com.barryholroyd.bluetoothchatdemo.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import com.barryholroyd.bluetoothchatdemo.Support;

import java.io.IOException;

import static com.barryholroyd.bluetoothchatdemo.bluetooth.BluetoothServer.MY_UUID;

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
            Support.userMessage(a, "Dropping current connection...");
            closeSocket(mSocket);
        }
        try {
            mSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) { }
    }

    public void run() {
        // Cancel discovery if in progress.
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
            Support.log("Client cancelling discovery...");
        }
        try {
            Support.log("Client connecting...");
            mSocket.connect();
            Support.log("Client connected...");
        } catch (IOException connectException) {
            Support.log("Client IOException...");
            closeSocket(mSocket);
            return;
        }

        // Start communications.
        Support.log("Client starting communications...");
        BluetoothComm.start("CLIENT", mSocket);
    }

    private void closeSocket(BluetoothSocket bs) {
        try   { bs.close(); }
        catch (IOException closeException) { }
    }

    public void cancel() {
        closeSocket(mSocket);
    }
}
