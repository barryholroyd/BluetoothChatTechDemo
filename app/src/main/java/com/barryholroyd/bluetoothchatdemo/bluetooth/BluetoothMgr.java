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
import com.barryholroyd.bluetoothchatdemo.Support;

import java.util.Locale;
import java.util.Set;

/**
 * Created by Barry on 12/14/2016.
 */

public class BluetoothMgr {
    public static final int RT_BTENABLED = 1;
    public static final int RT_BT_STATE_CHANGED = 2;
    public static final int RT_BT_SCANMODE_CHANGED = 3;
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
        Support.in("configureBluetooth");

        // Register receiver for handling newly discovered devices during a scan.
        registerDeviceFoundBroadcastReceiver(ma);
        refreshPaired(null);
        refreshDiscovered(null);
        requestDiscoverable(ma);

        Support.out("configureBluetooth");
    }

    /**
     * Ask the user for permission to make this device discoverable.
     */
    private static void requestDiscoverable(Activity a) {
        Intent discoverableIntent = new
                Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        // TBD: set max duration to 300 seconds when done.
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
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
        Support.log(String.format(Locale.US, "clickRefreshDiscovered:startDiscovery() => %b", ret));
    }

    /**
     * Find and display devices which are already paired with this one.
     *
     * @param v the View the user clicked on.
     */
    public static void refreshPaired(View v) {
        Support.in("clickRefreshPaired");
        Support.log(String.format(Locale.US, "BT Adapter state: %d", mBluetoothAdapter.getState()));
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        Support.log(String.format(Locale.US, "Paired devices: %d", pairedDevices.size()));
        if (pairedDevices.size() > 0) {
            MyAdapter myAdapter = MainActivity.getRvmPaired().getAdapter();
            BluetoothDevices btds = myAdapter.getDevices();
            btds.clear();
            for (BluetoothDevice device : pairedDevices) {
                Support.log(String.format(Locale.US, "Found paired device: %s -> %s",
                        device.getName(), device.getAddress()));
                btds.add(device);
            }
            Support.in("clickRefreshPaired:notifyDataSetChanged");
            myAdapter.notifyDataSetChanged();
            Support.out("clickRefreshPaired:notifyDataSetChanged");
        }
        Support.out("clickRefreshPaired");
    }

    /**
     * Register the broadcast receiver which will record each device found
     * during a Bluetooth scan.
     */
    private static void registerDeviceFoundBroadcastReceiver(final MainActivity ma) {
        Support.in("registerDeviceFoundBroadcastReceiver");
        // Create the receiver.
        mReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                Support.in(String.format(Locale.US, "onReceive: %s", action));
                // TBD: doesn't this *have* to be true?
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    MyAdapter myAdapter = ma.getRvmDiscovered().getAdapter();
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
        Intent intent = ma.registerReceiver(mReceiver, filter); // DEL: delete "intent" when not needed.
        Support.out("registerDeviceFoundBroadcastReceiver");

        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        mReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                Support.log(String.format(Locale.US, "Broadcast Receiver received: %s", action));
            }
        };
        ma.registerReceiver(mReceiver, filter);
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        ma.registerReceiver(mReceiver, filter);
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
     * Destroy the broadcast received; called by main Activity's onDestroy() method.
     *
     * @param a the current Activity.
     */
    static public void unregisterMyReceiver(Activity a) {
        a.unregisterReceiver(mReceiver);
    }
}
