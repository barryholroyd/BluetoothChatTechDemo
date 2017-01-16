package com.barryholroyd.bluetoothchattechdemo.activity_chat;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;

import com.barryholroyd.bluetoothchattechdemo.support.Support;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Locale;

import static com.barryholroyd.bluetoothchattechdemo.activity_chat.ChatActivity.getActivity;

/**
 * Bluetooth communications: send and receive text over a Bluetooth connection.
 * <p>
 *     This class is only used by ChatActivity, so any reference to an Activity here is
 *     always to the ChatActivity. Its constructor accepts an initialized Bluetooth btSocket,
 *     creates input and output streams from it, then starts running in the background to
 *     read incoming data on the input stream; incoming data is then sent to the UI thread
 *     for displayShort to the user.
 * <p>
 *     A static writeChat() method is provided for the UI thread to use to write data out
 *     to the remote app.
 */

class ChatServer extends Thread
{
    /** Bluetooth socket to be read and written. */
    private static BluetoothSocket btSocket;

    /** Handler message: displayShort incoming chat text. */
    private static final int CHATTEXT = 1;

    /** Buffer size for both input and output. */
    static final int BUFSIZE = 1024;

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
     * ChatServer is responsible for closing the btSocket when done.
     *
     * @param _btSocket Bluetooth socket to be read and written.
     * @param handler   Handler to cause the calling ChatActivity to exit.
     */
    ChatServer(BluetoothSocket _btSocket, Handler handler) throws ChatServerException {
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

    // TBD: Adjust, per https://developer.android.com/guide/topics/connectivity/bluetooth.html
    //   #ConnectingAsAClient.
    // Use Messages to send text back to the caller for display.
    // Make write() a non-static method.

    /**
     * Convert the incoming message into a text string and displayShort it.
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
            Support.userMessageLong(msg);
            return;
        }

        // The current Activity should be an instance of ChatActivity.
        ChatActivity ca = getActivity();
        if (ca == null) {
            String msg = String.format(Locale.US,
                    "Internal error -- could not displayShort incoming chat message: %s", text);
            Support.userMessageLong(msg);
            return;
        }
        trace(String.format(Locale.US, "received: %s", text));
        tmpPrint(String.format(Locale.US, "received: %s", text));
        TextView tv = ca.getTextViewReceive();
        tv.setText(text);
    }

    /**
     * Run read/displayShort loop.
     * <p>
     *     When the connection is closed, exit the loop and tell the running ChatActivity
     *     to exit, returning control to the original ChooserActivity.
     * <p>
     *     Sending this thread an interrupt won't have any effect. To interrupt the read(),
     *     the btIn input stream needs to be closed. This is due to an apparent bug in
     *     Android.
     *
     * @see <a href="http://stackoverflow.com/questions/6579539/how-to-unblock-inputstream-read-on-android">How to unblock InputStream.read() on Android?</a>
     * @see #stopChatServer()
     */
    @Override
    public void run() {
        while (true) {
            byte[] bytes = new byte[BUFSIZE];
            trace("waiting to read input...");
            tmpPrint("waiting to read");
            try {
                //noinspection ResultOfMethodCallIgnored
                btIn.read(bytes, 0, BUFSIZE);

                // TBD: print "read" here instead of on UI thread

                tmpPrint("just read");
            }
            catch (IOException ioe) {
                /*
                 * This can be caused by either the connection going down (e.g., if the other
                 * end closed it) or by a call to stopServer() (which closes btIn to force
                 * this exception to be generated).
                 */
                trace("closing the connection...");
                try {
                    btSocket.close();
                } catch (IOException ioe2) {
                    Support.exception("Failed to close the connection", ioe2);
                }

                /*
                 * If the exception was not requested by ChatActivity calling stopServer(),
                 * then we need to tell the running ChatActivity instance to exit. Doing this
                 * is o.k. even if the ChatActivity did trigger this exception.
                 */
                Message m = caHandler.obtainMessage(ChatActivity.FINISH);
                caHandler.sendMessage(m);
                return;
            }
            Message m = uiHandler.obtainMessage(CHATTEXT, bytes);
            uiHandler.sendMessage(m);
            Log.d("BLUETOOTH_CHAT_DEMO", "333");
        }
    }

    static void tmpPrint(String msg) {
        long id = Thread.currentThread().getId();
        String s = String.format("### ChatServerTmp: [%d] %s", id, msg);
        Log.d("BLUETOOTH_CHAT_DEMO", s);

    }

    /**
     * Send the chat message.
     * <p>
     *     This is called directly by ChatActivity.
     *     The message is limited to BUFSIZE bytes.
     *
     * @param bytes the buffer of bytes to write out.
     */
    void writeChat(byte[] bytes) {
        try {
            btOut.write(bytes, 0, bytes.length);
        }
        catch (IOException ioe) {
            Support.userMessageLong(String.format(Locale.US,
                    "Could not write message: %s", ioe.getMessage()));
        }
    }

    /**
     * Stop the background chat server.
     * <p>
     *     Sending this thread an interrupt won't have any effect. Instead, the
     *     input stream must be closed. That will cause the read() method to throw
     *     an exception and the thread can then exit.
     *
     * @see #run()
     */
    void stopChatServer() {
        trace("stopping...");
        if (btIn == null) {
            throw new IllegalStateException("Bluetooth input stream already closed.");
        }
        try {
            btIn.close();
        }
        catch (Exception e) {
            Support.exception("Exception attempting to close input stream", e);
        }
    }

    private static void trace(String msg) {
        Support.trace("ChatServer: " + msg);
    }

    /**
     * Exceptions specific to the chat server.
     */
    class ChatServerException extends Exception {
        ChatServerException(String msg) {
            super(msg);
        }
    }
}

