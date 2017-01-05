package com.barryholroyd.bluetoothchatdemo.activity_select;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.barryholroyd.bluetoothchatdemo.support.ApplicationGlobalState;
import com.barryholroyd.bluetoothchatdemo.R;
import com.barryholroyd.bluetoothchatdemo.bluetooth.BluetoothDevices;
import com.barryholroyd.bluetoothchatdemo.bluetooth.BluetoothUtils;
import com.barryholroyd.bluetoothchatdemo.recyclerview.MyAdapter;
import com.barryholroyd.bluetoothchatdemo.recyclerview.RecyclerViewManager;
import com.barryholroyd.bluetoothchatdemo.support.ActivityTracker;
import com.barryholroyd.bluetoothchatdemo.support.BroadcastReceivers;
import com.barryholroyd.bluetoothchatdemo.support.Support;

import java.util.Set;

/*
 * Clean up
 * TBD: Run code analyzer (e.g., Handlers should be static?)
 * TBD: clean out TBDs, etc.
 *
 * Bugs
 * TBD: Turn off Bluetooth: Server: waiting for a new connection to accept LOOPS
 *      Client contacts S7, S7 is server. Turn off bluetooth on S7, infinite loop in S7
 *      Server: waiting for a new connection to accept...
 *      SEE SelectConnectionListener.java.
 *      Client: bluetooth is off -- exit
 *      Server: connection lost; restart server (?).
 * TBD: Test -- is alternate connect approach ever used by either device?
 * TBD: Test -- try killing server (pull USB cable?)
 *
 * Look and feel
 * TBD: S7 portrait mode, Connected header, fully shows? (minLines=1)
 * TBD: Rotate server end (S7) -- retains text received?
 *
 * Closure
 * TBD: turn off tracing.
 */

/**
 * Display client UI to initiate connection requests and fork off a worker
 * thread to listen for incoming connection requests. Use BluetoothClient
 * and SelectConnectionListener, respectively, to perform the actual connect, then
 * instantiate ChatActivity to manage the chat session. ChatActivity uses
 * ChatServer to send and receive text over the Bluetooth connection.
 */
public class SelectActivity extends ActivityTracker
{
    // Global state is stored at the app level.
    private static ApplicationGlobalState ags;
    public  static ApplicationGlobalState getApplicationGlobalState() { return ags; }

    /** Request codes for onActivityResult(). */
    private static final int RT_BT_ENABLED = 1;

    // Private non-static fields.
    private RecyclerViewManager rvmDiscovered;
    private RecyclerViewManager rvmPaired;

    /**
     * All code in this app, except for code in ChatActivity and ChatServer (which is
     * only called by ChatActivity) executes while SelectActivity should be present and
     * available, with one caveat: BluetoothClient and SelectConnectionListener classes are both
     * extensions of Thread so that they can set up connections in the background. In both
     * the cases it is possible (although not likely) that the SelectActivity instance will
     * be gone by the time they need it. For that reason, we route requests for the SelectActivity
     * instance through ActivityTracker, which tracks the creation and destruction of
     * Activities in this app.
     *
     * @return the current SelectActivity instance if it exists; else null.
     */
    private static SelectActivity getMainActivity() {
        Activity a = getActivity(); // from ActivityTracker

        // This should only happen if this method is called from SelectActivity's onCreate() method.
        if (a == null) {
            Support.fatalError("Attempt to access uninitialized SelectActivity instance.");
        }
        if (! (a instanceof SelectActivity)) {
            return null;
        }
        return (SelectActivity) a;
    }

    /** Get the "Discovered" RecyclerViewManager. */
    public static RecyclerViewManager  getRvmDiscovered(){
        SelectActivity ma = getMainActivity();
        if (ma != null) return ma.rvmDiscovered;
        else            return null;
    }

    /** Get the "Paired" RecyclerViewManager. */
    public static RecyclerViewManager  getRvmPaired()    {
        SelectActivity ma = getMainActivity();
        if (ma != null) return ma.rvmPaired;
        else            return null;
    }

