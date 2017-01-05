package com.barryholroyd.bluetoothchatdemo.activity_chat;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.TextView;

import com.barryholroyd.bluetoothchatdemo.support.ActivityTracker;
import com.barryholroyd.bluetoothchatdemo.support.Support;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.channels.ClosedByInterruptException;
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

public class ChatServer extends Thread
{
    /** Bluetooth socket to be read and written. */
    private static BluetoothSocket btSocket;

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
    public ChatServer(BluetoothSocket _btSocket, Handler handler) throws ChatServerException {
        btSocket = _btSocket;
        caHandler = handler;
        if (btSocket == null) {
            throw new ChatServerException("Null Bluetooth socket.");
        }
        try {
            btIn = btSocket.getInputStream();
            btOut = btSocket.getOutputStream();
        }
        catch (IOException ioe) {
            String msg = String.format(Locale.US,
                    "Error: could not get input or output stream: %s",
                    ioe.getMessage());
            throw new ChatServerException(msg);
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
                            "Unexpected message type in ChatServer: %d.",
                            message.what);
                    throw new IllegalStateException(msg);
                }
            }
        };
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
     * running ChatActivity to exit, returning control to the original SelectActivity.
     */
    @Override
    public void run() {
        while (true) {
            byte[] bytes = new byte[BUFSIZE];
            Support.trace("Waiting to read input...");
            try {
                //noinspection ResultOfMethodCallIgnored
                btIn.read(bytes, 0, BUFSIZE);
            }
            catch (ClosedByInterruptException ioe) {
                // ChatActivity sends an interrupt when it wants to kill this server thread.
                Support.trace("Connection closed by interrupt.");
                return;
            }
            catch (IOException ioe) {
                Support.trace("Closing the connection...");
                try {
                    btSocket.close();
                } catch (IOException ioe2) {
                    Support.exception("Failed to close the connection", ioe2);
                }
                Message m = caHandler.obtainMessage(ChatActivity.FINISH);
                caHandler.sendMessage(m);
                return;
            }
            Message m = uiHandler.obtainMessage(CHATTEXT, bytes);
            uiHandler.sendMessage(m);
        }
    }

    /**
     * Send the chat message.
     * <p>
     *     This is called directly by ChatActivity.
     *     The message is limited to BUFSIZE bytes.
     *
     * @param bytes the buffer of bytes to write out.
     */
    static void writeChat(byte[] bytes) {
        try {
            btOut.write(bytes, 0, bytes.length);
        }
        catch (IOException ioe) {
            Support.userMessage(String.format(Locale.US,
                    "Could not write message: %s", ioe.getMessage()));
        }
    }
}

class ChatServerException extends Exception {
    ChatServerException(String msg) {
        super(msg);
    }
}