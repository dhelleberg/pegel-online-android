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
