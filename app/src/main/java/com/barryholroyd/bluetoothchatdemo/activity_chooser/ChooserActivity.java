package com.barryholroyd.bluetoothchatdemo.activity_chooser;

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
import com.barryholroyd.bluetoothchatdemo.support.ActivityTracker;
import com.barryholroyd.bluetoothchatdemo.bluetooth.BluetoothBroadcastReceivers;
import com.barryholroyd.bluetoothchatdemo.support.Support;

import java.util.Set;

/*
 * Fixed
 * BUG: clicking on remote device twice rapidly from Samsung
 * Bug: not asking to be discoverable. TEST: BT on/off when app started; BT toggled on/off.
 * Test
 * BUG: rotate N7 -- died. (chat screen?)
 * Bug: when paired, not refreshing paired.
 * Not Fixed
 *   BUG: ActivityTracker: old Activity still running when new one starts; causes exception!
 * TBD: test thoroughly, including
 *   TBD: turning off BT
 *   TBD: walking out-of-range.
 * TBD: brief code review
 * TBD: finish up comments.
 * TBD: clean out TBDs, etc.
 * TBD: turn off tracing.
 */

/**
 * Display client UI to initiate connection requests and fork off a worker
 * thread to listen for incoming connection requests. Use ChooserClient
 * and ChooserListener, respectively, to perform the actual connect, then
 * instantiate ChatActivity to manage the chat session. ChatActivity uses
 * ChatServer to send and receive text over the Bluetooth connection.
 */
public class ChooserActivity extends ActivityTracker
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
     * only called by ChatActivity) executes while ChooserActivity should be present and
     * available, with one caveat: ChooserClient and ChooserListener classes are both
     * extensions of Thread so that they can set up connections in the background. In both
     * the cases it is possible (although not likely) that the ChooserActivity instance will
     * be gone by the time they need it. For that reason, we route requests for the ChooserActivity
     * instance through ActivityTracker, which tracks the creation and destruction of
     * Activities in this app.
     *
     * @return the current ChooserActivity instance if it exists; else null.
     */
    private static ChooserActivity getChooserActivity() {
        Activity a = getActivity(); // from ActivityTracker

        // This should only happen if this method is called from ChooserActivity's onCreate() method.
        if (a == null) {
            throw new IllegalStateException(
                    "Attempt to access uninitialized ChooserActivity instance.");
        }
        if (! (a instanceof ChooserActivity)) {
            String msg = String.format(
                    "Unexpected Activity type: %s", a.getClass().getSimpleName());
            throw new IllegalStateException(msg);
        }
        return (ChooserActivity) a;
    }

    /** Get the "Discovered" RecyclerViewManager for the current ChooserActivity instance. */
    public static RecyclerViewManager  getRvmDiscovered(){
        return getChooserActivity().rvmDiscovered;
    }

    /** Get the "Paired" RecyclerViewManager for the current ChooserActivity instance. */
    public static RecyclerViewManager  getRvmPaired()    {
        return getChooserActivity().rvmPaired;
    }

    /** Display client interface, initialize Bluetooth, start server worker thread. */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ags = (ApplicationGlobalState) getApplication();

        // Display the "client" interface.
        setContentView(R.layout.activity_chooser);

        rvmPaired         = new RecyclerViewManager(this, R.id.rv_paired);
        rvmDiscovered     = new RecyclerViewManager(this, R.id.rv_discovered);

        //Ensure Bluetooth is enabled; if not, ask the user for permission.
        if (BluetoothUtils.isEnabled()) {
            refreshUI(false, !ags.isAppInitialized());
        }
        else {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, RT_BT_ENABLED);
        }

        ags.setAppInitialized();
    }

    @Override
    public void onStart() {
        super.onStart();

        // picks up new pairing after chat with a new remote device completes
        refreshPaired(false);

        BluetoothBroadcastReceivers.registerBroadcastReceiver(this, new ChooserBroadcastReceiver());
        BluetoothUtils.requestDiscoverable();

        // Start listening for incoming connections.
        ChooserListener.startListener();
    }

    @Override
    public void onStop() {
        super.onStop();
        BluetoothBroadcastReceivers.unregisterBroadcastReceiver(this);

        // Stop listening for incoming connections.
        ChooserListener.stopListener();
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
        refreshDiscovered(false);
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
        refreshPaired(false);
    }

    /** Handle result of request to user to enable Bluetooth. */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case RT_BT_ENABLED:
                if (resultCode == RESULT_OK) {
                    refreshUI(false, true);
                }
                break;
        }
    }

    /**
     * Refresh the user interface when Bluetooth has been enabled.
     * <p>
     *     The background listener for incoming connection requests is started and stopped
     *     by onStart() and onStop(), respectively, as well as by ChooserBroadcastReceiver
     *     when Bluetooth is toggled.
     *
     * @param requestDiscoverable if true, ask the user if they would like the app
     *                            to be discoverable.
     */
    static void refreshUI(boolean clearRequest, boolean requestDiscoverable) {
        refreshPaired(clearRequest);
        refreshDiscovered(clearRequest);

        /* Only make this request once per application run. */
        if (requestDiscoverable)
            BluetoothUtils.requestDiscoverable();
    }

    /**
     * Find and display devices which are already paired with this one.
     */
    static void refreshPaired(boolean clearRequest) {
        Set<BluetoothDevice> pairedDevices = BluetoothUtils.getPairedDevices();
        if (pairedDevices.size() > 0) {
            RecyclerViewAdapter recyclerViewAdapter = ChooserActivity.getRvmPaired().getAdapter();
            BluetoothDevices btds = recyclerViewAdapter.getDevices();
            btds.clear();
            if (!clearRequest) {
                for (BluetoothDevice device : pairedDevices) {
                    btds.add(device);
                }
            }
            recyclerViewAdapter.notifyDataSetChanged();
        }
    }

    /**
     * Refresh the list of "discovered" devices.
     * <p>
     *     When new devices are discovered, a broadcast is sent out. The "Discovered" RecyclerView
     *     is updated by the BroadcastReceiver.
     *     See {@link ChooserBroadcastReceiver#onReceive}.
     */
    static void refreshDiscovered(boolean clearRequest) {
        RecyclerViewAdapter recyclerViewAdapter = ChooserActivity.getRvmDiscovered().getAdapter();
        BluetoothDevices btds = recyclerViewAdapter.getDevices();
        btds.clear();
        if (!clearRequest) {
            BluetoothUtils.startDiscovery();
        }
    }
}
