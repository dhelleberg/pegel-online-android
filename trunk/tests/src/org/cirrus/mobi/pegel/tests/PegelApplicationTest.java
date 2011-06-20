package org.cirrus.mobi.pegel.tests;

import org.cirrus.mobi.pegel.PegelApplication;
import org.cirrus.mobi.pegel.data.PointStore;

import android.content.Context;
import android.content.SharedPreferences;
import android.test.ApplicationTestCase;

public class PegelApplicationTest extends ApplicationTestCase<PegelApplication> {
	
	private PegelApplication mPegelApp;
	private Context mContext;

	public PegelApplicationTest() {
		super(PegelApplication.class);
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
    	createApplication();
    	this.mPegelApp = getApplication();
    	this.mContext = getSystemContext();		
	}
    
    public void testRiversCache() throws Exception {    	
    	PointStore ps = mPegelApp.getPointStore();
    	String[] rivers = ps.getRivers(this.mContext);
    	assertTrue("Check Rhein in Rivers", contains(rivers, "RHEIN"));    	
    }

    public void testRiversUnCache() throws Exception {    	
    	PointStore ps = mPegelApp.getPointStore();
    	clearPrefs(mContext);
    	String[] rivers = ps.getRivers(this.mContext);
    	assertTrue("Check Rhein in Rivers", contains(rivers, "RHEIN"));    	
    }
    
    public void testMPointsCache() throws Exception {
    	PointStore ps = mPegelApp.getPointStore();
    	String[][]mpoints = ps.getMeasurePoints(this.mContext, "RHEIN");
    	assertTrue("Checking D\u00fcsseldorf in Measurepoints for RHEIN",contains(mpoints, "D\u00DCSSELDORF", "2750010"));
    }

    public void testMPointsUnCache() throws Exception {
    	PointStore ps = mPegelApp.getPointStore();
    	clearPrefs(mContext);
    	String[][]mpoints = ps.getMeasurePoints(this.mContext, "RHEIN");
    	assertTrue("Checking D\u00fcsseldorf in Measurepoints for RHEIN", contains(mpoints, "D\u00DCSSELDORF", "2750010"));
    }

    
	private void clearPrefs(Context c) {
		SharedPreferences prefs = c.getSharedPreferences("store", Context.MODE_WORLD_WRITEABLE);
		SharedPreferences.Editor edit = prefs.edit();
		edit.clear();
		edit.commit();
	}
	
    private boolean contains(String[] array, String match)
    {
    	for (String candidate : array) {
			if(candidate.equalsIgnoreCase(match))
				return true;
		}
    	return false;
    }

    private boolean contains(String[][] array, String match1, String match2)
    {
    	for (String[] candidate : array) {
			if(candidate[0].equalsIgnoreCase(match1) && candidate[1].equalsIgnoreCase(match2))
				return true;
		}
    	return false;
    }


}
