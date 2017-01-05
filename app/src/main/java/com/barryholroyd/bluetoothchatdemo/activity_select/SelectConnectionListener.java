package com.barryholroyd.bluetoothchatdemo.activity_select;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;

import com.barryholroyd.bluetoothchatdemo.activity_chat.ChatActivity;
import com.barryholroyd.bluetoothchatdemo.bluetooth.BluetoothUtils;
import com.barryholroyd.bluetoothchatdemo.support.ActivityTracker;
import com.barryholroyd.bluetoothchatdemo.support.Support;

import java.io.IOException;
import java.util.UUID;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

/**
 *  * Bluetooth "chat" server connection set up.
 * <p>
 *     Listen for an incoming connection request, accept it and then call start
 *     ChatActivity to run a chat session.
 * <p>
 *     Runs as a background thread.
 */
public class SelectConnectionListener extends Thread
{
    /** app name used to connect */
    static private final String SERVICE_NAME = "BluetoothChatDemo";

    /** UUID generated at: https://www.uuidgenerator.net/. Used by both client and server code. */
    static final UUID MY_UUID = UUID.fromString("bb303707-5a56-4536-8d07-7ead8264f6b9");

    static private BluetoothServerSocket btServerSocket = null;

    static void startListener() {
        if (BluetoothUtils.isEnabled()) {
            Support.trace("Starting listener...");
            (new SelectConnectionListener()).start();
        }
        else {
            Support.trace(
                    "Not starting select listener because Bluetooth is not currently enabled.");
        }
    }

    static void stopListener() {
        Support.trace("Stopping listener...");
        closeSocket();
    }

    /**
     * Accept a connection from a remote Bluetooth client and then pass it to ChatActivity
     * to run the chat session.
     */
    public void run() {
        try {
            Support.trace("Creating new server socket...");
            btServerSocket = BluetoothUtils.getBluetoothAdapter().
                    listenUsingRfcommWithServiceRecord(SERVICE_NAME, MY_UUID);
        } catch (IOException e) {
            // TBD: does fatalError work? If not, just always also throw an exception.
            Support.fatalError("Failed to get Bluetooth server socket.");
            return;
        }

        // to keep the compiler happy
        if (btServerSocket == null) {
            Support.fatalError("Failed to get Bluetooth server socket.");
            return;
        }

        // Server socket. ChatServer is responsible for closing it.
        //noinspection InfiniteLoopStatement
        BluetoothSocket btSocket = null;

        if (!BluetoothUtils.isEnabled()) {
            Support.userMessage("Connection dropped.");
            return;
        }
        Support.trace("Server: waiting for a new connection to accept...");

        try {
            btSocket = btServerSocket.accept();
        }
        catch (IOException ioe) {
            // Caused by closing the BluetoothServerSocket.
            Support.trace("Listener: exiting...");
            return;
        }

        BluetoothDevice btdevice;
        // TBD: test fatalError
        if (btSocket == null) {
            Support.fatalError("Failed to get Bluetooth socket.");
            Support.userMessage("*** Failed to get Bluetooth socket."); // TBD:
            return;
        }
        else {
            btdevice = btSocket.getRemoteDevice();
        }

        /*
         * Make the Bluetooth socket available to ChatActivity.
         * ChatActivity is responsible for closing it.
         */
        SelectActivity.getApplicationGlobalState().setBtSocket(btSocket);

        Support.userMessage("Connected!");

        // Pass control to the chat Activity.
        Context c = ActivityTracker.getAppContext();
        Intent intent = new Intent(c, ChatActivity.class);
        intent.putExtra(ChatActivity.BUNDLE_KEY_BTDEVICE, btdevice);
        intent.addFlags(FLAG_ACTIVITY_NEW_TASK); // required since an App Context is used
        c.startActivity(intent);
    }

    /**
     * Close the listening BluetoothServerSocket. Per the BluetoothServerSocket
     * reference page, its close() method must be used to abort its accept() method
     * (the worker thread's interrupt() method is ignored).
     */
    private static void closeSocket() {
        if (btServerSocket == null) {
            throw new IllegalStateException("Bluetooth server socket already closed.");
        }
        try {
            btServerSocket.close();
        }
        catch (Exception e) {
            Support.exception("Exception attempting to close Bluetooth server socket", e);
        }
    }
}
