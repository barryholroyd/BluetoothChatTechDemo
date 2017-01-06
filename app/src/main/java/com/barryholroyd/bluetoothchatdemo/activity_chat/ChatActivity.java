package com.barryholroyd.bluetoothchatdemo.activity_chat;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.barryholroyd.bluetoothchatdemo.support.ApplicationGlobalState;
import com.barryholroyd.bluetoothchatdemo.R;
import com.barryholroyd.bluetoothchatdemo.bluetooth.BluetoothUtils;
import com.barryholroyd.bluetoothchatdemo.support.ActivityTracker;
import com.barryholroyd.bluetoothchatdemo.bluetooth.BluetoothBroadcastReceivers;
import com.barryholroyd.bluetoothchatdemo.support.Support;

import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.util.Locale;

/**
 * Display the Chat view for the user and manage the sending/receiving of text.
 * <p>
 *     Write requests are handled in this class. Incoming text is read by ChatServer --
 *     it uses the main UI's Handler to get the ChatActivity instance and fill in the
 *     "received" text field. This works cleanly because ChatServer is only used by
 *     ChatActivity.
 */
public class ChatActivity extends ActivityTracker
{
    private EditText etTextSend;
    private TextView tvTextReceive;

    /** Handler message: call ChatActivity's finish() to exit. */
    public static final int FINISH = 1;

    /** Bundle keys for incoming Intents. */
    public static final String BUNDLE_KEY_BTDEVICE = "com.barryholroyd.bluetoothchatdemo.BTDEVICE";

    /** Getter for "receive" TextView in ChatActivity. */
    public TextView getTextViewReceive()  { return tvTextReceive; }

    /** Only allow a single running server thread at a time. */
    static ChatServer chatServer = null;

    /** Bluetooth socket passed in from ChooserActivity via static hook in app. */
    static BluetoothSocket btsocket = null;

    /** Handler providing callback to exit the current ChatActivity instance. */
    static ChatActivityHandler handler = null;

