package org.azavea.otm.test;

import android.test.InstrumentationTestCase;


public abstract class OpenTreeMapTestCase extends InstrumentationTestCase {

    @Override
    protected void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getInstrumentation().getTargetContext().getCacheDir().getPath());
    }
}
