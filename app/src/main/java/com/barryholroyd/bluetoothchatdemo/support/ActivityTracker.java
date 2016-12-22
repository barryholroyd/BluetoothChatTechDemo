package com.barryholroyd.bluetoothchatdemo.support;

import android.app.Activity;

import java.util.HashMap;
import java.util.Locale;

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
 *     registerActivity() should be called from onCreate().
 *     unregister() should be called from onDestroy().
 * <p>
 *     It is safe to call unregister() from onDestroy() since the latter will always
 *     be called when an Activity is terminated unless the entire process is being killed.
 * <p>
 *     Note that returning a valid Activity does not mean that it is in the running ("Resumed")
 *     state -- only that it hasn't been destroyed.
 */
public class ActivityTracker
{
    private static HashMap<Class, Activity> hMap = new HashMap<>();

    /**
     * Register an Activity so that it can be used from worker threads.
     *
     * @param a     the Activity instance.
     */
    public static void register(Activity a) {
        Class clazz = a.getClass();
        if (hMap.containsKey(clazz)) {
            throw new IllegalStateException(String.format(Locale.US,
                    "ActivityTracker: attempted to register class %s twice.",
                    clazz.getName()));
        }
        hMap.put(clazz, a);
    }

    /**
     * Get the currently running Activity as specified by "clazz", if there is one.
     *
     * @param clazz the desired Activity class, e.g., MainActivity.class.
     * @param <T>   the type of the class -- it must be an Activity or an extension of Activity.
     * @return the valid Activity instance.
     */
    public static <T extends Class> Activity get(T clazz) {
        return hMap.get(clazz);
    }

    /**
     * Get the currently running Activity
     */
}
