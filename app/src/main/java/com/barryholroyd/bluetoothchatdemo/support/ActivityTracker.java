package com.barryholroyd.bluetoothchatdemo.support;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.util.Locale;
import java.util.Stack;

/**
 * Registry to track the creation and destruction of Activities.
 * <p>
 *     References to Activities can't be saved for use by worker threads
 *     because the Activity could be destroyed without the worker thread
 *     being aware of it.  The Activity can be destroyed (and recreated) as a
 *     result of a device rotation, in which case the app is still running,
 *     or the entire process can be destroyed as a result of low memory, in
 *     which case both the Activity and the app are destroyed.  Also, hitting
 *     the Back button when there is only one Activity on the task's back
 *     stack will cause the Activity to be destroyed but the app to keep
 *     running in the background (without being immediately recreated).
 * <p>
 *     This class tracks the lifetime of each Activity instance (mirroring,
 *     to some degree, the task's back stack). It can be queried to get the
 *     current Activity as well as the state of the current Activity.  It can
 *     be used, for example, by background methods that need an Activity to
 *     create DialogFragments (an Activity instance is needed to get a
 *     FragmentManager).
 * <p>
 *     It can also be used by anything that needs a current Context. The best
 *     approach here is to pass in the Application Context, but that can be
 *     unwieldy at times. Context Activities should not be relied on in worker
 *     threads since an Activity's Context is actually just the Activity
 *     itself.
 * <p>
 *     To use this class, simply have each of the app's Activities extend it.
 *     They will all then automatically be tracked and the static methods can
 *     be used to access the current Activity.
 */
abstract public class ActivityTracker extends AppCompatActivity
{
    /** The four possible states of an Activity. */
    public enum ActivityState { CREATED, STARTED, RESUMED, DESTROYED }

    /** The stack of Activities. This basically mirrors the task's back stack. */
    private static Stack<ActivityInfo> stack = new Stack<>();

    /**
     * Activity instance and state storage.
     * <p>
     *    Maintained by Activity life cycle callbacks.
     */
    private class ActivityInfo
    {
        private Activity a;
        private ActivityState state;

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
     * @return the Application's Context instance.
     */
    public static Context getAppContext() {
        if (stack.empty())
            return null;
        return stack.peek().getActivity().getApplicationContext();
    }

    /**
     * The the Activity's Context instance.

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

    private void trace(String label) {
	String s = String.format(Locale.US, "AT [%#x]: %s", this.hashCode(), label);
        Support.log(s);
    }
}
