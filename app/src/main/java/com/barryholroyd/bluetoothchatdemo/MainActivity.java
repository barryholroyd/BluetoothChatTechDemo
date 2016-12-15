package com.barryholroyd.bluetoothchatdemo;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.barryholroyd.bluetoothchatdemo.bluetooth.BluetoothMgr;
import com.barryholroyd.bluetoothchatdemo.recyclerview.RecyclerViewManager;

import java.util.Locale;

/*
 * TBD: cancel discovery when not needed.
 * Performing device discovery is a heavy procedure for the Bluetooth adapter and will
 * consume a lot of its resources. Once you have found a device to connect, be certain
 * that you always stop discovery with cancelDiscovery() before attempting a connection.
 * Also, if you already hold a connection with a device, then performing discovery can
 * significantly reduce the bandwidth available for the connection, so you should not
 * perform discovery while connected
 */

public class MainActivity extends AppCompatActivity
{
    private static RecyclerViewManager rvmDiscovered;
    private static RecyclerViewManager rvmPaired;
    private static BluetoothAdapter mBluetoothAdapter;
    public static final int RT_BT_ENABLED = 1;

    // Getters
    public static RecyclerViewManager getRvmDiscovered()    { return rvmDiscovered; }
    public static RecyclerViewManager getRvmPaired()        { return rvmPaired; }
    public static BluetoothAdapter    getBluetoothAdapter() { return mBluetoothAdapter; }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

