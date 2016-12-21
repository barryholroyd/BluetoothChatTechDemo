package com.barryholroyd.bluetoothchatdemo;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.barryholroyd.bluetoothchatdemo.bluetooth.BluetoothComm;
import com.barryholroyd.bluetoothchatdemo.bluetooth.BluetoothMgr;
import com.barryholroyd.bluetoothchatdemo.recyclerview.RecyclerViewManager;
import com.barryholroyd.bluetoothchatdemo.support.Support;

import java.io.UnsupportedEncodingException;
import java.util.Locale;

/*
 * TBD: cancel discovery when not needed.
 * TBD: Do cancellations where appropriate.
 * TBD: Reformat screen.
 * TBD: Must connect before using buttons.
 * TBD: Do not include devices in Discovered if already paired.
 * TBD: Clear "received" field before re-filling.
 *
 * TBD: when you pair, refresh the pair list (when you connect from discovered list).
 * TBD: tag text fields so they survive device rotations.
 * TBD: test with device rotations.
 * TBD: after killing connections, can't reconnect from the "Cancel"d end. Probably need to set mSoocket to null.
 * TBD: add Toast at server end for incoming connection.
 * TBD: add Scrollview (Vertical)
 *
 * TBD: Finish comments.
 * TBD: clean out TBDs, log()s, etc.
 */

public class MainActivity extends AppCompatActivity
{
    private static RecyclerViewManager rvmDiscovered;
    private static RecyclerViewManager rvmPaired;
    private static BluetoothAdapter mBluetoothAdapter;
    private static EditText etTextSend;
    private static TextView tvTextReceive;
    public static final int RT_BT_ENABLED = 1;

    // Getters
    public static RecyclerViewManager getRvmDiscovered()    { return rvmDiscovered; }
    public static RecyclerViewManager getRvmPaired()        { return rvmPaired; }
    public static BluetoothAdapter    getBluetoothAdapter() { return mBluetoothAdapter; }
    public static EditText            getEditTextSend()     { return etTextSend; }
    public static TextView            getTextViewReceive()  { return tvTextReceive; }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Support.init(this);

        // These are order-sensitive.
        rvmDiscovered     = new RecyclerViewManager(this, R.id.rv_discovered);
        rvmPaired         = new RecyclerViewManager(this, R.id.rv_paired);
        mBluetoothAdapter = BluetoothMgr.getBluetoothAdapter(this);

        // Ensure it is enabled; if not, ask the user for permission.
        if (!mBluetoothAdapter.isEnabled()) {
            Support.startAFR(this, BluetoothAdapter.ACTION_REQUEST_ENABLE, RT_BT_ENABLED);
        }
        else {
            BluetoothMgr.configureBluetooth(this);
            BluetoothMgr.startServer(this);
        }

        etTextSend = (EditText) findViewById(R.id.text_send);
        tvTextReceive = (TextView) findViewById(R.id.text_receive);
    }

    /**
     * Do a device scan.
     *
     * @param v the View which the user clicked on.
     */
    public static void clickRefreshDiscovered(View v) {
        BluetoothMgr.refreshDiscovered(v);
    }

    /**
     * Find and display devices which are already paired with this one.
     *
     * @param v the View the user clicked on.
     */
    public static void clickRefreshPaired(View v) {
        BluetoothMgr.refreshPaired(v);
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
    public static void clickDone(View v) {
        BluetoothComm.closeConnection();
    }

    @Override
    protected void onActivityResult( int requestCode, int resultCode, Intent data) {
        Support.log(String.format(Locale.US, "***** ActivityResult: request=%d result=%d",
                requestCode, resultCode));
        switch (requestCode) {
            case RT_BT_ENABLED:
                Support.log("ActivityResult[RT_BT_ENABLED]");
                if (resultCode == RESULT_OK) {
                    BluetoothMgr.configureBluetooth(this);
                    BluetoothMgr.startServer(this);
                    return;
                }
                else { Support.fatalError(this, "No Bluetooth available."); }
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        BluetoothMgr.unregisterMyReceiver(this);
    }
}

