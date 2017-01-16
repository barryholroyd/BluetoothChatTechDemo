package com.barryholroyd.bluetoothchattechdemo.activity_chooser;

import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;

import com.barryholroyd.bluetoothchattechdemo.activity_chat.ChatActivity;
import com.barryholroyd.bluetoothchattechdemo.support.Support;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

/**
 * Support class providing "utility" methods for Chooser.
 */
class ChooserSupport {
    /*
     * Pass control to the ChatActivity.
     * Make the Bluetooth socket available to ChatActivity.
     * ChatActivity is responsible for closing it.
     */
    static void startChatActivity(BluetoothSocket btChatSocket) {
        Support.userMessageLong("Connected!");
        Support.getGlobalState().setBtChatSocket(btChatSocket);
        Context ac = ChooserActivity.getAppContext();
        Intent intent = new Intent(ac, ChatActivity.class);
        intent.putExtra(ChatActivity.BUNDLE_KEY_BTDEVICE, btChatSocket.getRemoteDevice());
        intent.addFlags(FLAG_ACTIVITY_NEW_TASK); // required since an App Context is used
        ac.startActivity(intent);
    }
}
