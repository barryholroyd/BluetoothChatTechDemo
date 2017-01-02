package com.barryholroyd.bluetoothchatdemo.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;

import com.barryholroyd.bluetoothchatdemo.ChatActivity;
import com.barryholroyd.bluetoothchatdemo.MainActivity;
import com.barryholroyd.bluetoothchatdemo.support.ActivityTracker;
import com.barryholroyd.bluetoothchatdemo.support.Support;

import java.io.IOException;
import java.util.Locale;
import java.util.UUID;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

/**
 * Bluetooth "server": listens for incoming connection, then starts a chat session.
 * <p>
 *     Runs in a background thread.
 */
public class BluetoothServer extends Thread
{
    /** app name used to connect */
    static private final String SERVICE_NAME = "BluetoothChatDemo";

    /** UUID generated at: https://www.uuidgenerator.net/. Used by both client and server code. */
    static final UUID MY_UUID = UUID.fromString("bb303707-5a56-4536-8d07-7ead8264f6b9");

    /**
     * Class used for coordinating server worker thread with foreground ChatActivity thread.
     * <p>
     *     The server thread waits on this lock after accepting a connection and initiating
     *     a ChatActivity activity. The ChatActivity activity then notifies this thread, via
     *     the Lock instance, when it is done, so that the server can resume waiting for
     *     another connection to accept.
     */
    static public class Lock
    {
        private boolean condition = false;
        boolean conditionMet() { return condition; }
        public void setCondition(boolean _condition) { condition = _condition; }
    }
    public static final Lock serverLock = new Lock();

    /**
     * Accept a connection from a remote Bluetooth client and then pass it to ChatActivity
     * to run the chat session.
     */
    public void run() {
        BluetoothServerSocket btServerSocket = null;

        try {
            Support.log("Creating new server socket...");
            btServerSocket = BluetoothMgr.getBluetoothAdapter().
                    listenUsingRfcommWithServiceRecord(SERVICE_NAME, MY_UUID);
        } catch (IOException e) {
            Support.fatalError("Failed to get Bluetooth server socket.");
        }

        //noinspection InfiniteLoopStatement
        while (true) {
            try {
                Support.log("Server: waiting for a new connection to accept...");

                @SuppressWarnings("ConstantConditions")
                BluetoothSocket btSocket = btServerSocket.accept();

                if (btSocket == null) {
                    Support.fatalError("Failed to get Bluetooth socket.");
                }

                // Make the Bluetooth socket available to ChatActivity.
                MainActivity.getApplicationGlobalState().setBtSocket(btSocket);

                // Start communications.
                Support.userMessage("Connected!");

                // Pass control to the chat Activity.
                Context c = ActivityTracker.getAppContext();
                Intent intent = new Intent(c, ChatActivity.class);
                intent.putExtra(ChatActivity.BUNDLE_KEY_BTDEVICE, (Parcelable) null);
                intent.addFlags(FLAG_ACTIVITY_NEW_TASK); // required since an App Context is used
                c.startActivity(intent);

                synchronized (serverLock) {
                    while (!serverLock.conditionMet()) {
                        try {
                            serverLock.wait();
                            Support.log("Resuming in server after wait()...");
                        } catch (InterruptedException ioe) {
                            String msg = String.format(Locale.US,
                                    "Spurious interrupt exception while waiting on server lock: %s",
                                    ioe.getMessage());
                            Support.log(msg);
                        }
                    }
                    serverLock.setCondition(false);
                }
            } catch (IOException ioe) {
                String msg = String.format(Locale.US,
                        "Server connection IO exception: %s", ioe.getMessage());
                Support.fatalError(msg);
            }
        }
    }
}
