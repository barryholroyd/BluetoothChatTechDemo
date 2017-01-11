package com.barryholroyd.bluetoothchatdemo;

import android.content.Context;

/**
 * Methods that each Activity needs to support.
 */
public interface ActivityExtensions {
    /** Enum for Bluetooth being turned on or off. */
    enum BluetoothToggle { BT_OFF, BT_ON }
    /**
     * Handles Bluetooth on/off (e.g., when the
     * user turns Bluetooth on/off via Android Settings).
     *
     * @param state indicates that Bluetooth was just turned on or off.
     */
    void onBluetoothToggle();
}