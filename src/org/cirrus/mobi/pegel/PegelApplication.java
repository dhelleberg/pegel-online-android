package org.cirrus.mobi.pegel;

import org.cirrus.mobi.pegel.data.PointStore;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

import android.app.Application;

public class PegelApplication extends Application {
	
	//for local testing
	//public static final String host = "http://10.0.2.2:8888";
	//for production
	public static final String host = "http://pegel-online.appspot.com";
	
	private PointStore pointStore;
	GoogleAnalyticsTracker tracker;


	@Override
	public void onCreate() {
		super.onCreate();
		
		this.pointStore = new PointStore();
		
		tracker = GoogleAnalyticsTracker.getInstance();
		tracker.start("UA-1395561-4",30 ,this); //every 30 seconds the data is send to analytics 
	}
	
	public PointStore getPointStore()
	{
		return this.pointStore;
	}

}
