package com.barryholroyd.bluetoothchatdemo.bluetooth;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.barryholroyd.bluetoothchatdemo.MainActivity;
import com.barryholroyd.bluetoothchatdemo.support.Support;
import com.barryholroyd.bluetoothchatdemo.recyclerview.MyAdapter;

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
        MyAdapter myAdapterDiscovered = MainActivity.getRvmDiscovered().getAdapter();
        String action = intent.getAction();
        switch (action) {
            case BluetoothDevice.ACTION_FOUND:
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                BrLog.brlog(String.format(Locale.US, "Device Found: %s, %s",
                        device.getName(), device.getAddress()));

                // Do not add to the discovered list if the device has already been paired.
                String deviceAddress = device.getAddress();
                BluetoothDevices pairedDevices =
                        MainActivity.getRvmPaired().getAdapter().getDevices();
                if (pairedDevices.getDevice(deviceAddress) != null)
                    break;

                BluetoothDevices btds = myAdapterDiscovered.getDevices();
                btds.addNoDup(device);
                myAdapterDiscovered.notifyDataSetChanged();
                break;
            case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                BrLog.brlog("Discovery Started");
                break;
            case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                BrLog.brlog("Discovery Finished");
                break;
            case BluetoothAdapter.ACTION_STATE_CHANGED:
                BrLog.logActionStateChanged(intent);
                break;
            case BluetoothAdapter.ACTION_SCAN_MODE_CHANGED:
                BrLog.logScanModeChanged(intent);
                break;
        }
    }
}

/**
 * Bluetooth Broadcast Receiver logging.
 */
class BrLog {
    /** Log changes to the Bluetooth state. */
    static void logActionStateChanged(Intent intent) {
        Bundle extras = intent.getExtras();
        brlog(String.format(Locale.US,
                "State Changed: Old=%s New=%s",
                BluetoothMaps.BtState.get(extras.getInt(EXTRA_PREVIOUS_STATE)),
                BluetoothMaps.BtState.get(extras.getInt(EXTRA_STATE))));
    }

    /** Log changes when the Bluetooth scanning mode changes. */
    static void logScanModeChanged(Intent intent) {
        Bundle extras = intent.getExtras();
        brlog(String.format(Locale.US,
                "Scan Mode Changed: Old=%s New=%s",
                BluetoothMaps.BtScanMode.get(extras.getInt(EXTRA_PREVIOUS_SCAN_MODE)),
                BluetoothMaps.BtScanMode.get(extras.getInt(EXTRA_SCAN_MODE))));
    }

    /** Wrapper for Bluetooth Broad Receiver log messages. */
    static void brlog(String s) {
        Support.log(String.format(Locale.US, "> Broadcast received: [%s]", s));
    }
}
