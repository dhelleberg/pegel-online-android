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

import org.cirrus.mobi.pegel.data.PointStore;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

import android.app.Application;
import android.graphics.drawable.Drawable;

public class PegelApplication extends Application {
	
	//for local testing
	//public static final String host = "http://10.0.2.2:8888";
	//for production
	public static final String host = "http://pegel-online.appspot.com";
	
	private PointStore pointStore;
	public GoogleAnalyticsTracker tracker;

	private Drawable d;


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

	public void setCachedImage(Drawable d) {
		this.d = d;		
	}

	public Drawable getCachedDrawable() {
		return d;
	}

}
