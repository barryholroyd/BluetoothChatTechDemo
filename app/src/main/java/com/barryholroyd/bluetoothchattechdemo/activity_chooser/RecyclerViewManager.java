package com.barryholroyd.bluetoothchattechdemo.activity_chooser;

import android.app.Activity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

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
    RecyclerViewManager(Activity a, int id, RecyclerViewAdapter _mAdapter) {
        mAdapter = _mAdapter;
        RecyclerView mRecyclerView = (RecyclerView) a.findViewById(id);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(a));
        mRecyclerView.setAdapter(mAdapter);
    }

    /**
     * Getter for the RecyclerView's adapter.
     *
     * @return the RecyclerView's adapter.
     */
    RecyclerViewAdapter getAdapter() { return mAdapter; }
}

