package com.barryholroyd.bluetoothchatdemo.support;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.util.Locale;
import java.util.Stack;

// TBD: Do we need this?

/**
 * Registry to track the creation and destruction of Activities.
 * <p>
 *     ActivityTracker tracks the lifetime of each Activity instance (mirroring,
 *     to some degree, the task's back stack). It can be queried to get the
 *     current Activity as well as the state of the current Activity.
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
 *     Note: This is a more powerful system than BluetoothChatDemo probably needs,
 *     but is it a good demonstration of how Activity instance management can be
 *     thoroughly and cleanly handled.
 */
abstract public class ActivityTracker extends AppCompatActivity
{
    /** The four possible states of an Activity. */
    public enum ActivityState { CREATED, STARTED, RESUMED }

    /** The stack of Activities. This basically mirrors the task's back stack. */
    private static final Stack<ActivityInfo> stack = new Stack<>();

    /** Application Context instance. */
    @SuppressLint("StaticFieldLeak") // not a leak -- this is the app's context
    private static Context appContext = null;

    /**
     * Activity instance and state storage.
     * <p>
     *    Maintained by Activity life cycle callbacks.
     */
    private class ActivityInfo
    {
        private final Activity a;
        private ActivityState state;

        @SuppressWarnings("SameParameterValue")
        ActivityInfo(Activity _a, ActivityState _state) {
            a = _a;
            state = _state;
        }

	    // Getters and Setters
        Activity getActivity() { return a; }
        ActivityState getState() { return state; }
        void setState(ActivityState _state) { state = _state; }
    }

    /**
     * Get the currently running Activity.
     *
     * @return the current Activity instance, if there is one.
     */
    public static Activity getActivity() {
        if (stack.empty())
            return null;
        return stack.peek().getActivity();
    }

    /**
     * Get the state of the currently running Activity.
     *
     * @return the current Activity's state, if there is one.
     */
    @SuppressWarnings("unused")
    public static ActivityState getState() {
        if (stack.empty())
            return null;
        return stack.peek().getState();
    }

    /**
     * The the Application's Context instance.
     * <p>
     *     This is useful when we want to have a Context which doesn't depend on the
     *     life cycle of Activities.
     *
     * @return the Application's Context instance.
     */
    public static Context getAppContext() { return appContext; }

    /**
     * The the Activity's Context instance.
     *
     * @return the Activity's Context instance.
     */
    public static Context getContext() {
        if (stack.empty())
            return null;
        return stack.peek().getActivity();
    }

    /**
     * Register an Activity so that it can be used from worker threads.
     *
     * @param savedInstanceState standard Bundle argument
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
	    trace("onCreate");
        if (appContext == null)
            appContext = getApplicationContext();
        stack.push(new ActivityInfo(this, ActivityState.CREATED));
    }
  
    @Override
    protected void onStart(){
        super.onStart();
	    trace("onStart");
        stack.peek().setState(ActivityState.STARTED);
    }
  
    @Override
    protected void onResume(){
        super.onResume();
	    trace("onResume");
        stack.peek().setState(ActivityState.RESUMED);
    }
  
    @Override
    protected void onPause(){
        super.onPause();
	    trace("onPause");
        stack.peek().setState(ActivityState.STARTED);
    }
  
    @Override
    protected void onStop(){
        super.onStop();
	    trace("onStop");
        stack.peek().setState(ActivityState.CREATED);
    }
  
    /**
     * Unregister the Activity just before it is destroyed.
     */
    @Override
    protected void onDestroy(){
        super.onDestroy();
        trace("onDestroy");
        if (stack.empty()) {
                throw new IllegalStateException(String.format(Locale.US,
                "Unexpected empty stack when trying to pop Activity: %s",
                    getClass()));
        }
        ActivityInfo ai = stack.pop();
        if (!ai.getActivity().equals(this)) {
            throw new IllegalStateException(String.format(Locale.US,
  	        "Bad activity popped from the stack: %s",
                getClass()));
        }
    }

    @Override
    public void finalize() throws Throwable {
        super.finalize();
        trace("finalize");
    }

    private void trace(String label) {
        String s = String.format(Locale.US, "ActivityTrace [%s:%#x]: %s",
                this.getClass().getSimpleName(),
                this.hashCode(), label);
        Support.trace(s);
    }
}
