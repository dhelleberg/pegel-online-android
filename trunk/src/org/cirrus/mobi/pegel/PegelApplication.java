package org.cirrus.mobi.pegel;

/*	Copyright (C) 2011	Dominik Helleberg
	
	This file is part of pegel-online.

    pegel-online is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    pegel-online is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with pegel-online.  If not, see <http://www.gnu.org/licenses/>.
 */

import java.util.HashMap;
import java.util.Map;

import org.cirrus.mobi.pegel.data.PointStore;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

import android.app.Application;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;

public class PegelApplication extends Application {
	
	private static final String TAG = "PegelApplication";

	//for local testing
//	public static final String host = "http://10.0.2.2:8888";
	//for production
	public static final String host = "http://pegel-online.appspot.com";
	
	private PointStore pointStore;
	private GoogleAnalyticsTracker tracker;

	private Map<String, Drawable> imageCache = null;

	private boolean emulator = false;

	public boolean isEmulator()
	{
		return this.emulator ;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		
		this.pointStore = new PointStore();
		
		tracker = GoogleAnalyticsTracker.getInstance();
		//detect emulator, 		
		tracker.start("UA-1395561-4",30 ,this); //every 30 seconds the data is send to analytics
		
		if(Build.PRODUCT.equalsIgnoreCase("sdk") || Build.PRODUCT.equalsIgnoreCase("google_sdk"))
		{
			StringBuilder deviceInfo = new StringBuilder(Build.PRODUCT).append(' ').append(Build.MANUFACTURER).append(' ').append(Build.DEVICE);
			Log.v(TAG, "Detected emulator, turn off tracker for: "+deviceInfo);
			tracker.trackEvent("DEBUG", "On", deviceInfo.toString(), 1);
			tracker.dispatch();
			tracker.stop();
			this.emulator = true;
		}
		
		this.imageCache = new HashMap<String, Drawable>();
	}
	
	public PointStore getPointStore()
	{
		return this.pointStore;
	}

	public void setCachedImage(String key, Drawable d) {
		this.imageCache.put(key, d);
	}

	public Drawable getCachedDrawable(String key) {
		return this.imageCache.get(key);
	}

	public void trackEvent(String string, String string2, String string3, int i) {
		if(this.emulator)
			Log.v(TAG, "dropping event, running on emulator "+string+string2+string3+i);
		else
			tracker.trackEvent(string, string2, string3, i);		
	}

	public void trackPageView(String string) {
		if(this.emulator)
			Log.v(TAG, "dropping pageView, running on emulator "+string);
		else
			tracker.trackPageView(string);
		
	}

}
