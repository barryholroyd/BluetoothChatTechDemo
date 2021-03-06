package com.barryholroyd.bluetoothchattechdemo.activity_chooser;

import android.bluetooth.BluetoothDevice;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.barryholroyd.bluetoothchattechdemo.R;
import com.barryholroyd.bluetoothchattechdemo.bluetooth.BluetoothUtils;
import com.barryholroyd.bluetoothchattechdemo.bluetooth.BluetoothDevices;
import com.barryholroyd.bluetoothchattechdemo.support.Support;

import java.util.ArrayList;
import java.util.Locale;

/**
 * RecyclerView adapter used for displaying both the list of discovered devices
 * and the list of paired devices.
 */
public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder> {
    private final BluetoothDevices bluetoothDevices = new BluetoothDevices();

    /**
     * Standard ViewHolder class.
     * <p>
     *     Contains references to fields to be set for a given row instance
     *     in the RecyclerView and also initializes a listener for clicks
     *     on that row.
     */
    class MyViewHolder extends RecyclerView.ViewHolder {
        final TextView mTvText;
        final TextView mTvMac;
        MyViewHolder(LinearLayout ll) {
            super(ll);
            mTvMac  = (TextView) ll.findViewById(R.id.row_mac);
            mTvText = (TextView) ll.findViewById(R.id.row_text);
            mTvMac.setOnClickListener(new OnClickListenerConnectDevice());
            mTvText.setOnClickListener(new OnClickListenerConnectDevice());
        }
    }

    // For adding/deleting devices from the list.
    BluetoothDevices getDevices() { return bluetoothDevices; }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LinearLayout ll = (LinearLayout)
                LayoutInflater.from(parent.getContext()).inflate(R.layout.rvrow, parent, false);
        return new MyViewHolder(ll);
    }

    @Override
    public void onBindViewHolder(MyViewHolder mvh, int position) {
        BluetoothDevice bd = bluetoothDevices.get(position);
        String name = bd.getName();
        if (name == null) {
            name = "<unknown>";
        }
        String text = String.format("%s: %s", name, bd.getAddress());
        mvh.mTvText.setText(text);
        mvh.mTvMac.setText(bd.getAddress());
    }

    @Override
    public int getItemCount() {
        return bluetoothDevices.size();
    }

    /**
     * Callback to handle clicks on a row in either the "discovered" or "paired"
     * list.
     * <p>
     *     Rows in both lists have the MAC address of a remote device; that is
     *     used by the onClick() method to create a Bluetooth connection. This effectively
     *     makes this end the "client" (initiator) of the Bluetooth chat session.
     */
    private class OnClickListenerConnectDevice implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            /* Check for Bluetooth... it may have been turned off. */
            if (!BluetoothUtils.isEnabled()) {
                Support.userMessageShort("Bluetooth must be turned on.");
                return;
            }

            TextView tvText = (TextView) v;
            FrameLayout fl = (FrameLayout) tvText.getParent();
            TextView tvMac = (TextView) fl.findViewById(R.id.row_mac);
            String mac = (String) tvMac.getText();
            BluetoothDevice btdevice = bluetoothDevices.getDevice(mac);
            if (btdevice == null) {
                String msg = String.format(Locale.US, "Device missing: %s", mac);
                throw new IllegalStateException(msg);
            }

            /*
             * Set up a Bluetooth client connection to the remote device.
             * We can assume that one isn't already running (or we would be interacting
             * with the Chat screen rather than the Chooser screen). The ChooserClient
             * thread will exit after it has made the connection and passed it off to
             * ChatActivity.
             */
            // Bug: rapidly clicking on a device can cause multiple ChooserClients to get started. Behavior is unclear.
            (new ChooserClient(btdevice)).start();
        }
    }
}



