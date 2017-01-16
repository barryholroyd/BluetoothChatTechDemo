package com.barryholroyd.bluetoothchattechdemo.activity_chooser;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.barryholroyd.bluetoothchattechdemo.ActivityExtensions;
import com.barryholroyd.bluetoothchattechdemo.R;
import com.barryholroyd.bluetoothchattechdemo.support.ActivityPrintStates;
import com.barryholroyd.bluetoothchattechdemo.bluetooth.BluetoothDevices;
import com.barryholroyd.bluetoothchattechdemo.bluetooth.BluetoothUtils;
import com.barryholroyd.bluetoothchattechdemo.bluetooth.BluetoothBroadcastReceivers;
import com.barryholroyd.bluetoothchattechdemo.support.Support;

import java.util.Set;

/*
 * TBD: brief code review
 * TBD: finish up comments.
 * TBD: clean out TBDs, etc.
 * TBD: turn off tracing.
 * TBD: Update overview.html:
 * Design parameter: can only have a single chat going at once.
 * For both Activities, their background network threads are
 * controlled by onStart() and onStop(). In both cases, the network threads are
 * implemented as Singletons controlled by static start()/stop() methods.
 * Changes to Bluetooth settings (on/off) are
 * caught by their respective BroadcastReceivers, which use onBluetoothToggle()
 * callbacks to also start and stop the background threads.
 * This also ensures everything necessary has been initialized
 */

/**
 * Display client UI to initiate connection requests and fork off a worker
 * thread to listen for incoming connection requests. Use ChooserClient
 * and ChooserListener, respectively, to perform the actual connect, then
 * instantiate ChatActivity to manage the chat session. ChatActivity uses
 * ChatServer to send and receive text over the Bluetooth connection.
 *
 * We will never need more than one ChooserClient and one ChooseListener
 * and will never allow more than once ChatClient and associated ChatServer.
 */
public class ChooserActivity extends ActivityPrintStates implements ActivityExtensions
{
    /** Request codes for onActivityResult(). */
    private static final int RT_BT_ENABLED = 1;

    // Private non-static fields.
    private RecyclerViewManager rvmDiscovered;
    private RecyclerViewManager rvmPaired;

    private static RecyclerViewAdapter mAdapterDiscovered = new RecyclerViewAdapter();
    private static RecyclerViewAdapter mAdapterPaired = new RecyclerViewAdapter();

    // This Activity.
    private static ChooserActivity ca = null;

    /**
     * Get the current Activity instance.
     *
     * @return the current ChooserActivity instance if it exists; else null.
     */
    public static ChooserActivity getActivity() { return ca; }

    /**
     * Get the app's Context instance.
     *
     * @return the app's Context instance.
     */
    public static Context getAppContext() { return ca.getApplicationContext(); }

    /** Get the "Discovered" RecyclerViewManager for the current ChooserActivity instance. */
    RecyclerViewManager  getRvmDiscovered() { return rvmDiscovered; }

    /** Get the "Paired" RecyclerViewManager for the current ChooserActivity instance. */
    RecyclerViewManager  getRvmPaired() { return rvmPaired; }

    /** True only until onResume() is called for the first time. */
    private static boolean appStarting = true;

    /** Display client interface, initialize Bluetooth, start server worker thread. */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Support.init(this);
        BluetoothUtils.init(this);
        Support.getGlobalState().setMainThreadId();

        ca = this;

        // Display the "client" interface.
        setContentView(R.layout.activity_chooser);

        rvmPaired         = new RecyclerViewManager(this, R.id.rv_paired, mAdapterPaired);
        rvmDiscovered     = new RecyclerViewManager(this, R.id.rv_discovered, mAdapterDiscovered);

