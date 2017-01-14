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
import com.barryholroyd.bluetoothchatdemo.support.GlobalState;
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
public class ChooserClient extends Thread
{
    /** The remote Bluetooth device. */
    private BluetoothDevice btdevice;

    /** Chat client socket. ChatServer is responsible for closing it. */
    private BluetoothSocket btChatSocket = null;

    /**
     * Create a client connection to a remote device, then kick off
     * the ChatActivity to run the Chat session.
     *
     * @param _btdevice remote Bluetooth device.
     */
    ChooserClient(BluetoothDevice _btdevice) {
        btdevice = _btdevice;
    }

    /**
     * Create a connection to a remote Bluetooth server and then pass it to ChatServer
     * to connect the chat session. This thread exits after spawning the communication thread.
     *
     * The check to ensure that Bluetooth is enabled is done before starting this worker thread.
     *
     */
    @Override
    public void run() {
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
                Support.userMessageShort("Could not connect.");
            } catch ( IOException ioe2 ) {
                String msg = String.format(Locale.US, "IOException: %s", ioe2.getMessage());
                Support.userMessageLong(msg);
            }
            return;
        }

        /*
        * Pass control to the chat Activity.
        *
        * ChooserListener is running, waiting for an incoming connection, but it
        * will be stopped by ChooserActivity.onStop(). (Ideally it should be stopped
        * while an outgoing connection attempt is being made, but that is beyond
        * the scope of this demo.)
        */
        ChooserSupport.startChatActivity(btChatSocket);
    }

    /** Local method to close the btChatSocket if it hasn't been passed to ChatServer yet. */
    private void closeSocket(BluetoothSocket btChatSocket) {
        try   {
            if (btChatSocket != null) {
                btChatSocket.close();
            }
        }
        catch (IOException ioe) {
            Support.exception("Failed to close client's btChatSocket", ioe);
        }
    }

    private void trace(String msg) {
        Support.trace("ChooserClient: " + msg);
    }
}

