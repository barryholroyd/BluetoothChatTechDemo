package com.barryholroyd.bluetoothchatdemo.activity_chooser;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;

import com.barryholroyd.bluetoothchatdemo.bluetooth.BluetoothUtils;
import com.barryholroyd.bluetoothchatdemo.support.ApplicationGlobalState;
import com.barryholroyd.bluetoothchatdemo.activity_chat.ChatActivity;
import com.barryholroyd.bluetoothchatdemo.support.ActivityTracker;
import com.barryholroyd.bluetoothchatdemo.support.Support;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Locale;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static com.barryholroyd.bluetoothchatdemo.activity_chooser.ChooserListener.MY_UUID;

/**
 * Bluetooth "chat" client connection set up.
 * <p>
 *     Given a remote device, create a connection to it and then call start
 *     ChatActivity to connect a chat session.
 * <p>
 *     This was originally a worker thread, but since only one connection is
 *     allowed at a time that only introduced a lot of complexity, so I now
 *     do the connection set up on the main thread and make the user wait.
 */
public class ChooserClient
{
    /** Client socket. ChatServer is responsible for closing it. */
    private static BluetoothSocket btSocket = null;

    /**
     * Create a connection to a remote Bluetooth server and then pass it to ChatServer
     * to connect the chat session. This thread exits after spawning the communication thread.
     *
     * The check to ensure that Bluetooth is enabled is done before starting this worker thread.
     *
     * @param btdevice remote Bluetooth device.
     */
    static public void connect(BluetoothDevice btdevice) {
        if ((btSocket != null) && btSocket.isConnected()) {
            Support.userMessage("Dropping current connection...");
            closeSocket(btSocket);
        }

        /*
         * Discovery is very intensive -- it can slow down the connection attempt
         * and cause it to fail. To prevent that, if discovery is running we cancel it
         * before attempting to make a connection.
         */
        final BluetoothAdapter mBluetoothAdapter = BluetoothUtils.getBluetoothAdapter();
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
            Support.trace("Client cancelling discovery...");
        }

        Support.userMessage("Connecting...");

        try{
            btSocket = btdevice.createRfcommSocketToServiceRecord( MY_UUID );
            Support.trace("Attempting main approach to connect...");
            btSocket.connect( );
        } catch ( IOException ioe ) {
            /*
             * This is a workaround for a bug in Google's Bluetooth library implementation.
             *   See: https://code.google.com/p/android/issues/detail?id=41415.
             * I'm not sure why this works, but from a look at the source code for
             * BluetoothDevice, the direct call to createRfcommSocket() effectively bypasses
             * the use of the UUID to select a channel -- createRfcommSocket() simply uses
             * the channel number passed in, in this case, '1'.
             */
            Support.trace("***** Attempting alternate approach to connect..."); // TBD: when is this used?
            try {
                Method m = btdevice.getClass().getMethod("createRfcommSocket", int.class);
                btSocket = (BluetoothSocket) m.invoke(btdevice, 1);
                btSocket.connect();
            } catch (IOException ioe2) {
                String msg = String.format(Locale.US,
                        "Could not connect to remote device %s:%s. Is %s running on it?",
                        btdevice.getName(), btdevice.getAddress(), Support.getAppLabel());
                Support.userMessage(msg);
                closeSocket(btSocket);
                return;
            }
            catch (Exception e) {
                String msg = String.format(Locale.US, "Exception: %s", e.getMessage());
                Support.userMessage(msg);
                closeSocket(btSocket);
                return;
            }
        }

        Support.userMessage("Connected!");

        // Get a valid Context.
        Context c = ActivityTracker.getAppContext();
        if (c == null) {
            Support.userMessage("Could not start chat -- foreground Activity is gone.");
            closeSocket(btSocket);
            return;
        }

        // Make the Bluetooth socket available to other components.
        Activity a = ChooserActivity.getActivity();
        if (a == null) {
            Support.userMessage("Internal (temporary) error: could not get Activity.");
            closeSocket(btSocket);
            return;
        }
        ((ApplicationGlobalState)  a.getApplication()).setBtSocket(btSocket);

        // Pass control to the chat Activity.
        Intent intent = new Intent(c, ChatActivity.class);
        intent.putExtra(ChatActivity.BUNDLE_KEY_BTDEVICE, btdevice);
        intent.addFlags(FLAG_ACTIVITY_NEW_TASK); // required since an App Context is used
        c.startActivity(intent);
    }

    /** Local method to close the btSocket if it hasn't been passed to ChatServer yet. */
    static private void closeSocket(BluetoothSocket btSocket) {
        try   {
            if (btSocket != null) {
                btSocket.close();
            }
        }
        catch (IOException ioe) {
            Support.exception("Failed to close client's btSocket", ioe);
        }
    }
}