    /**
     * Display the chat window for the user, get the BluetoothSocket stored in
     * ApplicationGlobalState by ChooserClient or ChooserListener, configure a
     * callback Handler to exit the Activity is requested by the worker thread and
     * then start the ChatServer worker thread to handle the actual reads and writes
     * from the connection.
     *
     * @param savedInstanceState standard Bundle argument.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        BluetoothDevice btdevice = intent.getParcelableExtra(BUNDLE_KEY_BTDEVICE);

        setContentView(R.layout.activity_chat);
        setTitle(btdevice);

        etTextSend = (EditText) findViewById(R.id.text_send);
        tvTextReceive = (TextView) findViewById(R.id.text_receive);

        configureScrollBars();

        btsocket = ((ApplicationGlobalState) getApplication()).getBtSocket();

        /*
         * We create a static ChatActivityHandler class and pass it a WeakReference to the
         * current ChatActivity instance to avoid possible memory leaks.
         */
        WeakReference<ChatActivity> wrca = new WeakReference<>(this);
        handler = new ChatActivityHandler(wrca);
    }

    @Override
    public void onStart() {
        super.onStart();
        BluetoothBroadcastReceivers.registerBroadcastReceiver(this, new ChatBroadcastReceiver());
        // TBD:
        Support.trace("### Calling startChatServer() from ChatActivity.onStart()...");
        startChatServer();
    }

    @Override
    public void onStop() {
        super.onStop();
        BluetoothBroadcastReceivers.unregisterBroadcastReceiver(this);
        stopChatServer();
    }

    /**
     * Start a new chat server in the background to read input from the remote device
     * for the current chat session.
     * TBD: can be called more than once... how?
     */
    public static void startChatServer() {
        if (BluetoothUtils.isEnabled()) {
            if (chatServer != null) {
                // DEL:
                throw new IllegalStateException("START CHAT SERVER EXCEPTION");
                // TBD: return;
            }
            Support.trace("Starting chat server...");
            try {
                chatServer = new ChatServer(btsocket, handler);
                chatServer.start();
            }
            catch (ChatServerException cse) {
                throw new IllegalStateException("Could not start chat server.");
            }
        }
    }

    public static void stopChatServer() {
        if (chatServer == null) {
            throw new IllegalStateException(
                    "Attempt to stop a non-existent chat server.");
        }
        Support.trace("Stopping chat server...");
        ChatServer.stopServer();
        chatServer = null;
    }

    /**
     * Configure scrollbars for the text receive TextView.
     * For some reason, this doesn't seem to be configurable from XML.
     */
    private void configureScrollBars() {
        TextView receive = (TextView) findViewById(R.id.text_receive);
        receive.setHorizontallyScrolling(true);
        receive.setMovementMethod(new ScrollingMovementMethod());
        receive.setHint("text received"); // with scrolling, doesn't work from XML for some reason
    }

    /** Set the title of the chat window to reflect the name and MAC address of the remote device. */
    private void setTitle(BluetoothDevice btdevice) {
        TextView tvConnectedTo = (TextView) findViewById(R.id.connected_to);
        String name = "?";
        String mac  = "?";

        if (btdevice != null) {
            name = btdevice.getName();
            if (name == null)
                name = "?";
            mac  = btdevice.getAddress();
        }
        String title = String.format(Locale.US, "Connected to: %s [%s]", name, mac);
        tvConnectedTo.setText(title);
    }

    /** Handle write requests from the user. */
    @SuppressWarnings("UnusedParameters")
    public void clickSend(View v) {
        String text = etTextSend.getText().toString();
        etTextSend.setText("");
        byte[] bytes;
        try {
            bytes = text.getBytes("UTF-8");
        }
        catch (UnsupportedEncodingException uee) {
            String msg = String.format(Locale.US, "Unsupported encoding: %s", uee.getMessage());
            Support.userMessage(msg);
            return;
        }

        if (bytes.length > ChatServer.BUFSIZE) {
            Support.userMessage(String.format(Locale.US,
                    "Message is too long (%d). Maximum length is %d.",
                    bytes.length, ChatServer.BUFSIZE));
            return;
        }

        ChatServer.writeChat(bytes);
    }

    /**
     * Cancel the connection if it exists, then exit the ChatActivity. The original ChooserActivity
     * will then be brought into the foreground from the back stack.
     *
     * @param v the View the user clicked on.
     */
    @SuppressWarnings("UnusedParameters")
    public void clickDone(View v) {
        finish();
    }

    /*
     * An instance of this Handler is passed to the ChatServer instance so that
     * it can contact the current ChatActivity instance to tell it to exit (e.g.,
     * when the server's connection goes away because the remote end exited).
     *
     * If we were to define this as an anonymous inner class, it could cause a
     * memory leak since its continued existence could prevent the containing
     * ChatActivity instance from being released. In order to prevent that,
     * we define a new inner static class, ChatActivityHandler, and instantiate that.
     *
     * We put the ChatActivityHandler instance on a static hook because the static method
     * startChatServer() needs to use it. That could result in a very small
     * memory leak since the ChatActivityHandler instance itself can outlive the ChatActivity
     * instance. However, it isn't very large and there will never be more than
     * one of it (since each new ChatActivity instance will overwrite it), so that
     * is acceptable.
     *
     * We pass the ChatActivity instance in to the ChatActivityHandler instance as a WeakReference;
     * that way it will disappear if the ChatActivity instance itself goes away.
     */
    static class ChatActivityHandler extends Handler
    {
        WeakReference<ChatActivity> wrca;

        ChatActivityHandler(WeakReference<ChatActivity> _wrca) {
            wrca = _wrca;
        }

        @Override
        public void handleMessage(Message message) {
            if (message.what == FINISH) {
                Support.trace("Exiting chat activity...");
                ChatActivity ca = wrca.get();
                if (ca != null) {
                    ca.finish();
                }
            }
            else {
                String msg = String.format(Locale.US,
                        "Unexpected message type in ChatActivity: %d.",
                        message.what);
                throw new IllegalStateException(msg);
            }
        }
    }

}
