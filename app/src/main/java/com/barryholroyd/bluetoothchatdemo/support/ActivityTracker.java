package com.barryholroyd.bluetoothchatdemo.support;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.util.Locale;

/**
 * Registry to track the creation and destruction of Activities.
 * <p>
 *    It is particularly useful for support methods which need an Activity instance
 *    or Context instance but which don't care about which specific Activity
 *    subclass currently running.  BluetoothChatDemo's Support class' fatalError()
 *    method uses it to create and display DialogFragments (they need a
 *    FragmentManager and that can only be obtained from an Activity
 *    instance). The method can be safely called from worker threads.
 * <p>
 *     ActivityTracker can also be used by anything that needs a current Context.
 *     Access to both the current Activity's Context as well as the Application's
 *     Context is provided. Context Activities should not be relied on in worker
 *     threads since an Activity's Context is actually just the Activity itself.
 * <p>
 *     To use this class, simply have each of the app's Activities extend it.
 *     The creation and destruction of each Activity instance, as well as its
 *     state transitions, will all automatically be tracked and made available
 *     to other classes.
 * <p>
 *     Notes:
 *       o This uses static hooks and so will not work correctly if multiple instances of
 *         the same Activity class are created.
 *       o This is a more powerful system than BluetoothChatDemo probably needs,
 *         but is it a good demonstration of how Activity instance management can
 *         be thoroughly and cleanly handled.
 */
abstract public class ActivityTracker extends AppCompatActivity
{
    /** The four possible states of an Activity. */
    public enum ActivityState { NONE, CREATED, STARTED, RESUMED }

    /** Application Context instance. */
    // not a leak -- this is the app's context
    @SuppressLint("StaticFieldLeak")
    private static Context appContext = null;

    // not a leak -- this is set and unset in onCreate() and onDestroy(), respectively.
    @SuppressLint("StaticFieldLeak")
    private static Activity a = null;

    private static ActivityState state = ActivityState.NONE;

    /*
     * Getters.
     */
    public static Activity getActivity()   { return a; }
    public static Context getContext()     { return a; }
    public static ActivityState getState() { return state; }
    public static Context getAppContext() { return appContext; }

    /*
     * Setters.
     */
    void setState(ActivityState _state)    { state = _state; }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
	    trace("onCreate");
        if (appContext != null) {
            // Multiple instances not supported. See block comment at beginning of this file.
            throw new IllegalStateException(String.format(Locale.US,
                    "Attempted to create multiple instances of %s.",
                    this.getClass().getSimpleName()));
        }
        appContext = getApplicationContext();

        // Register an Activity so that it can be used from worker threads.
        a = this;
    }
  
    @Override
    protected void onStart(){
        super.onStart();
	    trace("onStart");
        setState(ActivityState.STARTED);
    }
  
    @Override
    protected void onResume(){
        super.onResume();
	    trace("onResume");
        setState(ActivityState.RESUMED);
    }
  
    @Override
    protected void onPause(){
        super.onPause();
	    trace("onPause");
        setState(ActivityState.STARTED);
    }
  
    @Override
    protected void onStop(){
        super.onStop();
	    trace("onStop");
        setState(ActivityState.CREATED);
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        trace("onDestroy");

        // Unregister the Activity just before it is destroyed.
        a = null;
        state = ActivityState.NONE;
    }

    @Override
    public void finalize() throws Throwable {
        super.finalize();
        trace("finalize");
    }

    /**
     * Internal trace() method. Output depends on Support.trace().
     *
     * @param label
     */
    private void trace(String label) {
        String s = String.format(Locale.US, "ActivityTrace [%s:%#x]: %s",
                this.getClass().getSimpleName(),
                this.hashCode(), label);
        Support.trace(s);
    }
}
