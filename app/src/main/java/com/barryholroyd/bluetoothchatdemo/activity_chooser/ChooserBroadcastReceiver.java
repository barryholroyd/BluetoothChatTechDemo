package com.barryholroyd.bluetoothchatdemo.activity_chooser;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.barryholroyd.bluetoothchatdemo.bluetooth.BluetoothBroadcastReceivers;
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

        String action = intent.getAction();
        switch (action) {
            case BluetoothDevice.ACTION_FOUND:
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // Do not add to the discovered list if the device has already been paired.
                if (ChooserActivity.getRvmPaired().getAdapter()
                        .getDevices().getDevice(device.getAddress()) != null)
                    break;
                MyAdapter myAdapterDiscovered = ChooserActivity.getRvmDiscovered().getAdapter();
                myAdapterDiscovered.getDevices().addNoDup(device);
                myAdapterDiscovered.notifyDataSetChanged();
                break;
            case BluetoothAdapter.ACTION_STATE_CHANGED:
                switch (intent.getExtras().getInt(EXTRA_STATE)) {
                    case BluetoothAdapter.STATE_ON:
                        ChooserListener.startListener();
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        break;
                    case BluetoothAdapter.STATE_OFF | BluetoothAdapter.STATE_TURNING_OFF:
                        ChooserListener.stopListener();
                        break;
                }
                break;
        }
    }
}

