package com.barryholroyd.bluetoothchatdemo.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.View;

import com.barryholroyd.bluetoothchatdemo.MainActivity;
import com.barryholroyd.bluetoothchatdemo.recyclerview.MyAdapter;
import com.barryholroyd.bluetoothchatdemo.support.ActivityTracker;
import com.barryholroyd.bluetoothchatdemo.support.Support;

import java.util.Set;

/**
 * Static methods for managing Bluetooth activities.
 */

public class BluetoothMgr {
    private static BluetoothAdapter mBluetoothAdapter;
    static BroadcastReceiver mReceiver = null;
    static boolean serverRunning = false;

    public static BluetoothAdapter getBluetoothAdapter() {
        // Get the Bluetooth adapter.
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Support.fatalError("Device does not support Bluetooth.");
        }
        return mBluetoothAdapter;
    }

    /**
     * Configure the Bluetooth session (init BroadcastReceiver for discovery, refresh paired
     * and discovered windows, request that the device be discoverable).
     * <p>
     *     Passing in the Activity is appropriate because the BroadcastReceiver must be
     *     registered and unregistered at the beginning and end of each MainActivity lifecycle.
     * @param a the current Activity.
     */
    public static void configureBluetooth(Activity a) {
        // Register receiver for handling newly discovered devices during a scan.
        registerBroadcastReceiver(a);
        refreshPaired(null);
        refreshDiscovered(null);
        requestDiscoverable();
    }

    /**
     * Ask the user for permission to make this device discoverable.
     */
    private static void requestDiscoverable() {
        /** Only make this request once. */
        if (MainActivity.getApplicationGlobalState().isAppInitialized())
            return;

        Intent discoverableIntent = new
                Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        Context c = ActivityTracker.getContext();
        if (c == null) {
            Support.userMessage("Can't make this device discoverable (no Context available).");
            return;
        }
        c.startActivity(discoverableIntent);
    }

    /**
     * Do a device scan.
     * <p>
     *     This will automatically refresh the "Discovered" RecyclerView.
     *     When new devices are discovered, a broadcast is sent.
     *     See {@link BluetoothBroadcastReceiver#onReceive}.
     *
     * @param v the View which the user clicked on.
     */
    public static void refreshDiscovered(View v) {
        MyAdapter myAdapter = MainActivity.getRvmDiscovered().getAdapter();
        BluetoothDevices btds = myAdapter.getDevices();
        btds.clear();
        mBluetoothAdapter.startDiscovery();
    }

    /**
     * Find and display devices which are already paired with this one.
     *
     * @param v the View the user clicked on.
     */
    public static void refreshPaired(View v) {
        Support.log("Refreshing paired list...");
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            MyAdapter myAdapter = MainActivity.getRvmPaired().getAdapter();
            BluetoothDevices btds = myAdapter.getDevices();
            btds.clear();
            for (BluetoothDevice device : pairedDevices) {
                btds.add(device);
            }
            myAdapter.notifyDataSetChanged();
        }
    }

    /**
     * Register the broadcast receiver that records each device found
     * during a Bluetooth scan.
     *
     * @param c current Activity's Context.
     */
    private static void registerBroadcastReceiver(Context c) {
        IntentFilter ifilter = new IntentFilter();
        ifilter.addAction(BluetoothDevice.ACTION_FOUND);
        ifilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        ifilter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        ifilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        ifilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

        // Register the receiver.
        mReceiver = new BluetoothBroadcastReceiver();
        c.registerReceiver(mReceiver, ifilter);
    }

    /**
     * Fire up a Bluetooth server on this device.
     * <p>
     *     Must ensure that Bluetooth is enabled first. Only a single server should be
     *     run during the lifetime of the application.
     */
    static public synchronized void startServer() {
        if (!serverRunning) {
            Support.log("Starting server...");
            (new BluetoothServer(getBluetoothAdapter())).start();
            serverRunning = true;
        }
    }

    /**
     * Destroy the broadcast receiver; called by main Activity's onDestroy() method.
     *
     * @param c the current Activity's Context.
     */
    static public void unregisterBroadcastReceiver(Context c) {
        if (mReceiver != null) {
            c.unregisterReceiver(mReceiver);
            mReceiver = null;
        }
    }
}
