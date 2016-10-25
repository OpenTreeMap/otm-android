package org.azavea.otm.ui;

import android.support.v7.app.ActionBarActivity;

import org.azavea.otm.App;

/***
 * Custom class for app-wide changes to the Action Bar.
 *
 * This class will grow over time as new overrides are added.
 */
public class OTMActionBarActivity extends ActionBarActivity {
    @Override
    public void onResume() {
        super.onResume();
        // Change the title depending on whether or not
        // an instance is active.
        this.setTitle(App.getInstanceName());
    }

}
