package org.cirrus.mobi.pegel;

import java.util.Arrays;

import org.cirrus.mobi.pegel.data.PointStore;

import android.app.Activity;
import android.os.Handler;

public class AbstractSelectRiver {
	
	private Activity activity;
	// Need handler for callbacks to the UI thread
	final Handler mHandler = new Handler();

	// Create runnable for posting
	final Runnable mUpdateDaten = new Runnable() {
		public void run() {
			updateDataInUi();
		}
	};
	
	String[] rivers = null;
	private RiverCallBack riverCallback;

	public AbstractSelectRiver(Activity parentActivity, RiverCallBack selectRiver) {
		this.activity = parentActivity;
		this.riverCallback = selectRiver;
	}
	
	protected void updateDataInUi() {
		this.riverCallback.setRivers(rivers);
	}

	void getRivers() {
		// Fire off a thread to do some work that we shouldn't do directly in the UI thread
		Thread t = new Thread() {
			public void run() {
				PointStore ps = ((PegelApplication) activity.getApplication()).getPointStore();
				
				try {
					rivers = ps.getRivers(activity.getApplicationContext());
					Arrays.sort(rivers);
				} catch (Exception e) {
					try {
						rivers = ps.getRivers(activity.getApplicationContext());
						Arrays.sort(rivers);
					} catch (Exception ex) {
						
					}
				}		
				mHandler.post(mUpdateDaten);
			}
		};
		t.start();
	}
}
