package com.barryholroyd.bluetoothchattechdemo.activity_chooser;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.barryholroyd.bluetoothchattechdemo.bluetooth.BluetoothBroadcastReceivers;
import com.barryholroyd.bluetoothchattechdemo.support.Support;

/**
 * Broadcast Receiver for Bluetooth broadcasts.
 * <p>
 *     This class is responsible for starting/stopping the server worker thread (if/when
 *     Bluetooth is turned on/off), handling results from Bluetooth discovery and
 *     logging of various Bluetooth events.
 * <p>
 *     This needs to be registered and unregistered at the beginning and end of each
 *     ChooserActivity life cycle because it uses a RecyclerView adapter and that comes
 *     and goes with the Activity (retaining the RecyclerView would cause the Activity
 *     to also be retained, causing a memory leak).
 */
public class ChooserBroadcastReceiver extends BroadcastReceiver
{
    /**
     * Callback called by the system when a broadcast is received.
     *
     * @param context standard Context parameter
     * @param intent  standard Intent parameter
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        BluetoothBroadcastReceivers.Log.logAction(context, intent);

        ChooserActivity ca = ChooserActivity.getActivity();
        if (ca == null)
            return;

        String action = intent.getAction();
        switch (action) {
            case BluetoothDevice.ACTION_FOUND: // device found by startDiscovery()
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // Do not add to the discovered list if the device has already been paired.
                if (ca.getRvmPaired().getAdapter()
                        .getDevices().getDevice(device.getAddress()) == null) {
                    RecyclerViewAdapter recyclerViewAdapterDiscovered = ca.getRvmDiscovered().getAdapter();
                    recyclerViewAdapterDiscovered.getDevices().addNoDup(device);
                    recyclerViewAdapterDiscovered.notifyDataSetChanged();
                }
                break;
            case BluetoothAdapter.ACTION_STATE_CHANGED: // Bluetooth state change
                ca.onBluetoothToggle();
                break;
            case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                Support.userMessageShort("Discovery Finished");
        }
    }
}