        //Ensure Bluetooth is enabled; if not, ask the user for permission.
        if (BluetoothUtils.isEnabled()) {
            if (appStarting)
                refreshUI(false);
        }
        else {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, RT_BT_ENABLED);
        }
    }

    @Override
    public void onRestart() {
        super.onRestart();
        if (!BluetoothUtils.isEnabled()) {
            Support.userMessageLong(
                    "Bluetooth disabled. Must enable in Android Settings to continue.");
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        // Picks up new pairing after chat with a new remote device completes.
        refreshPaired(false);

        // Always register so that we can receive Bluetooth on/off broadcasts.
        BluetoothBroadcastReceivers.registerBroadcastReceiver(this, new ChooserBroadcastReceiver());

        // Ask user if the device should be discoverable (asks only once per app lifecycle).
        BluetoothUtils.requestDiscoverable(this);

        // Start listening for incoming connections.
        ChooserListener.startListener();
    }

    @Override
    public void onResume() {
        super.onResume();
        Support.getGlobalState().setCurrentActivity(this);
        appStarting = false;
    }

    @Override
    public void onStop() {
        super.onStop();
        BluetoothBroadcastReceivers.unregisterBroadcastReceiver(this);
        ChooserListener.stopListener();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ca = null;
    }

    /**
     * Do a device scan.
     *
     * @param v the View which the user clicked on.
     */
    @SuppressWarnings("UnusedParameters")
    public void clickRefreshDiscovered(View v) {
        if (!BluetoothUtils.isEnabled()) {
            Support.userMessageShort("Bluetooth must be turned on.");
            return;
        }
        refreshDiscovered(false);
    }

    /**
     * Find and displayShort devices which are already paired with this one.
     *
     * @param v the View the user clicked on.
     */
    @SuppressWarnings("UnusedParameters")
    public void clickRefreshPaired(View v) {
        if (!BluetoothUtils.isEnabled()) {
            Support.userMessageShort("Bluetooth must be turned on.");
            return;
        }
        Support.userMessageShort("Refreshing list of paired devices...");
        refreshPaired(false);
    }

    /** Handle result of request to user to enable Bluetooth. */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case RT_BT_ENABLED:
                if (resultCode == RESULT_OK) {
                    refreshUI(false);
                }
                else {
                    Support.userMessageLong(
                            "Bluetooth disabled. Must enable in Android Settings to continue.");
                }
                break;
        }
    }

    public void onBluetoothToggle() {
        int state = BluetoothUtils.getBluetoothAdapter().getState();
        switch (state) {
            case BluetoothAdapter.STATE_TURNING_ON: break;
            case BluetoothAdapter.STATE_TURNING_OFF: break;
            case BluetoothAdapter.STATE_ON:
                refreshUI(false);
                ChooserListener.startListener();
                Support.userMessageShort("Bluetooth on: BluetoothChatDemo ready.");
                break;
            case BluetoothAdapter.STATE_OFF:
                refreshUI(true);
                ChooserListener.stopListener();
                Support.userMessageShort("Bluetooth off: BluetoothChatDemo paused.");
                break;
        }
    }

    /**
     * Refresh the user interface when Bluetooth has been enabled.
     * <p>
     *     The background listener for incoming connection requests is started and stopped
     *     by onStart() and onStop(), respectively, as well as by ChooserBroadcastReceiver
     *     when Bluetooth is toggled.
     */
    void refreshUI(boolean clearRequest) {
        refreshPaired(clearRequest);
        refreshDiscovered(clearRequest);
        BluetoothUtils.requestDiscoverable(this);
    }

    /**
     * Find and displayShort devices which are already paired with this one.
     */
    void refreshPaired(boolean clearRequest) {
        Set<BluetoothDevice> pairedDevices = BluetoothUtils.getPairedDevices();
        RecyclerViewAdapter recyclerViewAdapter = getRvmPaired().getAdapter();
        BluetoothDevices btds = recyclerViewAdapter.getDevices();
        btds.clear();
        if (!clearRequest) {
            for (BluetoothDevice device : pairedDevices) {
                btds.add(device);
            }
        }
        recyclerViewAdapter.notifyDataSetChanged();
    }

    /**
     * Refresh the list of "discovered" devices.
     * <p>
     *     When new devices are discovered, a broadcast is sent out. The "Discovered" RecyclerView
     *     is updated by the BroadcastReceiver.
     *     See {@link ChooserBroadcastReceiver#onReceive}.
     */
    void refreshDiscovered(boolean clearRequest) {
        RecyclerViewAdapter recyclerViewAdapter = getRvmDiscovered().getAdapter();
        BluetoothDevices btds = recyclerViewAdapter.getDevices();
        btds.clear();
        if (clearRequest) {
            RecyclerViewAdapter recyclerViewAdapterDiscovered = ca.getRvmDiscovered().getAdapter();
            recyclerViewAdapterDiscovered.notifyDataSetChanged();
        }
        else {
            Support.userMessageShort("Refreshing list of discovered devices...");
            BluetoothUtils.startDiscovery();
        }
    }
}
