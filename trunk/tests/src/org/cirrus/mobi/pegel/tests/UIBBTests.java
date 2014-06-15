package org.cirrus.mobi.pegel.tests;
import com.squareup.spoon.Spoon;
import junit.framework.Assert;

import org.cirrus.mobi.pegel.SelectRiver;
//import org.cirrus.mobi.tests.tools.Screenshot;



import android.content.Context;
import android.content.SharedPreferences;
import android.test.ActivityInstrumentationTestCase2;

import com.jayway.android.robotium.solo.Solo;

public class UIBBTests extends ActivityInstrumentationTestCase2<SelectRiver>{

    private Solo solo;
    private SelectRiver mActivity;

    public UIBBTests() {
        super("org.cirrus.mobi.pegel", SelectRiver.class);
    }

    public void setUp() throws Exception {

        clearPrefs();

        mActivity = getActivity();

        getInstrumentation().waitForIdleSync();
        solo = new Solo(getInstrumentation(), mActivity);

    }

    private void clearPrefs() {
        SharedPreferences prefs = getInstrumentation().getTargetContext().getSharedPreferences("prefs", Context.MODE_WORLD_WRITEABLE);
        SharedPreferences.Editor edit = prefs.edit();
        edit.clear();
        edit.commit();
    }

    public void testSelection() throws Exception {

        try {
            Assert.assertTrue(solo.waitForText("ALLER", 1, 8000));
            Spoon.screenshot(getActivity(), "river");
            solo.clickOnText("RHEIN");
            Assert.assertTrue(solo.waitForText("BONN", 1, 8000));
            Spoon.screenshot(getActivity(), "station-select");
            solo.clickOnText("BONN");
            Assert.assertTrue(solo.waitForText("Ok"));
            Spoon.screenshot(getActivity(), "popup1");
            solo.clickOnButton("Ok");
            Assert.assertTrue(solo.waitForText("Tendenz"));
            Spoon.screenshot(getActivity(), "main-screen1");
            solo.clickOnMenuItem("About");

            getInstrumentation().waitForIdleSync();
//			Screenshot.save_screenshot(solo.getCurrentActivity().getWindow(), "messung");
            ;
            Assert.assertTrue(solo.waitForText("Dominik"));

            solo.goBack();

            solo.clickOnMenuItem("Refresh");
            Spoon.screenshot(getActivity(), "main-screen-refresh");
            solo.clickOnMenuItem("Feedback");
            assertTrue(solo.waitForText("Email senden..."));
            Spoon.screenshot(getActivity(), "about");
            solo.goBack();

            solo.clickOnText("Details");
            assertTrue(solo.waitForText("TuGLW"));
            getInstrumentation().waitForIdleSync();
            Spoon.screenshot(getActivity(), "details");
//			Screenshot.save_screenshot(solo.getCurrentActivity().getWindow(), "details");

            solo.clickOnText("Karte");
            assertTrue(solo.waitForText("Karte"));
            getInstrumentation().waitForIdleSync();
//			Screenshot.save_screenshot(solo.getCurrentActivity().getWindow(), "karte");


        }
        catch(Exception e)
        {
//			Screenshot.save_screenshot(solo.getCurrentActivity().getWindow(), "ERROR"+e.getMessage().trim());
            throw e;
        }
    }

    @Override
    public void tearDown() throws Exception {

        try {
            solo.finalize();
        } catch (Throwable e) {

            e.printStackTrace();
        }
        getActivity().finish();
        super.tearDown();

    }

}

