package com.barryholroyd.bluetoothchatdemo.activity_chooser;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;

import com.barryholroyd.bluetoothchatdemo.bluetooth.BluetoothUtils;
import com.barryholroyd.bluetoothchatdemo.support.ApplicationGlobalState;
import com.barryholroyd.bluetoothchatdemo.activity_chat.ChatActivity;
import com.barryholroyd.bluetoothchatdemo.support.Support;

import java.io.IOException;
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
    /** Chat client socket. ChatServer is responsible for closing it. */
    private static BluetoothSocket btChatSocket = null;

    /**
     * Create a connection to a remote Bluetooth server and then pass it to ChatServer
     * to connect the chat session. This thread exits after spawning the communication thread.
     *
     * The check to ensure that Bluetooth is enabled is done before starting this worker thread.
     *
     * @param btdevice remote Bluetooth device.
     */
    static void connect(Activity a, BluetoothDevice btdevice) {
        if ((btChatSocket != null) && btChatSocket.isConnected()) {
            Support.userMessageShort("Dropping current connection...");
            closeSocket(btChatSocket);
        }

        /*
         * Discovery is very intensive -- it can slow down the connection attempt
         * and cause it to fail. To prevent that, if discovery is running we cancel it
         * before attempting to make a connection.
         */
        final BluetoothAdapter mBluetoothAdapter = BluetoothUtils.getBluetoothAdapter();
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
            Support.userMessageShort("Cancelling discovery to connect...");
        }

        Support.userMessageShort("Connecting...");
        /*
         * Create a Bluetooth socket by providing an SDP UUID -- that will be used to
         * select a channel.
         */
        try {
            btChatSocket = btdevice.createRfcommSocketToServiceRecord( MY_UUID );
            btChatSocket.connect( );
        } catch ( IOException ioe ) {
            try {
                if (btChatSocket != null) {
                    btChatSocket.close();
                }
                Support.userMessageShort("Could not connect...");
            } catch ( IOException ioe2 ) {
                String msg = String.format(Locale.US, "IOException: %s", ioe2.getMessage());
                Support.userMessageLong(msg);
            }
            return;
        }

        Support.userMessageShort("Connected!");

        saveBtChatSocket(a, btChatSocket);

        /*
          * Pass control to the chat Activity.
          * ChooserListener will be stopped by ChooserActivity.onStop().
          */
        Intent intent = new Intent(a, ChatActivity.class);
        intent.putExtra(ChatActivity.BUNDLE_KEY_BTDEVICE, btdevice);
        intent.addFlags(FLAG_ACTIVITY_NEW_TASK); // required since an App Context is used
        a.startActivity(intent);
    }

    /** Local method to close the btChatSocket if it hasn't been passed to ChatServer yet. */
    static private void closeSocket(BluetoothSocket btChatSocket) {
        try   {
            if (btChatSocket != null) {
                btChatSocket.close();
            }
        }
        catch (IOException ioe) {
            Support.exception("Failed to close client's btChatSocket", ioe);
        }
    }

    static private void saveBtChatSocket(Activity a, BluetoothSocket btChatSocket) {
        ((ApplicationGlobalState)  a.getApplication()).setBtChatSocket(btChatSocket);
    }

    private static void trace(String msg) {
        Support.trace("ChooserClient: " + msg);
    }
}

