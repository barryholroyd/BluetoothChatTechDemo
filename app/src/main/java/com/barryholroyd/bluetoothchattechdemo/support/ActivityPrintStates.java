package com.barryholroyd.bluetoothchattechdemo.support;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

abstract public class ActivityPrintStates extends AppCompatActivity
{
  // Called at the start of the full lifetime.
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    // Initialize Activity and inflate the UI.
    runme("onCreate()");
  }

  // Called after onCreate has finished, use to restore UI state
  @Override
  protected void onRestoreInstanceState(Bundle savedInstanceState) {
    super.onRestoreInstanceState(savedInstanceState);
    // Restore UI state from the savedInstanceState.
    // This bundle has also been passed to onCreate.
    // Will only be called if the Activity has been 
    // killed by the system since it was last visible.
    runme("onRestoreInstanceState()");
  }

  // Called before subsequent visible lifetimes
  // for an activity process.
  @Override
  protected void onRestart(){
    super.onRestart();
    // Load changes knowing that the Activity has already
    // been visible within this process.
    runme("onRestart()");
  }

  // Called at the start of the visible lifetime.
  @Override
  protected void onStart(){
    super.onStart();
    // Apply any required UI change now that the Activity is visible.
    runme("onStart()");
  }

  // Called at the start of the active lifetime.
  @Override
  protected void onResume(){
    super.onResume();
    // Resume any paused UI updates, threads, or processes required
    // by the Activity but suspended when it was inactive.
    runme("onResume()");
  }

  // Called to save UI state changes at the
  // end of the active lifecycle.
  @Override
  protected void onSaveInstanceState(Bundle savedInstanceState) {
    // Save UI state changes to the savedInstanceState.
    // This bundle will be passed to onCreate and 
    // onRestoreInstanceState if the process is
    // killed and restarted by the run time.
    super.onSaveInstanceState(savedInstanceState);
    runme("onSaveInstanceState()");
  }

  // Called at the end of the active lifetime.
  @Override
  protected void onPause(){
    // Suspend UI updates, threads, or CPU intensive processes
    // that don't need to be updated when the Activity isn't
    // the active foreground Activity.
    super.onPause();
    runme("onPause()");
  }

  // Called at the end of the visible lifetime.
  @Override
  protected void onStop(){
    // Suspend remaining UI updates, threads, or processing
    // that aren't required when the Activity isn't visible.
    // Persist all edits or state changes
    // as after this call the process is likely to be killed.
    super.onStop();
    runme("onStop()");
  }

  // Sometimes called at the end of the full lifetime.
  @Override
  protected void onDestroy(){
    // Clean up any resources including ending threads,
    // closing database connections etc.
    super.onDestroy();
    runme("onDestroy()");
  }
  protected void runme(String label) {
    String s = String.format("--> %s %s", this.getClass().getSimpleName(), label);
    Support.trace(s);
  }
}
