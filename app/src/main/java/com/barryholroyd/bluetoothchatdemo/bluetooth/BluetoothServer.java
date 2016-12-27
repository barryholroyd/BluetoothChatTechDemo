package com.barryholroyd.bluetoothchatdemo.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;

import com.barryholroyd.bluetoothchatdemo.ApplicationGlobalState;
import com.barryholroyd.bluetoothchatdemo.ChatActivity;
import com.barryholroyd.bluetoothchatdemo.MainActivity;
import com.barryholroyd.bluetoothchatdemo.support.ActivityTracker;
import com.barryholroyd.bluetoothchatdemo.support.Support;

import java.io.IOException;
import java.util.UUID;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

/**
 * Bluetooth "chat" server implementation.
 * <p>
 *     Runs in a background thread.
 */

public class BluetoothServer extends Thread
{
    // app name used to connect
    public static final String SERVICE_NAME = "BluetoothChatDemo";

    // UUID generated at: https://www.uuidgenerator.net/
    public static final UUID MY_UUID = UUID.fromString("bb303707-5a56-4536-8d07-7ead8264f6b9");
    private final BluetoothServerSocket mServerSocket;

    public BluetoothServer(BluetoothAdapter mBluetoothAdapter) {
        // Use a temporary object that is later assigned to mServerSocket,
        // because mServerSocket is final
        // TBD: tmp not needed?
        BluetoothServerSocket tmp = null;
        try {
            // MY_UUID is the app's UUID string, also used by the client code
            tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(SERVICE_NAME, MY_UUID);
        } catch (IOException e) {
            Support.fatalError("Failed to get Bluetooth server socket.");
        }
        mServerSocket = tmp;
    }

    /**
     * Accept a connection from a remote Bluetooth client and the pass it to ChatActivity
     * to run the chat session.
     */
    public void run() {
        BluetoothSocket mSocket;
        while (true) {
            try {
                mSocket = mServerSocket.accept();
                if (mSocket != null) {
                    // Make the Bluetooth socket available to other components.
                    MainActivity.getApplicationGlobalState().setBtSocket(mSocket);

                    // Start communications.
                    Support.userMessage("Connected!");

                    // Pass control to the chat Activity.
                    Context c = ActivityTracker.getAppContext();
                    Intent intent = new Intent(c, ChatActivity.class);
                    intent.putExtra(Support.BUNDLE_KEY_BTDEVICE, (Parcelable) null);
                    intent.addFlags(FLAG_ACTIVITY_NEW_TASK); // required since an App Context is used
                    c.startActivity(intent);
                    break;
                }
            } catch (IOException e) {
                break;
            }
        }
    }
}
