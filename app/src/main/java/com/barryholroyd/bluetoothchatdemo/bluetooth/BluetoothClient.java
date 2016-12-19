package com.barryholroyd.bluetoothchatdemo.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import com.barryholroyd.bluetoothchatdemo.MainActivity;
import com.barryholroyd.bluetoothchatdemo.support.Support;

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
    private BluetoothDevice btdevice;

    public BluetoothClient(BluetoothDevice _btdevice) {
        btdevice = _btdevice;
        if ((mSocket != null) && mSocket.isConnected()) {
            Support.userMessage("Dropping current connection...");
            closeSocket(mSocket);
        }
    }

    public void run() {
        // Cancel discovery if in progress.
        final BluetoothAdapter mBluetoothAdapter = MainActivity.getBluetoothAdapter();
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
            Support.log("Client cancelling discovery...");
        }

        // TBD: provide user with message indicating other end may not be running the app.
        try{
            mSocket = btdevice.createRfcommSocketToServiceRecord( MY_UUID );
            Support.log("Client connecting...");
            mSocket.connect( );
            Support.log("Client connected...");
        } catch ( IOException ioe ){
            Support.log(String.format(Locale.US, "Client IOException: %s", ioe.getMessage()));

            /*
             * This is a workaround for a bug in Google's Bluetooth library implementation.
             * See: https://code.google.com/p/android/issues/detail?id=41415.
             */
            Support.log("*** Attempting alternate approach to connect...");
            // TBD: understand this.
            try {
                Method m = btdevice.getClass().getMethod("createRfcommSocket",
                        new Class[] {int.class});
                mSocket = (BluetoothSocket) m.invoke(btdevice, 1);
                mSocket.connect();
            } catch (IOException ioe2) {
                String msg = String.format(Locale.US,
                        "Could not connect to remote device. Is %s running on the remote device?",
                        Support.getAppLabel());
                Support.userMessage(msg);
                closeSocket(mSocket);
                return;
            }
            catch (Exception e) {
                String msg = String.format(Locale.US, "Exception: %s", e.getMessage());
                Support.userMessage(msg);
                closeSocket(mSocket);
                return;
            }
            Support.log("*** Connection succeeded!");
        }

        // Start communications.
        Support.userMessage("Connected!");
        (new BluetoothComm("CLIENT", mSocket)).start();
    }

    private void closeSocket(BluetoothSocket bs) {
        try   { bs.close(); }
        catch (IOException closeException) { }
    }

    public void cancel() {
        closeSocket(mSocket);
    }
}
