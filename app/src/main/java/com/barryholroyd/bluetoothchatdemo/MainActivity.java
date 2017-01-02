package com.barryholroyd.bluetoothchatdemo;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.barryholroyd.bluetoothchatdemo.bluetooth.BluetoothDevices;
import com.barryholroyd.bluetoothchatdemo.bluetooth.BluetoothMgr;
import com.barryholroyd.bluetoothchatdemo.bluetooth.BluetoothServer;
import com.barryholroyd.bluetoothchatdemo.recyclerview.MyAdapter;
import com.barryholroyd.bluetoothchatdemo.recyclerview.RecyclerViewManager;
import com.barryholroyd.bluetoothchatdemo.support.ActivityTracker;
import com.barryholroyd.bluetoothchatdemo.support.Support;

import java.util.Locale;
import java.util.Set;

/*
Test:
 * TBD: Fix memory leaks.
 *
 * TBD: Test -- is alternate connect approach ever used by either device?
 * TBD: Test -- try killing server (pull USB cable?)
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

    /** Request codes for onActivityResult(). */
    public static final int RT_BT_ENABLED = 1;

    // Private non-static fields.
    private RecyclerViewManager rvmDiscovered;
    private RecyclerViewManager rvmPaired;

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

        // This should only happen if this method is called from MainActivity's onCreate() method.
        if (a == null) {
            Support.fatalError("Attempt to access uninitialized MainActivity instance.");
        }
        if (! (a instanceof MainActivity)) {
            throw new IllegalStateException(String.format(Locale.US,
                    "getMainActivity returned Activity of the wrong class: %s",
                    a.getClass().getSimpleName()));
        }
        return (MainActivity) a;
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
         * DEL: These are order-sensitive.
         */
        rvmPaired         = new RecyclerViewManager(this, R.id.rv_paired);
        rvmDiscovered     = new RecyclerViewManager(this, R.id.rv_discovered);

        /*
         * Ensure Bluetooth is enabled; if not, ask the user for permission.
         * Bluetooth configuration and starting the "server" worker thread
         * both occur here if Bluetooth is already enabled; if it isn't,
         * they are started in onActivityResult() if and only if the user
         * allows Bluetooth to be enabled.
         */
        if (!BluetoothMgr.getBluetoothAdapter().isEnabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, RT_BT_ENABLED);
        }
        else {
            configureBluetooth();
            startServer();
        }

        ags.setAppInitialized();
    }

    /**
     * Do a device scan.
     *
     * @param v the View which the user clicked on.
     */
    public void clickRefreshDiscovered(View v) {
        Support.userMessage("Refreshing list of discovered devices...");
        refreshDiscovered();
    }

    /**
     * Find and display devices which are already paired with this one.
     *
     * @param v the View the user clicked on.
     */
    public void clickRefreshPaired(View v) {
        Support.userMessage("Refreshing list of paired devices...");
        refreshPaired();
    }

    /** Handle result of request to user to enable Bluetooth. */
    @Override
    protected void onActivityResult( int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case RT_BT_ENABLED:
                if (resultCode == RESULT_OK) {
                    configureBluetooth();
                    startServer();
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

    /**
     * Configure the Bluetooth session (init BroadcastReceiver for discovery, refresh paired
     * and discovered windows, request that the device be discoverable).
     * <p>
     *     Passing in the Activity is appropriate because the BroadcastReceiver must be
     *     registered and unregistered at the beginning and end of each MainActivity lifecycle.
     */
    private void configureBluetooth() {
        // Register receiver for handling newly discovered devices during a scan.
        BluetoothMgr.registerBroadcastReceiver(this);
        refreshPaired();
        refreshDiscovered();
        BluetoothMgr.requestDiscoverable();
    }

    /**
     * Fire up a Bluetooth server on this device.
     * <p>
     *     Must ensure that Bluetooth is enabled first. Only a single server should be
     *     run during the lifetime of the application.
     */
    private static synchronized void startServer() {
        if (!MainActivity.getApplicationGlobalState().isServerRunning()) {
            Support.log("Starting server...");
            (new BluetoothServer()).start();
            MainActivity.getApplicationGlobalState().setServerRunning(true);
        }
    }

    /**
     * Refresh the list of "discovered" devices.
     * <p>
     *     When new devices are discovered, a broadcast is sent out. The "Discovered" RecyclerView
     *     is updated by the BroadcastReceiver.
     *     See {@link com.barryholroyd.bluetoothchatdemo.bluetooth.BluetoothBroadcastReceiver#onReceive}.
     */
    public void refreshDiscovered() {
        MyAdapter myAdapter = MainActivity.getRvmDiscovered().getAdapter();
        BluetoothDevices btds = myAdapter.getDevices();
        btds.clear();
        BluetoothMgr.startDiscovery();
    }

    /**
     * Find and display devices which are already paired with this one.
     */
    public static void refreshPaired() {
        Set<BluetoothDevice> pairedDevices = BluetoothMgr.getPairedDevices();
        if (pairedDevices.size() > 0) {
            MyAdapter myAdapter = MainActivity.getRvmPaired().getAdapter();
            BluetoothDevices btds = myAdapter.getDevices();
            btds.clear();
            for (BluetoothDevice device : pairedDevices) {
                btds.add(device);
            }
            myAdapter.notifyDataSetChanged();
        }
    }
}
