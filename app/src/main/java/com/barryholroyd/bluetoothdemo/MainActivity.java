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

        rvmDiscovered = new RecyclerViewManager(this, R.id.rv_discovered);
        rvmPaired = new RecyclerViewManager(this, R.id.rv_paired);

        configureBluetooth();
    }

    private void configureBluetooth() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Support.userFatalError(this, "Device does not support Bluetooth.");
        }
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        registerDeviceFoundBroadcastReceiver();
        refreshPaired(null); // initialize; don't need the View
        requestDiscoverable();
    }

    /**
     * Register the broadcast receiver which will record each device found
     * during a Bluetooth scan.
     */
    private void registerDeviceFoundBroadcastReceiver() {
        mReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    BluetoothDevices btds = rvmDiscovered.getAdapter().getDevices();
                    btds.add(device);
                    Support.log(String.format(Locale.US, "Found new device: %s -> %s",
                            device.getName(), device.getAddress()));
                    rvmPaired.getAdapter().getDevices().add(device);
                }
            }
        };

        // Register the BroadcastReceiver
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);

        // TBD: Don't forget to unregister during onDestroy

    }

    @Override
    public void onDestroy() {
        unregisterReceiver(mReceiver);
    }

    public void refreshDiscovered(View v) {
        mBluetoothAdapter.startDiscovery();
    }

    private void requestDiscoverable() {
        Intent discoverableIntent = new
                Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(discoverableIntent);
    }

    /**
     * Find devices which are already paired with this one.
     *
     * @param v the View the user clicked on.
     */
    public void refreshPaired(View v) {
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            BluetoothDevices btds = rvmPaired.getAdapter().getDevices();
            btds.clear();
            for (BluetoothDevice device : pairedDevices) {
                Support.log(String.format(Locale.US, "Found paired device: %s -> %s",
                        device.getName(), device.getAddress()));
                btds.add(device);
            }
        }
    }


    @Override
    protected void onActivityResult( int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                if (resultCode == RESULT_OK) {
                    return; // TBD: do anything else here?
                }
                else {
                    // TBD: userError("No bluetooth available.");
                }
                break;
        }
    }
}

