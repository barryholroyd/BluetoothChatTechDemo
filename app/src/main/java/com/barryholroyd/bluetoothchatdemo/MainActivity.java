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
 *
 * TBD: Try server-only version: exit MainActivity immediately after initialization (finish()),
 * TBD: or provide a finish() button.
 *
Test:
  o Cancelling discovery (connect to a device in less than 12 seconds);
    watch logs
 *
 * TBD: Finish comments.
 * TBD: clean out TBDs, log()s, etc.
 */

public class MainActivity extends ActivityTracker
{
    private static RecyclerViewManager rvmDiscovered; // TBD: memory leak
    private static RecyclerViewManager rvmPaired;     // TBD: memory leak
    private static BluetoothAdapter mBluetoothAdapter;
    private static ApplicationGlobalState ags = null;
    public static final int RT_BT_ENABLED = 1;

    // Getters. Static for ease-of-access.
    public static RecyclerViewManager getRvmDiscovered()    { return rvmDiscovered; }
    public static RecyclerViewManager getRvmPaired()        { return rvmPaired; }
    public static BluetoothAdapter    getBluetoothAdapter() { return mBluetoothAdapter; }
    public static ApplicationGlobalState getApplicationGlobalState() { return ags; }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ags = (ApplicationGlobalState) getApplication();

        setContentView(R.layout.activity_main);
        /*
         * These are order-sensitive. They are re-created on each device re-configuration.
         * Retaining the RecyclerViews would cause their associated Activity instance to
         * be retained, causing a memory leak.
         */
        rvmDiscovered     = new RecyclerViewManager(this, R.id.rv_discovered);
        rvmPaired         = new RecyclerViewManager(this, R.id.rv_paired);
        mBluetoothAdapter = BluetoothMgr.getBluetoothAdapter();

        // Ensure it is enabled; if not, ask the user for permission.
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

    //DEL:
    @Override
    public void finalize() throws Throwable {
        super.finalize();
        Support.log(String.format(Locale.US,
                "MainActivity.finalize() called: %s - %#x",
                this.getClass().getSimpleName(),
                this.hashCode()));
    }
}

