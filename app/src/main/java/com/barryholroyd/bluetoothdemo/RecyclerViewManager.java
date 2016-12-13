package com.barryholroyd.bluetoothdemo;

import android.app.Activity;
import android.app.Fragment;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by Barry on 12/13/2016.
 */

class RecyclerViewManager
{
    private RecyclerView mRecyclerView;

    RecyclerViewManager(Activity a, int id) {

        // Get the desired RecyclerView.
        mRecyclerView = (RecyclerView) a.findViewById(id);
        mRecyclerView.setHasFixedSize(true);

        // Use a LinearLayout for the RecyclerView.
        mRecyclerView.setLayoutManager(new LinearLayoutManager(a));

        // Set the adapter. It is the same for both RecyclerViews.
        RecyclerView.Adapter mAdapter = new MyAdapter();
        mRecyclerView.setAdapter(mAdapter);
    }
}

class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
    ArrayList<BluetoothDevice> bluetoothDevices = new ArrayList<>();

    static class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView mTextView;
        MyViewHolder(TextView v) {
            super(v);
            mTextView = v;
        }
    }

    // For adding/deleting devices from the list.
    ArrayList<BluetoothDevice> getDevices() { return bluetoothDevices; }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LinearLayout ll = (LinearLayout)
                LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_row, parent, false);
        TextView tv = (TextView) ll.findViewById(R.id.row_text);
        MyViewHolder vh = new MyViewHolder(tv);
        return vh;
    }

    @Override
    public void onBindViewHolder(MyViewHolder mvh, int position) {
        BluetoothDevice bd = bluetoothDevices.get(position);
        String text = String.format("%s: %s", bd.getName(), bd.getAddress());
        mvh.mTextView.setText(text);
    }

    @Override
    public int getItemCount() {
        return bluetoothDevices.size();
    }
}
