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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;

public class StartupActivity extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		
		PegelApplication pa = (PegelApplication) getApplication();
		Intent i = new Intent();


		/**
		 * Display Fragments only on large & xlarge screens
		 * And only if API Level equals or greater Honeycomb
		 */

		SharedPreferences settings = getSharedPreferences("prefs", Context.MODE_PRIVATE);
		if(settings.getString("mode", "").equals(PegelApplication.ScreenMode.FORCE_SMARTPHONE.toString())) {
			i.setClass(getApplicationContext(), SelectRiver.class);
			pa.setScreenMode(PegelApplication.ScreenMode.FORCE_SMARTPHONE);
			startActivity(i);
		}
		else {
			if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
				if ((getResources().getConfiguration().screenLayout &
						Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE) {


					i.setClass(getApplicationContext(), PegelFragmentsActivity.class);
					pa.setScreenMode(PegelApplication.ScreenMode.TABLET);
					//sanity check, I'm getting paranoid about this
					try {
						Class.forName("android.app.Fragment");
					} catch (Exception e) {
						pa.trackEvent("EXCEPTION " + e.getLocalizedMessage(), "Device reports SDK Version:" + android.os.Build.VERSION.SDK_INT + " but has no Fragment class!", android.os.Build.BRAND + " " + android.os.Build.MANUFACTURER + " " + android.os.Build.MODEL, 0);
						i.setClass(getApplicationContext(), SelectRiver.class);
						pa.setScreenMode(PegelApplication.ScreenMode.SMARTPHONE);
					}
				} else if ((getResources().getConfiguration().screenLayout &
						Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_XLARGE) {
					i.setClass(getApplicationContext(), PegelFragmentsActivity.class);
					pa.setScreenMode(PegelApplication.ScreenMode.TABLET);
					//sanity check, I'm getting paranoid about this
					try {
						Class.forName("android.app.Fragment");
					} catch (Exception e) {
						pa.trackEvent("EXCEPTION " + e.getLocalizedMessage(), "Device reports SDK Version:" + android.os.Build.VERSION.SDK_INT + " but has no Fragment class!", android.os.Build.BRAND + " " + android.os.Build.MANUFACTURER + " " + android.os.Build.MODEL, 0);
						i.setClass(getApplicationContext(), SelectRiver.class);
						pa.setScreenMode(PegelApplication.ScreenMode.SMARTPHONE);
					}

				} else {
					i.setClass(getApplicationContext(), SelectRiver.class);
					pa.setScreenMode(PegelApplication.ScreenMode.SMARTPHONE);
				}
			} else {
				i.setClass(getApplicationContext(), SelectRiver.class);
				pa.setScreenMode(PegelApplication.ScreenMode.SMARTPHONE);
			}
		}
		startActivity(i);		

	}

}
