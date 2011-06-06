package org.cirrus.mobi.pegel.tests;

import junit.framework.Assert;

import org.cirrus.mobi.pegel.PegelApplication;
import org.cirrus.mobi.pegel.SelectRiver;


import android.app.Instrumentation;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ListView;
import android.widget.ProgressBar;


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

public class SelectRiversTest extends ActivityInstrumentationTestCase2<SelectRiver>{

	private SelectRiver mActivity;

	public SelectRiversTest() {
		super("org.cirrus.mobi.pegel", SelectRiver.class);

	}

	private boolean seen = false;
	
	@Override
	protected void setUp() throws Exception {
		// TODO Auto-generated method stub
		super.setUp();

		mActivity = getActivity();
		//clear Prefs 
		clearPrefs();
		
		//restart activity
		mActivity.finish();
		mActivity = getActivity();

		
		/*View v = mActivity.getWindow().getDecorView();
		final ProgressBar pb =  (ProgressBar) v.findViewById(16908812);
		Log.v("TEST!", "1st time "+pb.getVisibility());
		
		final ViewTreeObserver vto = pb.getViewTreeObserver();
		vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {			
			@Override
			public void onGlobalLayout() {
				Log.v("TEST!", "change time");
				if(pb.getVisibility() == View.VISIBLE)
				{
					Log.v("TEST!", "true");
					vto.removeGlobalOnLayoutListener(this);
					seen = true;
				}				
			}
		});
		vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
			
			@Override
			public boolean onPreDraw() {
				Log.v("TEST!!!", "change time "+pb.isi);
				if(pb.getVisibility() == View.VISIBLE)
				{
					Log.v("TEST!!!", "true");
					vto.removeOnPreDrawListener(this);
					seen = true;
				}
				return false;
			}
		});*/
		
		

	}


	/*public void test0ProgressIndicator() throws Exception {
		
		Thread.sleep(2000);
		Assert.assertTrue(seen);
		
	}*/

	public void testListContents() throws Exception {
		getInstrumentation().waitForIdleSync();
		if(mActivity.getListAdapter() == null || mActivity.getListAdapter().isEmpty())		
			Thread.sleep(2000) ;//wait for rivers to be fetched
		
		ListView list = (ListView) mActivity.findViewById(android.R.id.list);
		String river1 = (String) list.getAdapter().getItem(0);
		Assert.assertTrue(river1.equals("ALLER"));
	}
	


	private void clearPrefs() {
		SharedPreferences prefs = mActivity.getSharedPreferences("prefs", Context.MODE_WORLD_WRITEABLE);
		SharedPreferences.Editor edit = prefs.edit();
		edit.clear();
		edit.commit();
	}

}
