package com.barryholroyd.bluetoothchatdemo.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;

import com.barryholroyd.bluetoothchatdemo.activity_chooser.ChooserActivity;
import com.barryholroyd.bluetoothchatdemo.activity_chooser.ChooserBroadcastReceiver;
import com.barryholroyd.bluetoothchatdemo.support.ActivityTracker;
import com.barryholroyd.bluetoothchatdemo.support.Support;

import java.util.Set;

/**
 * Static methods for managing Bluetooth activities.
 */
public class BluetoothUtils {

    /** Get the Bluetooth adapter; includes check for lack of Bluetooth support. */
    public static BluetoothAdapter getBluetoothAdapter() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Support.fatalError("Device does not support Bluetooth.");
        }
        return mBluetoothAdapter;
    }

    /** Check to see if Bluetooth is up and running. */
    public static boolean isEnabled() {
        return getBluetoothAdapter().isEnabled();
    }

    /**
     * Do a device scan.
     * Bluetooth must be turned on.
     * <p>
     *     When new devices are discovered, a broadcast is sent.
     *     See {@link ChooserBroadcastReceiver#onReceive}.
     */
    public static void startDiscovery() {
        getBluetoothAdapter().startDiscovery();
    }

    /**
     * Return the list of devices which are already paired with this one.
     * Bluetooth must be turned on.
     */
    public static Set<BluetoothDevice> getPairedDevices() {
        return getBluetoothAdapter().getBondedDevices();
    }

    /**
     * Ask the user for permission to make this device discoverable.
     * The user will be asked to turn on Bluetooth if it is not already on.
     */
    public static void requestDiscoverable() {
        // Only do this once.
        if (ChooserActivity.getApplicationGlobalState().isAppInitialized())
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

}
