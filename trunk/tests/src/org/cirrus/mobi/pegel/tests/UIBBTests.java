package org.cirrus.mobi.pegel.tests;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import junit.framework.Assert;

import org.cirrus.mobi.pegel.SelectRiver;

import com.jayway.android.robotium.solo.Solo;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Environment;
import android.test.ActivityInstrumentationTestCase2;
import android.view.View;

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
		screenshot(solo.getCurrentActivity().getWindow().getDecorView());
		Assert.assertTrue(solo.waitForText("Dominik"));		
		screenshot(solo.getCurrentActivity().getWindow().getDecorView());
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

	private static void screenshot(View view) {
		view.setDrawingCacheEnabled(true);
		Bitmap screenshot = view.getDrawingCache(false);

		String filename_pre = "screenshot";
		String filename_suff = ".png";
		String SNAPSHOTDIR = "shots";
		OutputStream outStream = null;
		try {			
			String dirname = Environment.getExternalStorageDirectory()+File.separator+SNAPSHOTDIR+File.separator;
			//check if directory exists
			File dir = new File(dirname);
			dir.mkdir();
			File f = new File(dir, filename_pre+System.currentTimeMillis()+filename_suff);
			f.createNewFile();
			outStream = new FileOutputStream(f);
			screenshot.compress(Bitmap.CompressFormat.PNG, 100, outStream);
			outStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		finally
		{
			if(outStream != null)
			{		
				try {	outStream.close();	} catch (IOException e) {	e.printStackTrace(); }			
			}
		}
		view.setDrawingCacheEnabled(false);
	} 

}
