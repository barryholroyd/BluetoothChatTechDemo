package com.barryholroyd.bluetoothchatdemo.support;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import java.util.HashMap;
import java.util.Locale;

import static android.bluetooth.BluetoothAdapter.EXTRA_PREVIOUS_SCAN_MODE;
import static android.bluetooth.BluetoothAdapter.EXTRA_PREVIOUS_STATE;
import static android.bluetooth.BluetoothAdapter.EXTRA_SCAN_MODE;
import static android.bluetooth.BluetoothAdapter.EXTRA_STATE;

/**
 * TBD: comments
 */

public class BroadcastReceivers {
    private static final HashMap<Context, BroadcastReceiver> broadcastReceivers = new HashMap<>();

    /**
     * Register the Bluetooth broadcast receiver.
     *
     * @param c current Activity's Context.
     */
    public static void registerBroadcastReceiver(Context c, BroadcastReceiver br) {
        IntentFilter ifilter = new IntentFilter();
        ifilter.addAction(BluetoothDevice.ACTION_FOUND);
        ifilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        ifilter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        ifilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        ifilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

        // Register the receiver.
        Support.trace(String.format("Registering Broadcast Receiver for: %s",
                c.getClass().getSimpleName()));
        BroadcastReceiver mReceiver = br;
        broadcastReceivers.put(c, mReceiver);
        c.registerReceiver(mReceiver, ifilter);
    }

    /**
     * Destroy the broadcast receiver; called by main Activity's onDestroy() method.
     *
     * @param c the current Activity's Context.
     */
    static public void unregisterBroadcastReceiver(Context c) {
        Support.trace(String.format("Unregistering Broadcast Receiver for: %s",
                c.getClass().getSimpleName()));
        BroadcastReceiver mReceiver = broadcastReceivers.get(c);
        if (mReceiver != null) {
            c.unregisterReceiver(mReceiver);
            broadcastReceivers.remove(c);
        }
    }

    /**
     * Bluetooth Broadcast Receiver logging.
     */
    public static class Log {
        private static final HashMap<Integer,String> BtState = new HashMap<Integer,String>() {
            {
                put(BluetoothAdapter.STATE_OFF, "STATE_OFF");
                put(BluetoothAdapter.STATE_ON, "STATE_ON");
                put(BluetoothAdapter.STATE_TURNING_OFF, "STATE_TURNING_OFF");
                put(BluetoothAdapter.STATE_TURNING_ON, "STATE_TURNING_ON");
            }
        };

        private static final HashMap<Integer,String> BtScanMode = new HashMap<Integer,String>() {
            {
                put(BluetoothAdapter.SCAN_MODE_NONE, "SCAN_MODE_NONE");
                put(BluetoothAdapter.SCAN_MODE_CONNECTABLE, "SCAN_MODE_CONNECTABLE");
                put(BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE,
                        "SCAN_MODE_CONNECTABLE_DISCOVERABLE");
            }
        };

        public static void logAction(Context context, Intent intent) {
            String name = context.getClass().getSimpleName();
            String action = intent.getAction();
            switch (action) {
                case BluetoothDevice.ACTION_FOUND:
                    BroadcastReceivers.Log.brlog(name, "Found device");
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                    BroadcastReceivers.Log.brlog(name, "Discovery Started");
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                    BroadcastReceivers.Log.brlog(name, "Discovery Finished");
                    break;
                case BluetoothAdapter.ACTION_STATE_CHANGED:
                    BroadcastReceivers.Log.logActionStateChanged(name, intent);
                    break;
                case BluetoothAdapter.ACTION_SCAN_MODE_CHANGED:
                    BroadcastReceivers.Log.logScanModeChanged(name, intent);
                    break;
            }

        }

        /** Log changes to the Bluetooth state. */
        private static void logActionStateChanged(String name, Intent intent) {
            Bundle extras = intent.getExtras();
            brlog(name, String.format(Locale.US,
                    "State Changed: Old=%s New=%s",
                    BtState.get(extras.getInt(EXTRA_PREVIOUS_STATE)),
                    BtState.get(extras.getInt(EXTRA_STATE))));
        }

        /** Log changes when the Bluetooth scanning mode changes. */
        private static void logScanModeChanged(String name, Intent intent) {
            Bundle extras = intent.getExtras();
            brlog(name, String.format(Locale.US,
                    "Scan Mode Changed: Old=%s New=%s",
                    BtScanMode.get(extras.getInt(EXTRA_PREVIOUS_SCAN_MODE)),
                    BtScanMode.get(extras.getInt(EXTRA_SCAN_MODE))));
        }

        /** Wrapper for Bluetooth Broad Receiver log messages. */
        private static void brlog(String name, String s) {
            Support.trace(String.format(Locale.US, "> Broadcast received by %s: %s", name, s));
        }
    }
}
