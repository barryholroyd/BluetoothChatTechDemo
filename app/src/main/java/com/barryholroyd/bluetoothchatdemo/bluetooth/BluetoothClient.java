package com.barryholroyd.bluetoothchatdemo.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import com.barryholroyd.bluetoothchatdemo.MainActivity;
import com.barryholroyd.bluetoothchatdemo.Support;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Locale;

import static com.barryholroyd.bluetoothchatdemo.bluetooth.BluetoothServer.MY_UUID;

/**
 * Bluetooth client implementation for chatting.
 */

public class BluetoothClient extends Thread
{
    private BluetoothSocket mSocket = null;
    private final BluetoothAdapter mBluetoothAdapter;

    BluetoothDevice d;

    public BluetoothClient(Activity a, BluetoothDevice device) {
        mBluetoothAdapter = MainActivity.getBluetoothAdapter();

        d = device;
        // TBD: restore?
//        if ((mSocket != null) && mSocket.isConnected()) {
//            Support.userMessage(a, "Dropping current connection...");
//            closeSocket(mSocket);
//        }
//        try {
//            mSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
//        } catch (IOException e) { }
    }

    public void run() {
        // Cancel discovery if in progress.
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
            Support.log("Client cancelling discovery...");
        }
        // TBD: restore?
//        try {
//            Support.log("Client connecting...");
//            mSocket.connect();
//            Support.log("Client connected...");
//        } catch (IOException ioe) {
//            Support.log(String.format(Locale.US, "Client IOException: %s", ioe.getMessage()));
//            closeSocket(mSocket);
//            return;
//        }

        try{
            mSocket = d.createRfcommSocketToServiceRecord( MY_UUID );
            mSocket.connect( );
        } catch ( IOException exception ){
            Method m = null;
            Support.log("*** Attempting alternate approach to connect...");
            try {
                m = d.getClass().getMethod("createRfcommSocket", new Class[] {int.class});
                mSocket = (BluetoothSocket) m.invoke(d, 1);
                mSocket.connect();
            } catch (Exception e) {
                Support.log(String.format(Locale.US, "EXCEPTION: %s"));
                return;
            }
            Support.log("*** Connection succeeded!");
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
