package org.cirrus.mobi.pegel.tests;

import junit.framework.Assert;

import org.cirrus.mobi.pegel.SelectRiver;

import com.jayway.android.robotium.solo.Solo;

import android.content.Context;
import android.content.SharedPreferences;
import android.test.ActivityInstrumentationTestCase2;

public class UIBBTests extends ActivityInstrumentationTestCase2<SelectRiver>{

	private Solo solo;
	private SelectRiver mActivity;

	public UIBBTests() {
		super("org.cirrus.mobi.pegel", SelectRiver.class);
	}

	public void setUp() throws Exception {
		mActivity = getActivity();
		clearPrefs();
//		mActivity.finish();
//		mActivity = getActivity();
		solo = new Solo(getInstrumentation(), mActivity);
		
	}
	
	private void clearPrefs() {
		SharedPreferences prefs = mActivity.getSharedPreferences("prefs", Context.MODE_WORLD_WRITEABLE);
		SharedPreferences.Editor edit = prefs.edit();
		edit.clear();
		edit.commit();
	}
	 
	 public void testSelection() throws Exception {
		 Assert.assertTrue(solo.waitForText("ALLER", 1, 8000));
		 solo.clickOnText("RHEIN");
		 Assert.assertTrue(solo.waitForText("BONN", 1, 8000));
		 solo.clickOnText("BONN");
		 Assert.assertTrue(solo.waitForText("Tendenz"));
		 solo.clickOnMenuItem("About");
		 Assert.assertTrue(solo.waitForText("Dominik"));
		 solo.goBack();
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
