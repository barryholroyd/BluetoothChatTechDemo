package com.barryholroyd.bluetoothchattechdemo.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;

import com.barryholroyd.bluetoothchattechdemo.activity_chooser.ChooserBroadcastReceiver;
import com.barryholroyd.bluetoothchattechdemo.support.Support;

import java.util.Set;

/**
 * Static methods for managing Bluetooth activities.
 */
public class BluetoothUtils
{
    /** Reference to the device's Bluetooth adapter. */
    private static BluetoothAdapter mBluetoothAdapter = null;

    private static boolean requestAlreadyAsked = false;

    /** Initialization -- check for Bluetooth adapter. */
    public static void init(Activity a) {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Support.fatalError(a, "Device does not support Bluetooth.");
        }
    }

    /** Get the Bluetooth adapter. */
    public static BluetoothAdapter getBluetoothAdapter() {
        return mBluetoothAdapter;
    }

    /** Wrapper for adapter's isEnabled(). */
    public static boolean isEnabled() {
        return mBluetoothAdapter.isEnabled();
    }

    /**
     * Do a device scan.
     * Bluetooth must be turned on.
     * <p>
     *     When new devices are discovered, a broadcast is sent.
     *     See {@link ChooserBroadcastReceiver#onReceive}.
     */
    public static void startDiscovery() {
        mBluetoothAdapter.startDiscovery();
    }

    /**
     * Return the list of devices which are already paired with this one.
     * Bluetooth must be turned on.
     */
    public static Set<BluetoothDevice> getPairedDevices() {
        return mBluetoothAdapter.getBondedDevices();
    }

    /**
     * Ask the user for permission to make this device discoverable. Only ask once.
     */
    public static void requestDiscoverable(Context c) {
        if (requestAlreadyAsked || !mBluetoothAdapter.isEnabled())
            return;

        Intent discoverableIntent = new
                Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        if (c == null) {
            Support.userMessageLong("Can't make this device discoverable (no Context available).");
            return;
        }

        requestAlreadyAsked = true;
        c.startActivity(discoverableIntent);
    }
}
