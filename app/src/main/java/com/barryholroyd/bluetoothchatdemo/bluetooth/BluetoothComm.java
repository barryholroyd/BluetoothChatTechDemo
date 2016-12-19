package com.barryholroyd.bluetoothchatdemo.bluetooth;

import android.bluetooth.BluetoothSocket;

import com.barryholroyd.bluetoothchatdemo.support.Support;

/**
 * Created by Barry on 12/14/2016.
 */

class BluetoothComm
{
    static void start(String tag, BluetoothSocket socket) {
        // DEL: delete tag argument.
        Support.log("COMM ATTEMPT: " + tag);


        // TBD: close the socket when done.
    }
}
