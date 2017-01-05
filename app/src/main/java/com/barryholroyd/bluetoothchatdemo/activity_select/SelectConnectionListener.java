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
    static public final UUID MY_UUID = UUID.fromString("bb303707-5a56-4536-8d07-7ead8264f6b9");

    /** Singleton -- only allow a single running server thread at a time. */
    static SelectConnectionListener btListener = null;

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
     *     The ChatBroadcastReceiver thread sends an interrupt to this thread when
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
    public static final Lock listenerLock = new Lock();

    public static void startListener() {
        if (BluetoothUtils.isEnabled()) {
            if (btListener != null) {
                throw new IllegalStateException(
                        "Attempt to create a second running listener.");
            }
            Support.trace("Starting listener...");
            listenerLock.setExitFlag(false);
            btListener = new SelectConnectionListener();
            btListener.start();
        }
    }

    public static void stopListener() {
        if (BluetoothUtils.isEnabled()) {
            if (btListener == null) {
                throw new IllegalStateException(
                        "Attempt to stop a non-existent listener.");
            }
            Support.trace("Stopping listener...");
            listenerLock.setExitFlag(true);
            btListener.interrupt();
            btListener = null;
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
                    Support.userMessage("*** Failed to get Bluetooth socket.");} // TBD:
                else {
                    // Get the remote device information.
                    btdevice = btSocket.getRemoteDevice();
                }

                // Make the Bluetooth socket available to ChatActivity.
                SelectActivity.getApplicationGlobalState().setBtSocket(btSocket);

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
                synchronized (listenerLock) {
                    while (!listenerLock.isChatDone()) {
                        try {
                            Support.trace("Server: entering wait()...");
                            listenerLock.wait();
                            Support.trace("Server: resuming after wait()...");
                        } catch (InterruptedException ioe) {
                            Support.trace("Server: InterruptedException caught...");
                            if (listenerLock.getExitFlag()) {
                                Support.trace("Server: exiting...");
                                listenerLock.setExitFlag(false);
                                return;
                            }
                            Support.exception("Server: spurious interrupt exception while waiting on server lock", ioe);
                        }
                    }
                    listenerLock.setChatDone(false);
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
                SelectActivity.getApplicationGlobalState().setBtSocket(null);
            }
        }
    }
}
