package com.barryholroyd.bluetoothchatdemo.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.TextView;

import com.barryholroyd.bluetoothchatdemo.ApplicationGlobalState;
import com.barryholroyd.bluetoothchatdemo.ChatActivity;
import com.barryholroyd.bluetoothchatdemo.support.Support;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Locale;

/**
 * Created by Barry on 12/14/2016.
 */

public class BluetoothComm extends Thread
{
    private static BluetoothSocket socket;
    private static boolean connected = false;

    /** Message command: TOAST */
    private static final int CHATTEXT = 1;

    /** Buffer size for both input and output. */
    public static final int BUFSIZE = 1024;

    /** Handler for the UI thread. */
    private static Handler mHandler = null;

    /** Bluetooth input stream. */
    private static InputStream btIn;

    /** Bluetooth output stream. */
    private static OutputStream btOut;

    public BluetoothComm(BluetoothSocket _socket) {
        socket = _socket;

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

    private void processMessage(Message m) {
        byte[] bytes = (byte[]) m.obj;
        String text;

        TextView tv = ChatActivity.getTextViewReceive();
        try {
            text = new String(bytes, "UTF-8");
        }
        catch (UnsupportedEncodingException uee) {
            String msg = String.format(Locale.US, "Unsupported encoding: %s", uee.getMessage());
            Support.userMessage(msg);
            return;
        }
        Support.log(String.format(Locale.US, "SETTING TEXT: [%s]", text));
        tv.setText(text);
    }

    @Override
    public void run() {
        Support.log("Running read loop for input: ");
        byte[] bytes = new byte[BUFSIZE];
        int len = 0;

        while (true) {
            try {
                len = btIn.read(bytes, 0, BUFSIZE);
                if (len == -1) {
                    Support.userMessage("Connection closed.");
                    break;
                }
            }
            catch (IOException ioe) {
                Support.userMessage(String.format(Locale.US,
                        "Could not read message: %s", ioe.getMessage()));
                break;
            }
            Message m = mHandler.obtainMessage(CHATTEXT, bytes);
            mHandler.sendMessage(m);
        }
        closeConnection(null);
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
    static public void closeConnection(Activity a) {
        try {
            if (socket != null) {
                Support.log("Closing the connection...");
                socket.close();
                ApplicationGlobalState ags =
                        (ApplicationGlobalState)  ChatActivity.getActivity().getApplication();
                ags.setBtSocket(null);

                if (a != null)
                    a.finish();
                else
                    Support.log("TBD: NOT FINISHING ACTIVITY.");

            }
        } catch (IOException ioe) {
            Support.log(String.format(Locale.US,
                    "*** Failed to close the connection: %s", ioe.getMessage()));
        }
    }
}
