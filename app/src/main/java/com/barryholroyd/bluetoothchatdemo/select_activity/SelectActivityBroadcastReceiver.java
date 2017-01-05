package com.barryholroyd.bluetoothchatdemo.select_activity;


import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.barryholroyd.bluetoothchatdemo.bluetooth.BluetoothDevices;
import com.barryholroyd.bluetoothchatdemo.recyclerview.RecyclerViewManager;
import com.barryholroyd.bluetoothchatdemo.support.ActivityTracker;
import com.barryholroyd.bluetoothchatdemo.support.BroadcastReceivers;
import com.barryholroyd.bluetoothchatdemo.recyclerview.MyAdapter;

import static android.bluetooth.BluetoothAdapter.EXTRA_STATE;

/**
 * Broadcast Receiver for Bluetooth broadcasts.
 * <p>
 *     This class is responsible for starting/stopping the server worker thread (if/when
 *     Bluetooth is turned on/off), handling results from Bluetooth discovery and
 *     logging of various Bluetooth events.
 * <p>
 *     This needs to be registered and unregistered at the beginning and end of each
 *     SelectActivity life cycle because it uses a RecyclerView adapter and that comes
 *     and goes with the Activity (retaining the RecyclerView would cause the Activity
 *     to also be retained, causing a memory leak).
 */
public class SelectActivityBroadcastReceiver extends BroadcastReceiver
{
    /**
     * Callback called by the system when a broadcast is received.
     *
     * @param context standard Context parameter
     * @param intent  standard Intent parameter
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        BroadcastReceivers.Log.logAction(context, intent);

        // if SelectActivity isn't currently running, then ignore this call
        Activity a = ActivityTracker.getActivity();
        if ((a == null) || (!(a instanceof SelectActivity)))
            return;

        /** RecyclerView adapter instance for discovered devices. */
        RecyclerViewManager rvmd = SelectActivity.getRvmDiscovered();
        MyAdapter myAdapterDiscovered = rvmd.getAdapter();

        String action = intent.getAction();
        switch (action) {
            case BluetoothDevice.ACTION_FOUND:
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // Do not add to the discovered list if the device has already been paired.
                RecyclerViewManager rvmp = SelectActivity.getRvmPaired();
                if (rvmp != null) {
                    if (rvmp.getAdapter().getDevices().getDevice(device.getAddress()) != null) {
                        break;
                    }
                }
                BluetoothDevices btds = myAdapterDiscovered.getDevices();
                btds.addNoDup(device);
                myAdapterDiscovered.notifyDataSetChanged();
                break;
            case BluetoothAdapter.ACTION_STATE_CHANGED:
                switch (intent.getExtras().getInt(EXTRA_STATE)) {
                    case BluetoothAdapter.STATE_ON:
                        // If the activity is STARTED or RESUMED, start listener.
                        ActivityTracker.ActivityState state = ActivityTracker.getState();
                        if (    (state == ActivityTracker.ActivityState.STARTED) ||
                                (state == ActivityTracker.ActivityState.RESUMED)) {
                            BtConnectionListener.startListener();
                        }
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        break;
                    case BluetoothAdapter.STATE_OFF | BluetoothAdapter.STATE_TURNING_OFF:
                        BtConnectionListener.stopListener();
                        break;
                }
                break;
        }
    }
}

