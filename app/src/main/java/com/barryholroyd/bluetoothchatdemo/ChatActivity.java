package com.barryholroyd.bluetoothchatdemo;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.barryholroyd.bluetoothchatdemo.bluetooth.BluetoothComm;
import com.barryholroyd.bluetoothchatdemo.support.ActivityTracker;
import com.barryholroyd.bluetoothchatdemo.support.Support;

import java.io.UnsupportedEncodingException;
import java.util.Locale;

import static com.barryholroyd.bluetoothchatdemo.bluetooth.BluetoothServer.serverLock;

/**
 * Display the Chat view for the user and manage the sending/receiving of text.
 * <p>
 *     Write requests are handled in this class. Incoming text is read by BluetoothComm --
 *     it uses the main UI's Handler to get the ChatActivity instance and fill in the
 *     "received" text field. This works cleanly because BluetoothComm is only used by
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

    /**
     * Display the chat window for the user, get the BluetoothSocket stored in
     * ApplicationGlobalState by BluetoothClient or BluetoothServer, configure a
     * callback Handler to exit the Activity is requested by the worker thread and
     * then start the BluetoothComm worker thread to handle the actual reads and writes
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

        BluetoothSocket btsocket = ((ApplicationGlobalState) getApplication()).getBtSocket();

        // Callback to exit this activity.
        Handler handler = new Handler() {
            @Override
            public void handleMessage(Message message) {
                if (message.what == FINISH) {
                    Support.trace("Exiting ChatActivity...");
                    synchronized (serverLock) {
                        serverLock.setCondition(true);
                        serverLock.notifyAll();
                    }
                    finish();
                }
                else {
                    String msg = String.format(Locale.US,
                            "Unexpected message type in ChatActivity: %d.",
                            message.what);
                    throw new IllegalStateException(msg);
                }
            }
        };

        // Create a worker thread to handle the reads and writes from the Bluetooth connection.
        (new BluetoothComm(btsocket, handler)).start();
    }

    /** Set the title of the chat window to reflect the name and MAC address of the remote device. */
    private void setTitle(BluetoothDevice btdevice) {
        TextView tvConnectedTo = (TextView) findViewById(R.id.connected_to);
        String name = "<unknown>";
        String mac  = "<unknown>";

        if (btdevice != null) {
            name = btdevice.getName();
            if (name == null)
                name = "<unknown>";
            mac  = btdevice.getAddress();
        }
        String title = String.format(Locale.US, "Connected to: %s [%s}", name, mac);
        tvConnectedTo.setText(title);
    }

    /** Handle write requests from the user. */
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

        if (bytes.length > BluetoothComm.BUFSIZE) {
            Support.userMessage(String.format(Locale.US,
                    "Message is too long (%d). Maximum length is %d.",
                    bytes.length, BluetoothComm.BUFSIZE));
            return;
        }

        BluetoothComm.writeChat(bytes);
    }

    /**
     * Cancel the connection if it exists, then exit the ChatActivity. The original MainActivity
     * will then be brought into the foreground from the back stack.
     *
     * @param v the View the user clicked on.
     */
    public void clickDone(View v) {
        BluetoothComm.closeConnection();
        finish();
    }
}
