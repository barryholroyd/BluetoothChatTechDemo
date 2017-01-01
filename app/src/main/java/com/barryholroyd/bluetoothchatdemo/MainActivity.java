package com.barryholroyd.bluetoothchatdemo;

import android.app.Activity;
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
     * All code in this app, except for code in ChatActivity and BluetoothComm (which is
     * only called by ChatActivity) executes while MainActivity should be present and
     * available, with one caveat: BluetoothClient and BluetoothServer classes are both
     * extensions of Thread so that they can set up connections in the background. In both
     * the cases it is possible (although not likely) that the MainActivity instance will
     * be gone by the time they need it. For that reason, we route requests for the MainActivity
     * instance through ActivityTracker, which tracks the creation and destruction of
     * Activities in this app.
     *
     * @return the current MainActivity instance.
     */
    public  static MainActivity  getMainActivity() {
        Activity a = getActivity(); // from ActivityTracker
        if (a == null) {
            throw new IllegalStateException(
                    "Attempt to access uninitialized MainActivity instance");
        }
        if (! (a instanceof MainActivity)) {
            throw new IllegalStateException(String.format(Locale.US,
                    "getMainActivity returned Activity of the wrong class: %s",
                    a.getClass().getSimpleName()));
        }
        return (MainActivity) a;
    }

    /** Request codes for onActivityResult(). */
    public static final int RT_BT_ENABLED = 1;

    // Private non-static fields.
    private RecyclerViewManager rvmDiscovered;
    private RecyclerViewManager rvmPaired;
    private BluetoothAdapter mBluetoothAdapter;

    /** Get the device's Bluetooth adapter. */
    public static BluetoothAdapter getBluetoothAdapter() {
        return getMainActivity().mBluetoothAdapter;
    }

    /** Get the "Discovered" RecyclerViewManager. */
    public static RecyclerViewManager  getRvmDiscovered(){ return getMainActivity().rvmDiscovered; }

    /** Get the "Paired" RecyclerViewManager. */
    public static RecyclerViewManager  getRvmPaired()    { return getMainActivity().rvmPaired; }

    /**
     * Display client interface, initialize Bluetooth, start server worker thread.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ags = (ApplicationGlobalState) getApplication();

        // Display the "client" interface.
        setContentView(R.layout.activity_main);

        /*
         * These are order-sensitive.
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
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, RT_BT_ENABLED);
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

    /** Handle result of request to user to enable Bluetooth. */
    @Override
    protected void onActivityResult( int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case RT_BT_ENABLED:
                if (resultCode == RESULT_OK) {
                    BluetoothMgr.configureBluetooth(this);
                    BluetoothMgr.startServer();
                    return;
                }
                else { Support.fatalError("No Bluetooth available."); }
                break;
        }
    }

    /** Unregister the BroadcastReceiver which handles device discovery results. */
    @Override
    public void onDestroy() {
        super.onDestroy();
        BluetoothMgr.unregisterBroadcastReceiver(this);
    }
}
