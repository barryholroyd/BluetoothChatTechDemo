package com.barryholroyd.bluetoothchatdemo.bluetooth;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.TextView;

import com.barryholroyd.bluetoothchatdemo.ChatActivity;
import com.barryholroyd.bluetoothchatdemo.support.Support;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Locale;

/**
 * Bluetooth communications.
 * <p>
 *     This class is only used by ChatActivity, so any reference to an Activity here is
 *     always to the ChatActivity. Its constructor accepts and initialized Bluetooth socket,
 *     creates input and output streams from it, then starts running in the background to
 *     read incoming data on the input stream; incoming data is then sent to the UI thread
 *     for display to the user.
 * <p>
 *     A static writeChat() method is provided for the UI thread to use to write data out
 *     to the remote app. *
 */

public class BluetoothComm extends Thread
{
    private static BluetoothSocket socket;
    private static boolean connected = false;

    /** Handler message: display incoming chat text. */
    private static final int CHATTEXT = 1;

    /** Buffer size for both input and output. */
    public static final int BUFSIZE = 1024;

    /** Handler for the UI thread. */
    private static Handler mHandler = null;

    /** Bluetooth input stream. */
    private static InputStream btIn;

    /** Bluetooth output stream. */
    private static OutputStream btOut;

    /** Handler to cause the calling ChatActivity to exit. */
    private Handler caHandler;

    public BluetoothComm(BluetoothSocket _socket, Handler handler) {
        socket = _socket;
        caHandler = handler;

        try {
            btIn = socket.getInputStream();
            btOut = socket.getOutputStream();
        }
        catch (IOException ioe) {
            String msg = String.format(Locale.US,
                    "Could not get input or output stream: %s",
                    ioe.getMessage());
            Support.userMessage(msg);
        }

        connected = true;
        mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message message) {
                if (message.what == CHATTEXT) {
                    processMessage(message);
                }
                else {
                    String msg = String.format(Locale.US,
                            "TBD: ***** Unexpected message type in BluetoothComm: %d.",
                            message.what);
                    Support.log(msg);
                }
            }
        };
    }

    /**
     * Convert the incoming message into a text string and display it.
     * @param m
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

        TextView tv = ChatActivity.getTextViewReceive();
        try {
            text = new String(bytes, 0, len, "UTF-8");
        }
        catch (UnsupportedEncodingException uee) {
            String msg = String.format(Locale.US, "Unsupported encoding: %s", uee.getMessage());
            Support.userMessage(msg);
            return;
        }
        Support.log(String.format(Locale.US, "SETTING TEXT: [%s][len=%d]", text, len));
        tv.setText(text);
    }

    @Override
    public void run() {
        Support.log("Running read loop for input: ");

        while (true) {
            byte[] bytes = new byte[BUFSIZE];
            try {
                Support.log("Waiting to read input...");
                btIn.read(bytes, 0, BUFSIZE);
            }
            catch (IOException ioe) {
                Support.userMessage("Connection closed.");
                break;
            }
            Message m = mHandler.obtainMessage(CHATTEXT, bytes);
            mHandler.sendMessage(m);
        }
        closeConnection();
        Message m = caHandler.obtainMessage(ChatActivity.FINISH);
        caHandler.sendMessage(m);
    }

    /**
     * Send the chat message.
     * <p>
     *     The message is limited to BUFSIZE bytes.
     *
     * @param bytes the buffer of bytes to write out.
     */
    static public void writeChat(byte[] bytes) {
        if (!connected) {
            Support.userMessage("Please connect first by clicking on a device in the\n" +
                                "\"Discovered\" or \"Paired\" panel.");
            return;
        }
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
     * TBD: does it matter if the connection is open or not?
     */
    static public void closeConnection() {
        try {
            if (socket != null) {
                Support.log("Closing the connection...");
                socket.close();
            }
        } catch (IOException ioe) {
            Support.log(String.format(Locale.US,
                    "*** Failed to close the connection: %s", ioe.getMessage()));
        }
    }
}
