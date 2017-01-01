package com.barryholroyd.bluetoothchatdemo.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;

import com.barryholroyd.bluetoothchatdemo.ApplicationGlobalState;
import com.barryholroyd.bluetoothchatdemo.ChatActivity;
import com.barryholroyd.bluetoothchatdemo.MainActivity;
import com.barryholroyd.bluetoothchatdemo.support.ActivityTracker;
import com.barryholroyd.bluetoothchatdemo.support.Support;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Locale;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static com.barryholroyd.bluetoothchatdemo.bluetooth.BluetoothServer.MY_UUID;

/**
 * Bluetooth "chat" client connection set up.
 * <p>
 *     Runs in a background thread so that the user can still do other stuff (e.g.,
 *     adjust settings) even if the write to the network hangs.
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
     * Create a connection to a remote Bluetooth server and then pass it to BluetoothComm
     * to run the chat session.
     */
    public void run() {
        /*
         * Discovery is very intensive -- it can slow down the connection attempt
         * and cause it to fail. To prevent that, if discovery is running we cancel it
         * before attempting to make a connection.
         */
        final BluetoothAdapter mBluetoothAdapter = BluetoothMgr.getBluetoothAdapter();
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
            Support.log("Client cancelling discovery...");
        }

        Support.userMessage("Connecting...");

        try{
            mSocket = btdevice.createRfcommSocketToServiceRecord( MY_UUID );
            Support.log("*** Attempting main approach to connect...");
            mSocket.connect( );
        } catch ( IOException ioe ) {
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
                Support.log(String.format(Locale.US,
                        "Client connection ready: %#x", mSocket.hashCode()));

            } catch (IOException ioe2) {
                if (mSocket == null) {
                    Support.log("Client connection exception: <null>");
                }
                else {
                    Support.log(String.format(Locale.US,
                            "Client connection exception: %#x", mSocket.hashCode()));
                }
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

        // Get a valid Context.
        Context c = ActivityTracker.getAppContext();
        if (c == null) {
            Support.userMessage("Could not start chat -- foreground Activity is gone.");
            closeSocket(mSocket);
            return;
        }

        // Make the Bluetooth socket available to other components.
        ((ApplicationGlobalState)  MainActivity.getActivity().getApplication()).setBtSocket(mSocket);

        // Pass control to the chat Activity.
        Intent intent = new Intent(c, ChatActivity.class);
        intent.putExtra(Support.BUNDLE_KEY_BTDEVICE, btdevice);
        intent.addFlags(FLAG_ACTIVITY_NEW_TASK); // required since an App Context is used
        c.startActivity(intent);
        btdevice = null;
    }

    /** Local method to close the socket if it hasn't been passed to BluetoothComm yet. */
    private void closeSocket(BluetoothSocket socket) {
        btdevice = null;
        Support.log("Attempting to close the client socket...");
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
