package com.barryholroyd.bluetoothchatdemo.recyclerview;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.barryholroyd.bluetoothchatdemo.R;
import com.barryholroyd.bluetoothchatdemo.Support;
import com.barryholroyd.bluetoothchatdemo.bluetooth.BluetoothClient;
import com.barryholroyd.bluetoothchatdemo.bluetooth.BluetoothDevices;

import java.util.Locale;

/**
 * Created by Barry on 12/14/2016.
 */
public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
    final Activity a;
    final BluetoothDevices bluetoothDevices = new BluetoothDevices();

    MyAdapter(Activity _a) {
        a = _a;
    }

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
        Support.in("onCreateViewHolder");
        LinearLayout ll = (LinearLayout)
                LayoutInflater.from(parent.getContext()).inflate(R.layout.rvrow, parent, false);
        MyViewHolder vh = new MyViewHolder(ll);
        Support.out("onCreateViewHolder");
        return vh;
    }

    @Override
    public void onBindViewHolder(MyViewHolder mvh, int position) {
        Support.in("onBindViewHolder");
        Support.log(String.format(Locale.US, "  position=%d", position));
        BluetoothDevice bd = bluetoothDevices.get(position);
        String text = String.format("%s: %s", bd.getName(), bd.getAddress());
        mvh.mTvText.setText(text);
        mvh.mTvMac.setText(bd.getAddress());
        Support.out("onBindViewHolder");
    }

    @Override
    public int getItemCount() {
        Support.log(String.format(Locale.US, "##### getItemCount(): %d", bluetoothDevices.size()));
        return bluetoothDevices.size();
    }

    class OnClickListenerConnectDevice implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            TextView tvText = (TextView) v;
            LinearLayout ll = (LinearLayout) tvText.getParent();
            TextView tvMac = (TextView) ll.findViewById(R.id.row_mac);
            String mac = (String) tvMac.getText();
            String text = (String) tvText.getText();
            Support.log(String.format(Locale.US, "CLICKED ON: %s -> %s", text, mac));

            BluetoothDevice bd = bluetoothDevices.getDevice(mac);
            if (bd == null) {
                Support.fatalError(a, String.format(Locale.US, "Device missing: %s", mac));
            }

            (new BluetoothClient(a, bd)).start();
        }
    }
}