    /**
     * Display client interface, initialize Bluetooth, start server worker thread.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ags = (ApplicationGlobalState) getApplication();

        // Display the "client" interface.
        setContentView(R.layout.activity_main);

        rvmPaired         = new RecyclerViewManager(this, R.id.rv_paired);
        rvmDiscovered     = new RecyclerViewManager(this, R.id.rv_discovered);

        // Register receiver for handling Bluetooth events.
        BroadcastReceivers.registerBroadcastReceiver(this, new SelectBroadcastReceiver());

        /*
         * Ensure Bluetooth is enabled; if not, ask the user for permission.
         * Bluetooth configuration and starting the "server" worker thread
         * both occur here if Bluetooth is already enabled; if it isn't,
         * they are started in onActivityResult() if and only if the user
         * allows Bluetooth to be enabled.
         */
        if (BluetoothUtils.isEnabled()) {
            refreshPaired();
            refreshDiscovered();
            BluetoothUtils.requestDiscoverable();
        }
        else {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, RT_BT_ENABLED);
        }

        ags.setAppInitialized();
    }

    @Override
    public void onStart() {
        SelectConnectionListener.startListener();
    }

    @Override
    public void onStop() {
        SelectConnectionListener.stopListener();
    }

    /**
     * Do a device scan.
     *
     * @param v the View which the user clicked on.
     */
    @SuppressWarnings("UnusedParameters")
    public void clickRefreshDiscovered(View v) {
        if (!BluetoothUtils.isEnabled()) {
            Support.userMessage("Bluetooth must be turned on.");
            return;
        }
        Support.userMessage("Refreshing list of discovered devices...");
        refreshDiscovered();
    }

    /**
     * Find and display devices which are already paired with this one.
     *
     * @param v the View the user clicked on.
     */
    @SuppressWarnings("UnusedParameters")
    public void clickRefreshPaired(View v) {
        if (!BluetoothUtils.isEnabled()) {
            Support.userMessage("Bluetooth must be turned on.");
            return;
        }
        Support.userMessage("Refreshing list of paired devices...");
        refreshPaired();
    }

    /** Handle result of request to user to enable Bluetooth. */
    @Override
    protected void onActivityResult( int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case RT_BT_ENABLED:
                if (resultCode == RESULT_OK) {
                    refreshPaired();
                    refreshDiscovered();
                    /** Only make this request once. */
                    if (ags.isAppInitialized())
                        BluetoothUtils.requestDiscoverable();
                    // Server turned on by SelectBroadcastReceiver.
                    return;
                }
                break;
        }
    }

    /** Unregister the BroadcastReceiver. */
    @Override
    public void onDestroy() {
        super.onDestroy();
        BroadcastReceivers.unregisterBroadcastReceiver(this);
    }

    /**
     * Refresh the list of "discovered" devices.
     * <p>
     *     When new devices are discovered, a broadcast is sent out. The "Discovered" RecyclerView
     *     is updated by the BroadcastReceiver.
     *     See {@link SelectBroadcastReceiver#onReceive}.
     */
    private void refreshDiscovered() {
        MyAdapter myAdapter = rvmDiscovered.getAdapter();
        BluetoothDevices btds = myAdapter.getDevices();
        btds.clear();
        BluetoothUtils.startDiscovery();
    }

    /**
     * Find and display devices which are already paired with this one.
     */
    private void refreshPaired() {
        Set<BluetoothDevice> pairedDevices = BluetoothUtils.getPairedDevices();
        if (pairedDevices.size() > 0) {
            MyAdapter myAdapter = rvmPaired.getAdapter();
            BluetoothDevices btds = myAdapter.getDevices();
            btds.clear();
            for (BluetoothDevice device : pairedDevices) {
                btds.add(device);
            }
            myAdapter.notifyDataSetChanged();
        }
    }
}
