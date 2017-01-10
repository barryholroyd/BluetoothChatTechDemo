package com.barryholroyd.bluetoothchatdemo.activity_chat;


import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.barryholroyd.bluetoothchatdemo.bluetooth.BluetoothBroadcastReceivers;
import com.barryholroyd.bluetoothchatdemo.support.ActivityTracker;
import com.barryholroyd.bluetoothchatdemo.support.Support;

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
public class ChatBroadcastReceiver extends BroadcastReceiver
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
            case BluetoothAdapter.ACTION_STATE_CHANGED:
                ActivityTracker at = ChatActivity.getActivityTracker();
                if (at != null) {
                    switch (intent.getExtras().getInt(EXTRA_STATE)) {
                        case BluetoothAdapter.STATE_ON:
                            at.onBluetoothToggle(ActivityTracker.BluetoothToggle.BT_ON);
                            break;
                        case BluetoothAdapter.STATE_TURNING_ON:
                            break;
                        case BluetoothAdapter.STATE_OFF | BluetoothAdapter.STATE_TURNING_OFF:
                            at.onBluetoothToggle(ActivityTracker.BluetoothToggle.BT_OFF);
                            break;
                    }
                }
                break;
        }
    }
}

