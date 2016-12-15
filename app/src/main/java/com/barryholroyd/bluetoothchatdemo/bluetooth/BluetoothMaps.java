package com.barryholroyd.bluetoothchatdemo.bluetooth;

import android.bluetooth.BluetoothAdapter;

import java.util.HashMap;

/**
 * Created by Barry on 12/15/2016.
 */

class BluetoothMaps
{
    static HashMap<Integer,String> BtState = new HashMap<Integer,String>() {
        {
            put(BluetoothAdapter.STATE_OFF, "STATE_OFF");
            put(BluetoothAdapter.STATE_ON, "STATE_ON");
            put(BluetoothAdapter.STATE_TURNING_OFF, "STATE_TURNING_OFF");
            put(BluetoothAdapter.STATE_TURNING_ON, "STATE_TURNING_ON");
        }
    };

    static HashMap<Integer,String> BtScanMode = new HashMap<Integer,String>() {
        {
            put(BluetoothAdapter.SCAN_MODE_NONE, "SCAN_MODE_NONE");
            put(BluetoothAdapter.SCAN_MODE_CONNECTABLE, "SCAN_MODE_CONNECTABLE");
            put(BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE,
                    "SCAN_MODE_CONNECTABLE_DISCOVERABLE");
        }
    };
}
