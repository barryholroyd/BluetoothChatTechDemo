package com.barryholroyd.bluetoothchatdemo.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;

import com.barryholroyd.bluetoothchatdemo.ChatActivity;
import com.barryholroyd.bluetoothchatdemo.MainActivity;
import com.barryholroyd.bluetoothchatdemo.support.ActivityTracker;
import com.barryholroyd.bluetoothchatdemo.support.Support;

import java.io.IOException;
import java.util.Locale;
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
public class BluetoothServer extends Thread
{
    /** app name used to connect */
    static private final String SERVICE_NAME = "BluetoothChatDemo";

    /** UUID generated at: https://www.uuidgenerator.net/. Used by both client and server code. */
    static final UUID MY_UUID = UUID.fromString("bb303707-5a56-4536-8d07-7ead8264f6b9");

    /** Singleton -- only allow a single running server thread at a time. */
    static BluetoothServer bluetoothServer = null;

    /**
     * Class used by other threads to coordinate
     * with this server thread.
     * <p>
     *     The server thread waits on this lock after accepting a connection and initiating
     *     a ChatActivity activity. The ChatActivity thread then notifies this thread, via
     *     the Lock instance, when it is done, so that the server can resume waiting for
     *     another connection to accept.
     * <p> TBD: BR is separate thread from UI? (NO)
     * DEL:?
     *     The BluetoothBroadcastReceiver thread sends an interrupt to this thread when
     *     Bluetooth is turned off on the device so that this thread can exit.
     */
    static public class Lock
    {
        private boolean condition = false;
        private boolean exitFlag = false;

        boolean isChatDone() { return condition; }
        public void setChatDone(boolean _condition) { condition = _condition; }

        boolean getExitFlag() { return exitFlag; }
        void setExitFlag(boolean _exitFlag) { exitFlag = _exitFlag; }

    }
    public static final Lock serverLock = new Lock();

    public static void manage(int btState) {
        switch (btState) {
            case BluetoothAdapter.STATE_ON:
                if (bluetoothServer != null) {
                    throw new IllegalStateException(
                            "Attempt to create a second running Bluetooth server.");
                }
                Support.trace("Starting server...");
                serverLock.setExitFlag(false);
                bluetoothServer = new BluetoothServer();
                bluetoothServer.start();
                break;
            default:
                if (bluetoothServer != null) {
                    Support.trace("Stopping server...");
                    serverLock.setExitFlag(true);
                    bluetoothServer.interrupt();
                    bluetoothServer = null;
                }
                break;
        }
    }

    /**
     * Accept a connection from a remote Bluetooth client and then pass it to ChatActivity
     * to run the chat session.
     */
    public void run() {
        BluetoothServerSocket btServerSocket = null;

        try {
            Support.trace("Creating new server socket...");
            btServerSocket = BluetoothUtils.getBluetoothAdapter().
                    listenUsingRfcommWithServiceRecord(SERVICE_NAME, MY_UUID);
        } catch (IOException e) {
            Support.fatalError("Failed to get Bluetooth server socket.");
        }

        // to keep the compiler happy
        if (btServerSocket == null) {
            Support.fatalError("Failed to get Bluetooth server socket.");
            return;
        }

        //noinspection InfiniteLoopStatement
        while (true) {
            BluetoothSocket btSocket = null;
            try {
                if (!BluetoothUtils.isEnabled()) {
                    Support.userMessage("Connection dropped.");
                    return;
                }
                Support.trace("Server: waiting for a new connection to accept...");

                btSocket = btServerSocket.accept();

                BluetoothDevice btdevice = null;
                if (btSocket == null) {
                    Support.fatalError("Failed to get Bluetooth socket.");
                }
                else {
                    // Get the remote device information.
                    btdevice = btSocket.getRemoteDevice();
                }

                // Make the Bluetooth socket available to ChatActivity.
                MainActivity.getApplicationGlobalState().setBtSocket(btSocket);

                // Start communications.
                Support.userMessage("Connected!");


                // Pass control to the chat Activity.
                Context c = ActivityTracker.getAppContext();
                Intent intent = new Intent(c, ChatActivity.class);
                intent.putExtra(ChatActivity.BUNDLE_KEY_BTDEVICE, btdevice);
                intent.addFlags(FLAG_ACTIVITY_NEW_TASK); // required since an App Context is used
                c.startActivity(intent);

                /*
                 * Wait for the ChatActivity to complete. Exit this thread if Bluetooth
                 * is turned off.
                 */
                synchronized (serverLock) {
                    while (!serverLock.isChatDone()) {
                        try {
                            Support.trace("Server: entering wait()...");
                            serverLock.wait();
                            Support.trace("Server: resuming after wait()...");
                        } catch (InterruptedException ioe) {
                            Support.trace("Server: InterruptedException caught...");
                            if (serverLock.getExitFlag()) {
                                Support.trace("Server: exiting...");
                                serverLock.setExitFlag(false);
                                return;
                            }
                            Support.exception("Server: spurious interrupt exception while waiting on server lock", ioe);
                        }
                    }
                    serverLock.setChatDone(false);
                }
            } catch (IOException ioe) {
                Support.exception("Server connection IO exception", ioe); // TBD:
            }
            finally {
                try {
                    if (btSocket != null) {
                        btSocket.close();
                    }
                }
                catch (IOException ioe2){
                    Support.exception("Server connection IO exception #2", ioe2); // TBD:
                }
                MainActivity.getApplicationGlobalState().setBtSocket(null);
            }
        }
    }
}
