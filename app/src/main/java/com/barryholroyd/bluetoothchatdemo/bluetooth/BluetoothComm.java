package com.barryholroyd.bluetoothchatdemo.bluetooth;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.TextView;

import com.barryholroyd.bluetoothchatdemo.ChatActivity;
import com.barryholroyd.bluetoothchatdemo.support.ActivityTracker;
import com.barryholroyd.bluetoothchatdemo.support.Support;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Locale;

/**
 * Bluetooth communications: send and receive text over a Bluetooth connection.
 * <p>
 *     This class is only used by ChatActivity, so any reference to an Activity here is
 *     always to the ChatActivity. Its constructor accepts an initialized Bluetooth btSocket,
 *     creates input and output streams from it, then starts running in the background to
 *     read incoming data on the input stream; incoming data is then sent to the UI thread
 *     for display to the user.
 * <p>
 *     A static writeChat() method is provided for the UI thread to use to write data out
 *     to the remote app.
 */

public class BluetoothComm extends Thread
{
    /** Bluetooth socket to be read and written. */
    private static BluetoothSocket btSocket;

    /** running is "true" when initialization is successful. */
    private static boolean running;

    /** Handler message: display incoming chat text. */
    private static final int CHATTEXT = 1;

    /** Buffer size for both input and output. */
    public static final int BUFSIZE = 1024;

    /** Handler for the UI thread. */
    private static Handler uiHandler = null;

    /** Handler to cause the calling ChatActivity to exit. */
    private final Handler caHandler;

    /** Bluetooth input stream. */
    private static InputStream btIn;

    /** Bluetooth output stream. */
    private static OutputStream btOut;

    /**
     * Constructor -- set up IO and UI handler.
     *
     * @param _btSocket Bluetooth socket to be read and written.
     * @param handler   Handler to cause the calling ChatActivity to exit.
     */
    public BluetoothComm(BluetoothSocket _btSocket, Handler handler) {
        btSocket = _btSocket;
        caHandler = handler;
        running = false;

        try {
            btIn = btSocket.getInputStream();
            btOut = btSocket.getOutputStream();
        }
        catch (IOException ioe) {
            String msg = String.format(Locale.US,
                    "Error: could not get input or output stream: %s",
                    ioe.getMessage());
            Support.userMessage(msg);
            return;
        }

        /*
         * Create the UI handler responsible for displaying the text on the UI thread.
         * ChatActivity should be running; processMessage() will check to make sure.
         */
        uiHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message message) {
                if (message.what == CHATTEXT) {
                    processMessage(message);
                }
                else {
                    String msg = String.format(Locale.US,
                            "Unexpected message type in BluetoothComm: %d.",
                            message.what);
                    throw new IllegalStateException(msg);
                }
            }
        };
        running = true;
    }

    /**
     * Convert the incoming message into a text string and display it.
     * Called by the UI handler.
     *
     * @param m the Message containing the text to be displayed.
     */
    private void processMessage(Message m) {
        byte[] bytes = (byte[]) m.obj;
        String text;

        // Find the end of the zero-terminated text string.
        int len = 0;
        while (len < bytes.length) {
            if (bytes[len] == 0)
                break;
            len++;
        }

        try {
            text = new String(bytes, 0, len, "UTF-8");
        }
        catch (UnsupportedEncodingException uee) {
            String msg = String.format(Locale.US, "Unsupported encoding: %s", uee.getMessage());
            Support.userMessage(msg);
            return;
        }

        // The current Activity should be an instance of ChatActivity.
        ChatActivity ca = (ChatActivity) ActivityTracker.getActivity();
        if (ca == null) {
            String msg = String.format(Locale.US,
                    "Internal error -- could not display incoming chat message: %s", text);
            Support.userMessage(msg);
            return;
        }
        TextView tv = ca.getTextViewReceive();
        tv.setText(text);
    }

    /*
     * Run read/display loop. When the connection is closed, exit the loop and tell the
     * running ChatActivity to exit, returning control to the original MainActivity.
     */
    @Override
    public void run() {
        if (!running) {
            Support.userMessage("Server not initialized... exiting.");
            return;
        }

        while (true) {
            byte[] bytes = new byte[BUFSIZE];
            try {
                Support.trace("Waiting to read input...");
                //noinspection ResultOfMethodCallIgnored
                btIn.read(bytes, 0, BUFSIZE);
            }
            catch (IOException ioe) {
                Support.userMessage("Connection closed.");
                break;
            }
            Message m = uiHandler.obtainMessage(CHATTEXT, bytes);
            uiHandler.sendMessage(m);
        }
        closeConnection();
        Message m = caHandler.obtainMessage(ChatActivity.FINISH);
        caHandler.sendMessage(m);
    }

    /**
     * Send the chat message.
     * <p>
     *     This is called directly by ChatActivity.
     *     The message is limited to BUFSIZE bytes.
     *
     * @param bytes the buffer of bytes to write out.
     */
    static public void writeChat(byte[] bytes) {
        try {
            btOut.write(bytes, 0, bytes.length);
        }
        catch (IOException ioe) {
            Support.userMessage(String.format(Locale.US,
                    "Could not write message: %s", ioe.getMessage()));
        }
    }
    /**
     * Close the current open connection if there is one.
     */
    static public void closeConnection() {
        try {
            if (btSocket != null) {
                Support.trace("Closing the connection...");
                btSocket.close();
            }
        } catch (IOException ioe) {
            Support.exception("Failed to close the connection", ioe);
        }
        btSocket = null;
    }
}
