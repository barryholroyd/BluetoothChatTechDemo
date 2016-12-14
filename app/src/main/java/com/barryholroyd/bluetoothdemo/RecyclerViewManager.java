package com.barryholroyd.bluetoothdemo;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

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
        public TextView mTextView;
        MyViewHolder(TextView v) {
            super(v);
            mTextView = v;
        }
    }

    // For adding/deleting devices from the list.
    BluetoothDevices getDevices() { return bluetoothDevices; }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Support.in("onCreateViewHolder");
        LinearLayout ll = (LinearLayout)
                LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_row, parent, false);
        TextView tv = (TextView) ll.findViewById(R.id.row_text);
        MyViewHolder vh = new MyViewHolder(tv);
        Support.out("onCreateViewHolder");
        return vh;
    }

    @Override
    public void onBindViewHolder(MyViewHolder mvh, int position) {
        Support.in("onBindViewHolder");
        BluetoothDevice bd = bluetoothDevices.get(position);
        String text = String.format("%s: %s", bd.getName(), bd.getAddress());
        mvh.mTextView.setText(text);
        Support.out("onBindViewHolder");
    }

    @Override
    public int getItemCount() {
        return bluetoothDevices.size();
    }
}
