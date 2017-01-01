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
 * TBD: comments.
 */

public class ChatActivity extends ActivityTracker
{
    private static EditText etTextSend;
    private static TextView tvTextReceive;

    /** Handler message: call ChatActivity's finish() to exit. */
    public static final int FINISH = 1;

    public static EditText            getEditTextSend()     { return etTextSend; }
    public static TextView            getTextViewReceive()  { return tvTextReceive; }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        BluetoothDevice btdevice = intent.getParcelableExtra(Support.BUNDLE_KEY_BTDEVICE);

        setContentView(R.layout.activity_choose);
        setTitle(btdevice);

        etTextSend = (EditText) findViewById(R.id.text_send);
        tvTextReceive = (TextView) findViewById(R.id.text_receive);

        BluetoothSocket btsocket = ((ApplicationGlobalState) getApplication()).getBtSocket();

        // Call back to exit this activity.
        Handler handler = new Handler() {
            @Override
            public void handleMessage(Message message) {
                if (message.what == FINISH) {
                    Support.log("Exiting ChatActivity...");
                    synchronized (serverLock) {
                        serverLock.setCondition(true);
                        serverLock.notifyAll();
                    }
                    finish();
                }
                else {
                    String msg = String.format(Locale.US,
                            "TBD: ***** Unexpected message type in ChatActivity: %d.",
                            message.what);
                    Support.log(msg);
                }
            }
        };

        (new BluetoothComm(btsocket, handler)).start();
    }

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

    public static void clickSend(View v) {
        String text = etTextSend.getText().toString();
        etTextSend.setText("");
        Support.log(String.format(Locale.US, "GOT TEXT FROM FIELD: %s", text));
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
     * Cancel the connection if it exists.
     *
     * @param v the View the user clicked on.
     */
    public void clickDone(View v) {
        BluetoothComm.closeConnection();
        finish();
    }
}
