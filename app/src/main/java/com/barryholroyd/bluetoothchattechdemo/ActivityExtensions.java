package com.barryholroyd.bluetoothchattechdemo;

/**
 * Methods that each Activity needs to support.
 */
public interface ActivityExtensions {
    /**
     * Handles Bluetooth on/off (e.g., when the
     * user turns Bluetooth on/off via Android Settings).
     *
     */
    void onBluetoothToggle();
}