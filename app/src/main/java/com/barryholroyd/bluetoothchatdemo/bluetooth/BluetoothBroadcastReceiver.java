package com.barryholroyd.bluetoothchatdemo.bluetooth;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.barryholroyd.bluetoothchatdemo.MainActivity;
import com.barryholroyd.bluetoothchatdemo.recyclerview.RecyclerViewManager;
import com.barryholroyd.bluetoothchatdemo.support.Support;
import com.barryholroyd.bluetoothchatdemo.recyclerview.MyAdapter;

import java.util.HashMap;
import java.util.Locale;

import static android.bluetooth.BluetoothAdapter.EXTRA_PREVIOUS_SCAN_MODE;
import static android.bluetooth.BluetoothAdapter.EXTRA_PREVIOUS_STATE;
import static android.bluetooth.BluetoothAdapter.EXTRA_SCAN_MODE;
import static android.bluetooth.BluetoothAdapter.EXTRA_STATE;

/**
 * Broadcast Receiver for Bluetooth broadcasts.
 * <p>
 *     This needs to be registered and unregistered at the beginning and end of each
 *     MainActivity life cycle because it uses a RecyclerView adapter and that comes
 *     and goes with the Activity (retaining the RecyclerView would cause the Activity
 *     to also be retained, causing a memory leak).
 */
public class BluetoothBroadcastReceiver extends BroadcastReceiver
{
    /**
     * Callback called by the system when a broadcast is received.
     *
     * @param context standard Context parameter
     * @param intent  standard Intent parameter
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        /** RecyclerView adapter instance for discovered devices. */
        RecyclerViewManager rvmd = MainActivity.getRvmDiscovered();
        if (rvmd == null) // if MainActivity isn't currently running, then ignore this call
            return;
        MyAdapter myAdapterDiscovered = rvmd.getAdapter();

        String action = intent.getAction();
        switch (action) {
            case BluetoothDevice.ACTION_FOUND:
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // Do not add to the discovered list if the device has already been paired.
                RecyclerViewManager rvmp = MainActivity.getRvmPaired();
                if (rvmp != null) {
                    if (rvmp.getAdapter().getDevices().getDevice(device.getAddress()) != null) {
                        break;
                    }
                }
                BluetoothDevices btds = myAdapterDiscovered.getDevices();
                btds.addNoDup(device);
                myAdapterDiscovered.notifyDataSetChanged();
                break;
            case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                BtBrLog.brlog("Discovery Started");
                break;
            case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                BtBrLog.brlog("Discovery Finished");
                break;
            case BluetoothAdapter.ACTION_STATE_CHANGED:
                BtBrLog.logActionStateChanged(intent);
                break;
            case BluetoothAdapter.ACTION_SCAN_MODE_CHANGED:
                BtBrLog.logScanModeChanged(intent);
                break;
        }
    }
}

/**
 * Bluetooth Broadcast Receiver logging.
 */
class BtBrLog {
    /** Flag to enable/disable Bluetooth broadcast receiver logging. */
    static final boolean btBrLog = false;

    static final HashMap<Integer,String> BtState = new HashMap<Integer,String>() {
        {
            put(BluetoothAdapter.STATE_OFF, "STATE_OFF");
            put(BluetoothAdapter.STATE_ON, "STATE_ON");
            put(BluetoothAdapter.STATE_TURNING_OFF, "STATE_TURNING_OFF");
            put(BluetoothAdapter.STATE_TURNING_ON, "STATE_TURNING_ON");
        }
    };

    static final HashMap<Integer,String> BtScanMode = new HashMap<Integer,String>() {
        {
            put(BluetoothAdapter.SCAN_MODE_NONE, "SCAN_MODE_NONE");
            put(BluetoothAdapter.SCAN_MODE_CONNECTABLE, "SCAN_MODE_CONNECTABLE");
            put(BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE,
                    "SCAN_MODE_CONNECTABLE_DISCOVERABLE");
        }
    };

    /** Log changes to the Bluetooth state. */
    static void logActionStateChanged(Intent intent) {
        Bundle extras = intent.getExtras();
        brlog(String.format(Locale.US,
                "State Changed: Old=%s New=%s",
                BtState.get(extras.getInt(EXTRA_PREVIOUS_STATE)),
                BtState.get(extras.getInt(EXTRA_STATE))));
    }

    /** Log changes when the Bluetooth scanning mode changes. */
    static void logScanModeChanged(Intent intent) {
        Bundle extras = intent.getExtras();
        brlog(String.format(Locale.US,
                "Scan Mode Changed: Old=%s New=%s",
                BtScanMode.get(extras.getInt(EXTRA_PREVIOUS_SCAN_MODE)),
                BtScanMode.get(extras.getInt(EXTRA_SCAN_MODE))));
    }

    /** Wrapper for Bluetooth Broad Receiver log messages. */
    static void brlog(String s) {
        if (btBrLog) {
            Support.trace(String.format(Locale.US, "> Broadcast received: [%s]", s));
        }
    }
}
