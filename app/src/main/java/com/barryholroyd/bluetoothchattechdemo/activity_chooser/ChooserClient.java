package com.barryholroyd.bluetoothchattechdemo.activity_chooser;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import com.barryholroyd.bluetoothchattechdemo.bluetooth.BluetoothUtils;
import com.barryholroyd.bluetoothchattechdemo.support.Support;

import java.io.IOException;
import java.util.Locale;

import static com.barryholroyd.bluetoothchattechdemo.activity_chooser.ChooserListener.MY_UUID;

/**
 * Bluetooth "chat" client connection set up.
 * <p>
 *     Given a remote device, create a connection to it and then call start
 *     ChatActivity to connect a chat session.
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

