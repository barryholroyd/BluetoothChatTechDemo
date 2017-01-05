package com.barryholroyd.bluetoothchatdemo.chat_activity;


import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.barryholroyd.bluetoothchatdemo.select_activity.BtConnectionListener;
import com.barryholroyd.bluetoothchatdemo.support.ActivityTracker;
import com.barryholroyd.bluetoothchatdemo.support.BroadcastReceivers;

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
public class ChatActivityBroadcastReceiver extends BroadcastReceiver
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
        String action = intent.getAction();
        switch (action) {
            case BluetoothAdapter.ACTION_STATE_CHANGED:
                switch (intent.getExtras().getInt(EXTRA_STATE)) {
                    case BluetoothAdapter.STATE_ON:
                        // TBD: we don't receive broadcasts if we aren't in at least STARTED state
                        // If the activity is STARTED or RESUMED, start listener.
//                        ActivityTracker.ActivityState state = ActivityTracker.getState();
//                        if (    (state == ActivityTracker.ActivityState.STARTED) ||
//                                (state == ActivityTracker.ActivityState.RESUMED)) {
                            ChatActivity.startChat();
//                        }
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        break;
                    case BluetoothAdapter.STATE_OFF | BluetoothAdapter.STATE_TURNING_OFF:
                        ChatActivity.stopChat();
                        break;
                }
                break;
        }
    }
}

