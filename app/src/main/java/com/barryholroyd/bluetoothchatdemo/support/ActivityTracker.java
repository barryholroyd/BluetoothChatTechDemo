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
 *     ActivityTracker tracks the lifetime of each Activity instance (mirroring,
 *     to some degree, the task's back stack). It can be queried to get the
 *     current Activity as well as the state of the current Activity.
 * <p>
 *    It is particularly useful for support methods which need an Activity or
 *    Activity context but which don't care about the specific Activity
 *    subclass currently running.  BluetoothChatDemo uses it in a Support
 *    method which creates and displays DialogFragments (they need a
 *    FragmentManager and that can only be obtained from an Activity
 *    instance). The method can be safely called from worker threads.
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
 * <p>
 *     Note: BluetoothChatDemo uses this primarily in support of Support.fatalError(),
 *     to provide a current Activity instance so that DialogFragments can be
 *     created and displayed. It is a more powerful system than this app probably needs,
 *     but is it a good demonstration of how Activity instance management can be
 *     thoroughly and cleanly handled.
 */
abstract public class ActivityTracker extends AppCompatActivity
{
    /** The four possible states of an Activity. */
    public enum ActivityState { CREATED, STARTED, RESUMED }

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

    @Override
    public void finalize() throws Throwable {
        super.finalize();
        Support.log(String.format(Locale.US,
                "%s.finalize() called: - %#x",
                this.getClass().getSimpleName(),
                this.hashCode()));
    }

    private void trace(String label) {
	String s = String.format(Locale.US, "AT [%#x]: %s", this.hashCode(), label);
        Support.log(s);
    }
}
