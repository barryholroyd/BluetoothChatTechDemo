package com.barryholroyd.bluetoothdemo;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import java.util.Locale;
import java.util.Set;

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
    private static final int REQUEST_ENABLE_BT = 1;
    private RecyclerViewManager rvmDiscovered;
    private RecyclerViewManager rvmPaired;
    private BroadcastReceiver mReceiver;
    private BluetoothAdapter mBluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        configureRecyclerViews();
        configureBluetooth();
    }

    private void configureRecyclerViews() {
        Support.in("configureRecyclerViews");
        rvmDiscovered = new RecyclerViewManager(this, R.id.rv_discovered);
        rvmPaired = new RecyclerViewManager(this, R.id.rv_paired);
        Support.out("configureRecyclerViews");
    }

    private void configureBluetooth() {
        Support.in("configureBluetooth");

        // Get the Bluetooth adapter.
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Support.userFatalError(this, "Device does not support Bluetooth.");
        }

        // Ensure it is enabled; if not, ask the user for permission. We will exit, if refused.
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        // Register receiver for handling newly discovered devices during a scan.
        registerDeviceFoundBroadcastReceiver();

        // Initialize the "paired devices" RecyclerView.
        refreshPaired(null);

        // Ask the user for permission to be discoverable.
        requestDiscoverable();
        Support.out("configureBluetooth");
    }

    /**
     * Do a device scan.
     * <p>
     *     This will automatically refresh the "Discovered"
     *     RecyclerView.
     *
     * @param v the View which the user clicked on.
     */
    public void refreshDiscovered(View v) {
        mBluetoothAdapter.startDiscovery();
    }

    /**
     * Ask the user for permission to make this device discoverable.
     */
    private void requestDiscoverable() {
        Intent discoverableIntent = new
                Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(discoverableIntent);
    }

    /**
     * Find and display devices which are already paired with this one.
     *
     * @param v the View the user clicked on.
     */
    public void refreshPaired(View v) {
        Support.in("refreshPaired");
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            MyAdapter myAdapter = rvmPaired.getAdapter();
            BluetoothDevices btds = myAdapter.getDevices();
            btds.clear();
            for (BluetoothDevice device : pairedDevices) {
                Support.log(String.format(Locale.US, "Found paired device: %s -> %s",
                        device.getName(), device.getAddress()));
                btds.add(device);
            }
            Support.in("refreshPaired:notifyDataSetChanged");
            myAdapter.notifyDataSetChanged();
            Support.out("refreshPaired:notifyDataSetChanged");
        }
        Support.out("refreshPaired");
    }

    /**
     * Register the broadcast receiver which will record each device found
     * during a Bluetooth scan.
     */
    private void registerDeviceFoundBroadcastReceiver() {
        Support.in("registerDeviceFoundBroadcastReceiver");
// Create the receiver.
        mReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                Support.in("onReceive");
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    MyAdapter myAdapter = rvmDiscovered.getAdapter();
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    BluetoothDevices btds = myAdapter.getDevices();
                    Support.log(String.format(Locale.US, "Found new device: %s -> %s",
                            device.getName(), device.getAddress()));
                    btds.add(device);
                    Support.in("onReceive:notifyDataSetChanged");
                    myAdapter.notifyDataSetChanged();
                    Support.out("onReceive:notifyDataSetChanged");
                }
                Support.out("onReceive");
            }
        };

        // Register the receiver.
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);
        Support.out("registerDeviceFoundBroadcastReceiver");
    }

    @Override
    protected void onActivityResult( int requestCode, int resultCode, Intent data) {
        Support.in("onActivityResult");
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                if (resultCode == RESULT_OK) {
                    return;
                }
                else {
                    Support.userFatalError(this, "No Bluetooth available.");
                }
                break;
        }
        Support.out("onActivityResult");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }
}

