package org.azavea.otm.ui;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

public class UpEnabledActionBarActivity extends ActionBarActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

}
