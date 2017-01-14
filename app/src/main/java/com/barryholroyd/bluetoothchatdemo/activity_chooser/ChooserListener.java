package com.barryholroyd.bluetoothchatdemo.activity_chooser;

import android.app.Activity;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;

import com.barryholroyd.bluetoothchatdemo.activity_chat.ChatActivity;
import com.barryholroyd.bluetoothchatdemo.bluetooth.BluetoothUtils;
import com.barryholroyd.bluetoothchatdemo.support.Support;

import java.io.IOException;
import java.util.UUID;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

/**
 *  * Bluetooth listener to receive incoming connection requests.
 * <p>
 *     Listen for an incoming connection request, accept it and then call start
 *     ChatActivity to connect a chat session.
 * <p>
 *     Runs as a background thread. We only ever need one of these.
 */
class ChooserListener extends Thread
{
    /** app name used to connect */
    static private final String SERVICE_NAME = "BluetoothChatDemo";

    /** UUID generated at: https://www.uuidgenerator.net/. Used by both client and server code. */
    static final UUID MY_UUID = UUID.fromString("bb303707-5a56-4536-8d07-7ead8264f6b9");

    static private ChooserListener chooserListener = null;

    static private BluetoothServerSocket btServerSocket = null;

    static void startListener() {
        if (BluetoothUtils.isEnabled()) {
            trace("starting...");
            chooserListener = new ChooserListener();
            chooserListener.start();
        }
    }
    /**
     * Close the listening BluetoothServerSocket. Per the BluetoothServerSocket
     * reference page, its close() method must be used to abort its accept() method
     * (the worker thread's interrupt() method is ignored).
     */
    static void stopListener() {
        // btServerSocket can be null if Bluetooth wasn't turned on when the app was started.
        if (btServerSocket == null)
            return;

        trace("stopping...");
        try {
                btServerSocket.close();
        }
        catch (Exception e) {
            Support.exception(
                    "Listener: exception attempting to close Bluetooth server socket", e);
        }
        finally {
            btServerSocket = null;
        }
    }

    /**
     * Accept a connection from a remote Bluetooth client and then pass it to ChatActivity
     * to connect the chat session.
     */
    public void run() {
        try {
            trace("creating new server socket...");
            btServerSocket = BluetoothUtils.getBluetoothAdapter().
                    listenUsingRfcommWithServiceRecord(SERVICE_NAME, MY_UUID);
        } catch (IOException e) {
            reportError("Listener: failed to get Bluetooth server socket.");
            return;
        }

        // to keep the compiler happy
        if (btServerSocket == null) {
            reportError("Listener: failed to get Bluetooth server socket.");
        }

        // Bluetooth socket. ChatServer is responsible for closing it.
        //noinspection InfiniteLoopStatement
        BluetoothSocket btChooserSocket = null;

        if (!BluetoothUtils.isEnabled()) {
            Support.userMessageLong("Listener: connection dropped.");
            return;
        }
        trace("waiting for a connection...");

        try {
            btChooserSocket = btServerSocket.accept();
        }
        catch (IOException ioe) {
            /*
             * From https://developer.android.com/guide/topics/connectivity/
             *      bluetooth.html#ConnectingDevices:
             *   To abort a blocked call such as accept(), call close() on the
             *   BluetoothServerSocket or BluetoothSocket from another thread.
             */
            trace("exiting...");
            return;
        }
        finally {
            /*
             * From https://developer.android.com/guide/topics/connectivity/
             *      bluetooth.html#ConnectingDevices:
             *   This method call [close()] releases the server socket and all its resources,
             *   but doesn't close the connected BluetoothSocket that's been returned
             *   by accept(). Unlike TCP/IP, RFCOMM allows only one connected client
             *   per channel at a time, so in most cases, it makes sense to call close()
             *   on the BluetoothServerSocket immediately after accepting a connected socket.
             */
            try {
                if (btServerSocket != null)
                    btServerSocket.close();
            }
            catch (IOException ioe) {
                trace("exiting...");
            }
        }

        if (btChooserSocket == null) {
            reportError("Failed to get Bluetooth socket.");
        }
        else {
            ChooserSupport.startChatActivity(btChooserSocket);
        }
    }

    private static void reportError(String msg) {
        Activity a = ChooserActivity.getActivity();
        if (a == null) {
            Support.error(msg);
            throw new IllegalStateException("reportError: " + msg);
        }
        else {
            Support.fatalError(a, msg);
        }
    }

    private static void trace(String msg) {
        Support.trace("Listener: " + msg);
    }
}
