package com.barryholroyd.bluetoothchatdemo.activity_chooser;

import android.app.Activity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.barryholroyd.bluetoothchatdemo.activity_chooser.RecyclerViewAdapter;

/**
 * A generalized manager for RecyclerViews, to handle both the "discovered"
 * and "paired" RecyclerViews.
 */
public class RecyclerViewManager
{
    private final RecyclerViewAdapter mAdapter;

    /**
     * Constructor that provides standard RecyclerView initialization.
     *
     * @param a current Activity instance used to find the RecyclerView of interest.
     * @param id    The RecyclerView to find and initialize.
     */
    public RecyclerViewManager(Activity a, int id) {
        // Get the desired RecyclerView.
        RecyclerView mRecyclerView = (RecyclerView) a.findViewById(id);
        mRecyclerView.setHasFixedSize(true);

        // Use a LinearLayout for the RecyclerView.
        mRecyclerView.setLayoutManager(new LinearLayoutManager(a));

        // Set the adapter. It is the same for both RecyclerViews.
        mAdapter = new RecyclerViewAdapter();
        mRecyclerView.setAdapter(mAdapter);
    }

    /**
     * Getter for the RecyclerView's adapter.
     *
     * @return the RecyclerView's adapter.
     */
    public RecyclerViewAdapter getAdapter() { return mAdapter; }
}

