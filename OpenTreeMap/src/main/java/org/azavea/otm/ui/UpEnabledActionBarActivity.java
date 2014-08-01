package org.azavea.otm.ui;

import android.os.Bundle;

public class UpEnabledActionBarActivity extends OTMActionBarActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

}
