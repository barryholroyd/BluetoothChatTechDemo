package com.barryholroyd.bluetoothchatdemo.recyclerview;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.barryholroyd.bluetoothchatdemo.support.ActivityTracker;

/**
 * Created by Barry on 12/13/2016.
 */

public class RecyclerViewManager
{
    private RecyclerView mRecyclerView;
    private MyAdapter mAdapter;

    public RecyclerViewManager(Activity a, int id) {
        // Get the desired RecyclerView.
        mRecyclerView = (RecyclerView) a.findViewById(id);
        mRecyclerView.setHasFixedSize(true);

        // Use a LinearLayout for the RecyclerView.
        mRecyclerView.setLayoutManager(new LinearLayoutManager(a));

        // Set the adapter. It is the same for both RecyclerViews.
        mAdapter = new MyAdapter();
        mRecyclerView.setAdapter(mAdapter);
    }
    public MyAdapter getAdapter() { return mAdapter; }
}

