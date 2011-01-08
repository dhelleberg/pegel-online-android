package org.cirrus.mobi.pegel;

import org.cirrus.mobi.pegel.data.PointStore;

import android.app.Application;

public class PegelApplication extends Application {
	
	private PointStore pointStore;

	@Override
	public void onCreate() {
		super.onCreate();
		
		this.pointStore = new PointStore();		
	}
	
	public PointStore getPointStore()
	{
		return this.pointStore;
	}

}
