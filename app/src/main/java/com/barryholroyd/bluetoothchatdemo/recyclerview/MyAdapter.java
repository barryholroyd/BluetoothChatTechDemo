package com.barryholroyd.bluetoothchatdemo.recyclerview;

import android.bluetooth.BluetoothDevice;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.barryholroyd.bluetoothchatdemo.R;
import com.barryholroyd.bluetoothchatdemo.bluetooth.BluetoothClient;
import com.barryholroyd.bluetoothchatdemo.support.Support;
import com.barryholroyd.bluetoothchatdemo.bluetooth.BluetoothDevices;

import java.util.Locale;

/**
 * RecyclerView adapter used for displaying both the list of discovered devices
 * and the list of paired devices.
 */
public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
    final BluetoothDevices bluetoothDevices = new BluetoothDevices();

    /**
     * Standard ViewHolder class.
     * <p>
     *     Contains references to fields to be set for a given row instance
     *     in the RecyclerView and also initializes a listener for clicks
     *     on that row.
     */
    class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView mTvText;
        public TextView mTvMac;
        MyViewHolder(LinearLayout ll) {
            super(ll);
            mTvMac  = (TextView) ll.findViewById(R.id.row_mac);
            mTvText = (TextView) ll.findViewById(R.id.row_text);
            mTvText.setOnClickListener(new OnClickListenerConnectDevice());
        }
    }

    // For adding/deleting devices from the list.
    public BluetoothDevices getDevices() { return bluetoothDevices; }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LinearLayout ll = (LinearLayout)
                LayoutInflater.from(parent.getContext()).inflate(R.layout.rvrow, parent, false);
        MyViewHolder vh = new MyViewHolder(ll);
        return vh;
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

    class OnClickListenerConnectDevice implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            TextView tvText = (TextView) v;
            LinearLayout ll = (LinearLayout) tvText.getParent();
            TextView tvMac = (TextView) ll.findViewById(R.id.row_mac);
            String mac = (String) tvMac.getText();
            BluetoothDevice btdevice = bluetoothDevices.getDevice(mac);
            if (btdevice == null) {
                Support.fatalError(String.format(Locale.US, "Device missing: %s", mac));
            }
            // Set up a Bluetooth client connection to the remote device.
            (new BluetoothClient(btdevice)).start();
        }
    }
}



