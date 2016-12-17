package com.barryholroyd.bluetoothchatdemo.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.View;

import com.barryholroyd.bluetoothchatdemo.MainActivity;
import com.barryholroyd.bluetoothchatdemo.recyclerview.MyAdapter;
import com.barryholroyd.bluetoothchatdemo.Support;

import java.util.Locale;
import java.util.Set;

/**
 * Created by Barry on 12/14/2016.
 */

public class BluetoothMgr {
    private static BluetoothAdapter mBluetoothAdapter;
    static BroadcastReceiver mReceiver;

    public static BluetoothAdapter getBluetoothAdapter(MainActivity ma) {
        // Get the Bluetooth adapter.
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Support.fatalError(ma, "Device does not support Bluetooth.");
        }
        return mBluetoothAdapter;
    }

    public static void configureBluetooth(MainActivity ma) {
        // Register receiver for handling newly discovered devices during a scan.
        registerBroadcastReceiver(ma);
        refreshPaired(null);
        refreshDiscovered(null);
        requestDiscoverable(ma);
    }

    /**
     * Ask the user for permission to make this device discoverable.
     */
    private static void requestDiscoverable(Activity a) {
        Intent discoverableIntent = new
                Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        a.startActivity(discoverableIntent);
    }

    /**
     * Do a device scan.
     * <p>
     *     This will automatically refresh the "Discovered" RecyclerView.
     *
     * @param v the View which the user clicked on.
     */
    public static void refreshDiscovered(View v) {
        boolean ret = mBluetoothAdapter.startDiscovery();
    }

    /**
     * Find and display devices which are already paired with this one.
     *
     * @param v the View the user clicked on.
     */
    public static void refreshPaired(View v) {
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            MyAdapter myAdapter = MainActivity.getRvmPaired().getAdapter();
            BluetoothDevices btds = myAdapter.getDevices();
            btds.clear();
            for (BluetoothDevice device : pairedDevices) {
                Support.log(String.format(Locale.US, "Found paired device: %s -> %s",
                        device.getName(), device.getAddress()));
                btds.add(device);
            }
            myAdapter.notifyDataSetChanged();
        }
    }

    /**
     * Register the broadcast receiver which will record each device found
     * during a Bluetooth scan.
     */
    private static void registerBroadcastReceiver(final MainActivity ma) {
        IntentFilter ifilter = new IntentFilter();
        ifilter.addAction(BluetoothDevice.ACTION_FOUND);
        ifilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        ifilter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        ifilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        ifilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

        // Register the receiver.
        mReceiver = new BluetoothBroadcastReceiver(ma);
        ma.registerReceiver(mReceiver, ifilter);
    }

    /**
     * Fire up a Bluetooth server on this device.
     * <p>
     *     Must ensure that Bluetooth is enabled first.
     * @param a current Activity.
     */
    static public void startServer(Activity a) {
        (new BluetoothServer(a, mBluetoothAdapter)).start();
    }

    /**
     * Destroy the broadcast receiver; called by main Activity's onDestroy() method.
     *
     * @param a the current Activity.
     */
    static public void unregisterMyReceiver(Activity a) {
        a.unregisterReceiver(mReceiver);
    }
}
