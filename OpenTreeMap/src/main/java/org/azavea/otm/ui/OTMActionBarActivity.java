package org.azavea.otm.ui;

import android.app.Activity;

import org.azavea.otm.App;

/***
 * Custom class for app-wide changes to the Action Bar.
 *
 * This class will grow over time as new overrides are added.
 */
public class OTMActionBarActivity extends Activity {
    @Override
    public void onResume() {
        super.onResume();
        // Change the title depending on whether or not
        // an instance is active.
        this.setTitle(App.getInstanceName());
    }

}
