package org.cirrus.mobi.pegel;

import android.test.ActivityInstrumentationTestCase2;

/**
 * This is a simple framework for a test of an Application.  See
 * {@link android.test.ApplicationTestCase ApplicationTestCase} for more information on
 * how to write and extend Application tests.
 * <p/>
 * To run this test, you can type:
 * adb shell am instrument -w \
 * -e class org.cirrus.mobi.pegel.StartupActivityTest \
 * org.cirrus.mobi.pegel.tests/android.test.InstrumentationTestRunner
 */
public class StartupActivityTest extends ActivityInstrumentationTestCase2<StartupActivity> {

    public StartupActivityTest() {
        super("org.cirrus.mobi.pegel", StartupActivity.class);
    }

}
