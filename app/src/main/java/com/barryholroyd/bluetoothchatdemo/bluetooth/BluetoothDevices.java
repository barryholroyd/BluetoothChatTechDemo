package com.barryholroyd.bluetoothchatdemo.bluetooth;

import android.bluetooth.BluetoothDevice;

import com.barryholroyd.bluetoothchatdemo.support.Support;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Class created primarily to assist with type safety.
 * <p>
 *     Also facilitates device lookup by MAC address.
 */
public class BluetoothDevices extends ArrayList<BluetoothDevice>
{
    /**
     * Avoid adding duplicates.
     * <p>
     *     Remote Bluetooth devices may "advertise" multiple times, so we have to filter
     *     out duplicates. See
     *     {@link <a href="https://github.com/WebBluetoothCG/web-bluetooth/issues/225">
     *         Discovery Duplicates
     *         </a>}.
     */
    public void addNoDup(BluetoothDevice device) {
        String deviceAddress = device.getAddress();
        for (BluetoothDevice bd : this) {
            if (deviceAddress.equals(bd.getAddress())) {
                return;
            }
        }
        add(device);
    }
    /**
     * Find a BluetoothDevice based on its mac value.
     *
     * @param mac the BluetoothDevice's MAC address.
     * @return the BluetoothDevice of interest.
     */
    public BluetoothDevice getDevice(String mac) {
        for (BluetoothDevice bd : this) {
            if (bd.getAddress().equals(mac)) {
                return bd;
            }
        }
        return null;
    }
}
