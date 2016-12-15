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
    public static final int REQUEST_ENABLE_BT = 1;
    private static BluetoothAdapter mBluetoothAdapter;
    static BroadcastReceiver mReceiver;

    public static BluetoothAdapter configureAdapter(MainActivity ma) {
        // Get the Bluetooth adapter.
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Support.fatalError(ma, "Device does not support Bluetooth.");
        }
        // Ensure it is enabled; if not, ask the user for permission. We will exit, if refused.
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            ma.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        else {
            configureBluetooth(ma);
            startServer(ma);
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
        Support.log(String.format(Locale.US, "refreshDiscovered:startDiscovery() => %b", ret));
    }

    /**
     * Find and display devices which are already paired with this one.
     *
     * @param v the View the user clicked on.
     */
    public static void refreshPaired(View v) {
        Support.in("refreshPaired");
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
    private static void registerDeviceFoundBroadcastReceiver(final MainActivity ma) {
        Support.in("registerDeviceFoundBroadcastReceiver");
        // Create the receiver.
        mReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                Support.in("onReceive");
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
        Intent intent = ma.registerReceiver(mReceiver, filter); // DEL: delete when not needed.
        Support.out("registerDeviceFoundBroadcastReceiver");
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
