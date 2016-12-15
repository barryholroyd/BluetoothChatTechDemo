package com.barryholroyd.bluetoothchatdemo;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.barryholroyd.bluetoothchatdemo.bluetooth.BluetoothMgr;
import com.barryholroyd.bluetoothchatdemo.recyclerview.RecyclerViewManager;

import static com.barryholroyd.bluetoothchatdemo.bluetooth.BluetoothMgr.REQUEST_ENABLE_BT;

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

    public static RecyclerViewManager getRvmDiscovered() { return rvmDiscovered; }
    public static RecyclerViewManager getRvmPaired()     { return rvmPaired; }
    public static BluetoothAdapter getBluetoothAdapter()     { return mBluetoothAdapter; }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // These are order-sensitive.
        configureRecyclerViews();
        mBluetoothAdapter = BluetoothMgr.configureAdapter(this);
    }

    private void configureRecyclerViews() {
        Support.in("configureRecyclerViews");
        rvmDiscovered = new RecyclerViewManager(this, R.id.rv_discovered);
        rvmPaired = new RecyclerViewManager(this, R.id.rv_paired);
        Support.out("configureRecyclerViews");
    }

    /**
     * Do a device scan.
     *
     * @param v the View which the user clicked on.
     */
    public static void refreshDiscovered(View v) {
        BluetoothMgr.refreshDiscovered(v);
    }

    /**
     * Find and display devices which are already paired with this one.
     *
     * @param v the View the user clicked on.
     */
    public static void refreshPaired(View v) {
        BluetoothMgr.refreshPaired(v);
    }

    @Override
    protected void onActivityResult( int requestCode, int resultCode, Intent data) {
        Support.in("onActivityResult");
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                if (resultCode == RESULT_OK) {
                    BluetoothMgr.configureBluetooth(this);
                    BluetoothMgr.startServer(this);
                    return;
                }
                else { Support.fatalError(this, "No Bluetooth available."); }
                break;
        }
        Support.out("onActivityResult");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        BluetoothMgr.unregisterMyReceiver(this);
    }
}

