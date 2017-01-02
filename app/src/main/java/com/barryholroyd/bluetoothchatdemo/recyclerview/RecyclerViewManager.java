package com.barryholroyd.bluetoothchatdemo.recyclerview;

import android.app.Activity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

/**
 * A generalized manager for RecyclerViews, to handle both the "discovered"
 * and "paired" RecyclerViews.
 */
public class RecyclerViewManager
{
    private RecyclerView mRecyclerView;
    private MyAdapter mAdapter;

    /**
     * Constructor that provides standard RecyclerView initialization.
     *
     * @param a current Activity instance used to find the RecyclerView of interest.
     * @param id    The RecyclerView to find and initialize.
     */
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

    /**
     * Getter for the RecyclerView's adapter.
     *
     * @return the RecyclerView's adapter.
     */
    public MyAdapter getAdapter() { return mAdapter; }
}

