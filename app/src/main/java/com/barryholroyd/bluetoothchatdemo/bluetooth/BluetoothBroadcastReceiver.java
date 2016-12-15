package com.barryholroyd.bluetoothchatdemo.bluetooth;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.barryholroyd.bluetoothchatdemo.MainActivity;
import com.barryholroyd.bluetoothchatdemo.Support;
import com.barryholroyd.bluetoothchatdemo.recyclerview.MyAdapter;

import java.util.HashMap;
import java.util.Locale;

import static android.bluetooth.BluetoothAdapter.EXTRA_PREVIOUS_SCAN_MODE;
import static android.bluetooth.BluetoothAdapter.EXTRA_PREVIOUS_STATE;
import static android.bluetooth.BluetoothAdapter.EXTRA_SCAN_MODE;
import static android.bluetooth.BluetoothAdapter.EXTRA_STATE;

/**
 * Created by Barry on 12/15/2016.
 */

public class BluetoothBroadcastReceiver extends BroadcastReceiver
{
    MyAdapter myAdapterDiscovered;
    BluetoothBroadcastReceiver(MainActivity ma) {
        myAdapterDiscovered = ma.getRvmDiscovered().getAdapter();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Support.log(String.format(Locale.US, "### Broadcast received: %s", action));
        switch (action) {
            case BluetoothDevice.ACTION_FOUND:
                Support.log("************* IGNORING RECEIVED ACTION FOUND! *************");
//                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
//                BluetoothDevices btds = myAdapterDiscovered.getDevices();
//                Support.log(String.format(Locale.US, "Found new device: %s -> %s",
//                        device.getName(), device.getAddress()));
//                btds.add(device);
//                Support.log("myAdapterDiscovered.notifyDataSetChanged");
//                myAdapterDiscovered.notifyDataSetChanged();
                break;
            case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                break;
            case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
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

class BrLog {
    static void logActionStateChanged(Intent intent) {
        Bundle extras = intent.getExtras();
        Support.log(String.format(Locale.US,
                "ActionStateChanged: Old=%s New=%s",
                BluetoothMaps.BtState.get(extras.getInt(EXTRA_PREVIOUS_STATE)),
                BluetoothMaps.BtState.get(extras.getInt(EXTRA_STATE))));
    }

    static void logScanModeChanged(Intent intent) {
        Bundle extras = intent.getExtras();
        brlog(String.format(Locale.US,
                "ScanModeChanged: Old=%s New=%s",
                BluetoothMaps.BtScanMode.get(extras.getInt(EXTRA_PREVIOUS_SCAN_MODE)),
                BluetoothMaps.BtScanMode.get(extras.getInt(EXTRA_SCAN_MODE))));
    }

    private static void brlog(String s) {
        Support.log(String.format(Locale.US, "  > Broadcast received: [%s]", s));
    }
}