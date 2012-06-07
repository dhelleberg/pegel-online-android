package org.cirrus.mobi.pegel.tests;

import junit.framework.Assert;

import org.cirrus.mobi.pegel.SelectMeasurePoint;

import android.content.Intent;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.ListView;

public class SelectPointsTest extends ActivityInstrumentationTestCase2<SelectMeasurePoint>{

	private SelectMeasurePoint mActivity;

	public SelectPointsTest() {
		super("org.cirrus.mobi.pegel", SelectMeasurePoint.class);
	}
	
	@Override
	protected void setUp() throws Exception {
		// TODO Auto-generated method stub
		super.setUp();

		Intent i = new Intent();
		i.putExtra("river", "ALLER");
		setActivityIntent(i);
		
		mActivity = getActivity();
		
	}
	
	public void testListContents() throws Exception {
		getInstrumentation().waitForIdleSync();
		ListView list = (ListView) mActivity.findViewById(android.R.id.list);
		String river1 = (String) list.getAdapter().getItem(0);
		Assert.assertTrue(river1.equals("AHLDEN"));

	}
}
