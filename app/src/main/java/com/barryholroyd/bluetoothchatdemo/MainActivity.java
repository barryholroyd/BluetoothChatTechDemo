package com.barryholroyd.bluetoothchatdemo;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.barryholroyd.bluetoothchatdemo.bluetooth.BluetoothMgr;
import com.barryholroyd.bluetoothchatdemo.recyclerview.RecyclerViewManager;
import com.barryholroyd.bluetoothchatdemo.support.ActivityTracker;
import com.barryholroyd.bluetoothchatdemo.support.Support;

import java.util.Locale;

/*
Test:
 * Fix for memory leaks.
 *
 * TBD: Finish comments.
 * TBD: clean out TBDs, log()s, etc.
 */

/**
 * Display client UI to initiate connection requests and fork off a worker
 * thread to listen for incoming connection requests. Use BluetoothClient
 * and BluetoothServer, respectively, to perform the actual connect, then
 * instantiate ChatActivity to manage the chat session. ChatActivity uses
 * BluetoothComm to send and receive text over the Bluetooth connection.
 */
public class MainActivity extends ActivityTracker
{
    // Global state is stored at the app level.
    private static ApplicationGlobalState ags;
    public  static ApplicationGlobalState getApplicationGlobalState() { return ags; }

    /**
     * Hook for the current MainActivity instance. This approach allows ut to
     * avoid the usual memory- and thread-leak issues that can be caused by storing
     * references on static hooks.
     * <br>
     * Any references to objects which exist in the context of this Activity (e.g.,
     * RecyclerViews) are accessed via getters which call getMainActivity().
     */
    private static MainActivity ma = null;
    /** Method to validate and return the current MainActivity instance. */
    public  static MainActivity  getMainActivity() {
        if (ma == null)
            throw new IllegalStateException(
                    "Attempt to access uninitialized MainActivity instance");
        return ma;
    }

    /** Request codes for onActivityResult(). */
    public static final int RT_BT_ENABLED = 1;

    // Private non-static fields.
    private RecyclerViewManager rvmDiscovered;
    private RecyclerViewManager rvmPaired;
    private BluetoothAdapter mBluetoothAdapter;

    /** Get the device's Bluetooth adapter. */
    public static BluetoothAdapter getBluetoothAdapter() { return getMainActivity().mBluetoothAdapter; }

    /** Get the "Discovered" RecyclerViewManager. */
    public static RecyclerViewManager  getRvmDiscovered(){ return getMainActivity().rvmDiscovered; }

    /** Get the "Paired" RecyclerViewManager. */
    public static RecyclerViewManager  getRvmPaired()    { return getMainActivity().rvmPaired; }

    /** Standard onCreate() method. */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ags = (ApplicationGlobalState) getApplication();
        ma = this;

        // Display the "client" interface.
        setContentView(R.layout.activity_main);
        /*
         * These are order-sensitive. They are re-created on each device re-configuration.
         * Retaining the RecyclerViews would cause their associated Activity instance to
         * be retained, causing a memory leak.
         */
        rvmDiscovered     = new RecyclerViewManager(this, R.id.rv_discovered);
        rvmPaired         = new RecyclerViewManager(this, R.id.rv_paired);
        mBluetoothAdapter = BluetoothMgr.getBluetoothAdapter();

        /*
         * Ensure Bluetooth is enabled; if not, ask the user for permission.
         * Bluetooth configuration and starting the "server" worker thread
         * both occur here if Bluetooth is already enabled; if it isn't,
         * they are started in onActivityResult() if and only if the user
         * allows Bluetooth to be enabled.
         */
        if (!mBluetoothAdapter.isEnabled()) {
            Support.startAFR(this, BluetoothAdapter.ACTION_REQUEST_ENABLE, RT_BT_ENABLED);
        }
        else {
            BluetoothMgr.configureBluetooth(this);
            BluetoothMgr.startServer();
        }

        ags.setAppInitialized();
    }

    /**
     * Do a device scan.
     *
     * @param v the View which the user clicked on.
     */
    public static void clickRefreshDiscovered(View v) {
        BluetoothMgr.refreshDiscovered(v);
    }

    /**
     * Find and display devices which are already paired with this one.
     *
     * @param v the View the user clicked on.
     */
    public static void clickRefreshPaired(View v) {
        BluetoothMgr.refreshPaired(v);
    }

    @Override
    protected void onActivityResult( int requestCode, int resultCode, Intent data) {
        Support.log(String.format(Locale.US, "***** ActivityResult: request=%d result=%d",
                requestCode, resultCode));
        switch (requestCode) {
            case RT_BT_ENABLED:
                Support.log("ActivityResult[RT_BT_ENABLED]");
                if (resultCode == RESULT_OK) {
                    BluetoothMgr.configureBluetooth(this);
                    BluetoothMgr.startServer();
                    return;
                }
                else { Support.fatalError("No Bluetooth available."); }
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        BluetoothMgr.unregisterBroadcastReceiver(this);
    }
}
