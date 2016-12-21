package com.barryholroyd.bluetoothchatdemo;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.barryholroyd.bluetoothchatdemo.bluetooth.BluetoothClient;
import com.barryholroyd.bluetoothchatdemo.bluetooth.BluetoothComm;
import com.barryholroyd.bluetoothchatdemo.support.Support;

import java.io.UnsupportedEncodingException;
import java.util.Locale;

/**
 * TBD: comments.
 */

public class ChatActivity extends AppCompatActivity
{
    private static EditText etTextSend;
    private static TextView tvTextReceive;
    private static Activity activity = null;

    public static EditText            getEditTextSend()     { return etTextSend; }
    public static TextView            getTextViewReceive()  { return tvTextReceive; }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activity = this;

        // Start communications.
        Support.userMessage("Starting Chat...");

        Intent intent = getIntent();
        BluetoothDevice btdevice = intent.getParcelableExtra(Support.BUNDLE_KEY_BTDEVICE);

        setContentView(R.layout.activity_choose);
        setTitle(btdevice);

        etTextSend = (EditText) findViewById(R.id.text_send);
        tvTextReceive = (TextView) findViewById(R.id.text_receive);

        BluetoothSocket btsocket = ((ApplicationGlobalState) getApplication()).getBtSocket();
        (new BluetoothComm(btsocket)).start();

    }

    /** Getter to return the current activity to a worker thread, to create an Intent. */
    public static Activity getActivity() {
        return activity;
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
        BluetoothComm.closeConnection(this);
    }
}
