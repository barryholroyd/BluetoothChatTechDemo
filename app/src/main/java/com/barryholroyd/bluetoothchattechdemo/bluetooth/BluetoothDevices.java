package com.barryholroyd.bluetoothchattechdemo.bluetooth;

import android.bluetooth.BluetoothDevice;

import java.util.ArrayList;

/**
 * Class which provides access to a list of Bluetooth devices.
 * <p>
 *     This class exists primarily to assist with type safety and code readability.
 *     It also facilitates device lookup by MAC address.
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
        if (getDevice(device.getAddress()) != null)
            return;
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
