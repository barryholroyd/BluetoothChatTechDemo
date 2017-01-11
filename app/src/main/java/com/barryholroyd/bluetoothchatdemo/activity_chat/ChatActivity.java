package com.barryholroyd.bluetoothchatdemo.activity_chat;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.barryholroyd.bluetoothchatdemo.ActivityExtensions;
import com.barryholroyd.bluetoothchatdemo.bluetooth.BluetoothUtils;
import com.barryholroyd.bluetoothchatdemo.support.ActivityPrintStates;
import com.barryholroyd.bluetoothchatdemo.support.ApplicationGlobalState;
import com.barryholroyd.bluetoothchatdemo.R;
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
public class ChatActivity extends ActivityPrintStates implements ActivityExtensions
{
    private EditText etTextSend;
    private TextView tvTextReceive;

    /** Handler message: call ChatActivity's finish() to exit. */
    public static final int FINISH = 1;

    /** Bundle keys for incoming Intents. */
    public static final String BUNDLE_KEY_BTDEVICE = "com.barryholroyd.bluetoothchatdemo.BTDEVICE";

    /** Getter for "receive" TextView in ChatActivity. */
    public TextView getTextViewReceive()  { return tvTextReceive; }

    /** Bluetooth socket passed in from ChooserActivity via static hook in app. */
    private static BluetoothSocket btChatSocket = null;

    /** Handler providing callback to exit the current ChatActivity instance. */
    private static ChatActivityHandler handler = null;

    // This Activity.
    private static ChatActivity ca = null;

    /**
     * Get the current Activity instance.
     *
     * @return the current ChatActivity instance if it exists; else null.
     */
    public static ChatActivity getActivity() { return ca; }

    /**
     * Get the app's Context instance.
     *
     * @return the app's Context instance.
     */
    public static Context getAppContext() { return ca.getApplicationContext(); } // DEL: ?

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

        ca = this;

        Intent intent = getIntent();
        BluetoothDevice btdevice = intent.getParcelableExtra(BUNDLE_KEY_BTDEVICE);

        setContentView(R.layout.activity_chat);
        setTitle(btdevice);

        etTextSend = (EditText) findViewById(R.id.text_send);
        tvTextReceive = (TextView) findViewById(R.id.text_receive);

        configureScrollBars();

        btChatSocket = retrieveBtChatSocket();

        /*
         * We create a static ChatActivityHandler class and pass it a WeakReference to the
         * current ChatActivity instance to avoid possible memory leaks.
         */
        WeakReference<ChatActivity> wrca = new WeakReference<>(this);
        handler = new ChatActivityHandler(wrca);
    }

    private BluetoothSocket retrieveBtChatSocket() {
        return ((ApplicationGlobalState) getApplication()).getBtChatSocket();
    }

    @Override
    public void onStart() {
        super.onStart();
        BluetoothBroadcastReceivers.registerBroadcastReceiver(this, new ChatBroadcastReceiver());
        Support.trace("### Calling startChatServer() from ChatActivity.onStart()...");
        ChatServer.startChatServer(btChatSocket, handler);
    }

    @Override
    public void onStop() {
        super.onStop();
        BluetoothBroadcastReceivers.unregisterBroadcastReceiver(this);
        ChatServer.stopChatServer();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ca = null;
    }

    /**
     * Handle Bluetooth on/off from Broadcast Receiver.
     *
     * @param state Bluetooth turned on / off.
     */
    // DEL: ?
    public void onBluetoothToggle(BluetoothToggle state) {
        switch (state) {
            case BT_ON:
                Support.trace("### Calling startChatServer() from onBluetoothToggle()...");
                ChatServer.startChatServer(btChatSocket, handler);
                break;
            case BT_OFF:
                ChatServer.stopChatServer();
                finish();
                break;
        }
    }

    public void onBluetoothToggle() {
        if (BluetoothUtils.isEnabled()) {
            Support.trace("### Calling startChatServer() from onBluetoothToggle()...");
            ChatServer.startChatServer(btChatSocket, handler);
        }
        else {
            ChatServer.stopChatServer();
            finish();
        }
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
            Support.userMessageLong(msg);
            return;
        }

        if (bytes.length > ChatServer.BUFSIZE) {
            Support.userMessageLong(String.format(Locale.US,
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
        final WeakReference<ChatActivity> wrca;

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
