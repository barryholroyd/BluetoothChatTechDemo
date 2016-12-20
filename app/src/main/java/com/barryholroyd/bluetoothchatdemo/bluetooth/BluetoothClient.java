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
 * Bluetooth "chat" client implementation.
 * <p>
 *     Runs in a background thread.
 */
public class BluetoothClient extends Thread
{
    /** client socket */
    private static BluetoothSocket mSocket = null;

    /** remote Bluetooth device */
    private static BluetoothDevice btdevice;

    /** Constructor for initialization. */
    public BluetoothClient(BluetoothDevice _btdevice) {
        btdevice = _btdevice;
        if ((mSocket != null) && mSocket.isConnected()) {
            Support.userMessage("Dropping current connection...");
            closeSocket(mSocket);
        }
    }

    /**
     * Create a connection to a remove Bluetooth server and the pass it to BluetoothComm
     * to run the chat session.
     */
    public void run() {
        /*
         * Discovery is very intensive -- it can slow down the connection attempt
         * and cause it to fail. To prevent that, if discovery is running we cancel it
         * before attempting to make a connection.
         */
        final BluetoothAdapter mBluetoothAdapter = MainActivity.getBluetoothAdapter();
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
            Support.log("Client cancelling discovery...");
        }

        try{
            mSocket = btdevice.createRfcommSocketToServiceRecord( MY_UUID );
            mSocket.connect( );
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
                        "Could not connect to remote device %s:%s. Is %s running on it?",
                        btdevice.getName(), btdevice.getAddress(), Support.getAppLabel());
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
        }

        // Start communications.
        Support.userMessage("Connected!");
        (new BluetoothComm("CLIENT", mSocket)).start();
    }

    /** Local method to close the socket if it hasn't been passed to BluetoothComm yet. */
    private void closeSocket(BluetoothSocket socket) {
        try   {
            if (socket != null) {
                Support.log("Closing the client socket...");
                socket.close();
            }
        }
        catch (IOException ioe) {
            Support.log(String.format(Locale.US,
                    "*** Failed to close the client connection: %s", ioe.getMessage()));
        }
    }
}
