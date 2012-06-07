package org.cirrus.mobi.pegel.tests;

import junit.framework.Assert;

import org.cirrus.mobi.pegel.SelectRiver;
import org.cirrus.mobi.tests.tools.Screenshot;


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
			solo.clickOnText("RHEIN");
			Assert.assertTrue(solo.waitForText("BONN", 1, 8000));
			solo.clickOnText("BONN");
			Assert.assertTrue(solo.waitForText("Tendenz"));
			solo.clickOnMenuItem("About");
	
			getInstrumentation().waitForIdleSync();
			Screenshot.save_screenshot(solo.getCurrentActivity().getWindow(), "messung");
	
			Assert.assertTrue(solo.waitForText("Dominik"));		
			solo.goBack();
			
			solo.clickOnMenuItem("Refresh");
			
			solo.clickOnMenuItem("Feedback");
			assertTrue(solo.waitForText("Email senden..."));
			
			solo.goBack();
			
			solo.clickOnText("Details");
			assertTrue(solo.waitForText("TUGLW"));
			getInstrumentation().waitForIdleSync();
			Screenshot.save_screenshot(solo.getCurrentActivity().getWindow(), "details");
			
			solo.clickOnText("Karte");
			assertTrue(solo.waitForText("Karte"));
			getInstrumentation().waitForIdleSync();
			Screenshot.save_screenshot(solo.getCurrentActivity().getWindow(), "karte");

			
		}
		catch(Exception e)
		{
			Screenshot.save_screenshot(solo.getCurrentActivity().getWindow(), "ERROR"+e.getMessage().trim());
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
