package com.barryholroyd.bluetoothchatdemo.bluetooth;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.TextView;

import com.barryholroyd.bluetoothchatdemo.MainActivity;
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
    private static String tag;
    private static BluetoothSocket socket;

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

    BluetoothComm(String _tag, BluetoothSocket _socket) {
        tag = _tag;
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
        // TBD: close the socket when done.
    }

    private void processMessage(Message m) {
        byte[] bytes = (byte[]) m.obj;
        String text;

        TextView tv = MainActivity.getTextViewReceive();
        try {
            text = new String(bytes, "UTF-8");
        }
        catch (UnsupportedEncodingException uee) {
            String msg = String.format(Locale.US, "Unsupported encoding: %s", uee.getMessage());
            Support.userMessage(msg);
            return;
        }
        tv.setText(text);
    }

    @Override
    public void run() {
        Support.log("COMM ATTEMPT: " + tag);
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
            }
            Support.log(String.format(Locale.US, "BluetoothComm.run(): len=%d, bytes[].length=%d",
                    len, bytes.length));
            Message m = mHandler.obtainMessage(CHATTEXT, bytes);
            // DEL: ? m.arg1 = bytes;
            mHandler.sendMessage(m);
        }
    }

    /**
     * Send the chat message.
     * <p>
     *     The message is limited to BUFSIZE bytes.
     *
     * @param bytes the buffer of bytes to write out.
     */
    static public void writeChat(byte[] bytes) {
        try {
            Support.log(String.format(Locale.US, "=> writeChat(): bytes len = %d", bytes.length));
            btOut.write(bytes, 0, bytes.length);
        }
        catch (IOException ioe) {
            Support.userMessage(String.format(Locale.US,
                    "Could not write message: %s", ioe.getMessage()));
        }
    }
}
