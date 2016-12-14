package com.barryholroyd.bluetoothdemo;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Locale;

/**
 * Created by Barry on 12/13/2016.
 */

class RecyclerViewManager
{
    private RecyclerView mRecyclerView;
    private MyAdapter mAdapter;

    RecyclerViewManager(Activity a, int id) {
        // Get the desired RecyclerView.
        mRecyclerView = (RecyclerView) a.findViewById(id);
        mRecyclerView.setHasFixedSize(true);

        // Use a LinearLayout for the RecyclerView.
        mRecyclerView.setLayoutManager(new LinearLayoutManager(a));

        // Set the adapter. It is the same for both RecyclerViews.
        mAdapter = new MyAdapter();
        mRecyclerView.setAdapter(mAdapter);
    }
    MyAdapter getAdapter() { return mAdapter; }
}

class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
    BluetoothDevices bluetoothDevices = new BluetoothDevices();

    static class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView mTvText;
        public TextView mTvMac;
        MyViewHolder(LinearLayout ll, TextView tv, TextView mac) {
            super(ll);
            mTvText = tv;
            mTvMac = mac;
        }
    }

    // For adding/deleting devices from the list.
    BluetoothDevices getDevices() { return bluetoothDevices; }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Support.in("onCreateViewHolder");
        LinearLayout ll = (LinearLayout)
                LayoutInflater.from(parent.getContext()).inflate(R.layout.rvrow, parent, false);
        TextView tv = (TextView) ll.findViewById(R.id.row_text);
        tv.setOnClickListener(new OnClickListenerConnectDevice());
        TextView mac = (TextView) ll.findViewById(R.id.row_mac);
        MyViewHolder vh = new MyViewHolder(ll, tv, mac);
        Support.out("onCreateViewHolder");
        return vh;
    }

    @Override
    public void onBindViewHolder(MyViewHolder mvh, int position) {
        Support.in("onBindViewHolder");
        BluetoothDevice bd = bluetoothDevices.get(position);
        String text = String.format("%s: %s", bd.getName(), bd.getAddress());
        mvh.mTvText.setText(text);
        mvh.mTvMac.setText(bd.getAddress());
        Support.out("onBindViewHolder");
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
            String text = (String) tvText.getText();
            Support.log(String.format(Locale.US, "CLICKED ON: %s -> %s", text, mac));
        }
    }
}
