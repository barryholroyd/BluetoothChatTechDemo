package com.barryholroyd.bluetoothchatdemo.support;

import android.app.Activity;

import java.util.Locale;
import java.util.Stack;

/**
 * Registry to track the creation and destruction of Activities.
 * <p>
 *     This is used, for example, by background methods that need an Activity
 *     (e.g., to create DialogFragments).
 * <p>
 *     References to Activities can't be saved for use by worker threads because
 *     the Activity could be destroyed without the worker thread being aware of it.
 *     The Activity can be destroyed (and recreated) as a result of a device rotation, in
 *     which case the app is still running, or the entire process can be destroyed as a
 *     result of low memory, in which case both the Activity and the app are destroyed.
 *     Also, hitting the Back button when there is only one Activity on the task's
 *     back stack will cause the Activity to be destroyed but the app to keep running
 *     in the background (without being immediately recreated).
 * <p>
 *     register() should be called from onCreate().
 *     unregister() should be called from onDestroy().
 * <p>
 *     It is safe to call unregister() from onDestroy() since the latter will always
 *     be called when an Activity is terminated unless the entire process is being killed.
 * <p>
 *     Note that returning a valid Activity does not mean that it is in the running ("Resumed")
 *     state -- only that it hasn't been destroyed.
 * <p>
 *     TBD: Progress this through the other states using the same approach as
 *     ActivityPrintStates.java. Add a getState() method.
 */
public class ActivityTracker
{
    private static Stack<ActivityInfo> stack = new Stack<>();

    /**
     * Register an Activity so that it can be used from worker threads.
     *
     * @param a     the Activity instance.
     */
    public static void register(Activity a) {
        stack.push(new ActivityInfo(a, ActivityState.CREATED));
    }

    /**
     * Get the currently running Activity.
     *
     * @return the current Activity instance, if there is one.
     */
    public static Activity get() {
        ActivityInfo ai = stack.pop();
        if (ai == null)
            return null;
        return ai.getActivity();
    }

    /**
     * Register an Activity so that it can be used from worker threads.
     *
     * This should be called from onDestroy().
     *
     * @param a     the Activity instance.
     */
    public static void unregister(Activity a) {
        ActivityInfo ai = stack.pop();
        if (!ai.getActivity().equals(a)) {
            throw new IllegalStateException(
                    String.format(Locale.US, "Bad activity popped from the stack: %s",
                            a.getClass()));
        }
    }

}

enum ActivityState { CREATED, STARTED, RESUMED }

class ActivityInfo
{
    private Activity a;
    private ActivityState state;

    ActivityInfo(Activity _a, ActivityState _state) {
        a = _a;
        state = _state;
    }

    Activity getActivity() { return a; }
    ActivityState getState() { return state; }
}