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

import org.acra.ACRA;
import org.acra.ACRAConfiguration;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
import org.acra.sender.HttpSender;
import org.cirrus.mobi.pegel.data.PointStore;




import android.app.Application;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

@ReportsCrashes(
		mode = ReportingInteractionMode.SILENT,
		httpMethod = HttpSender.Method.POST,
		formUri = "http://104.155.60.110:9090/",
		forceCloseDialogAfterToast = false,
		reportType = HttpSender.Type.JSON,
		formUriBasicAuthLogin="crash",
		formUriBasicAuthPassword="rep0rter"

)
public class PegelApplication extends Application {

	private static final String TAG = "PegelApplication";

	//for local testing
//	public static final String host = "http://10.0.2.2:8888";//:8888";
	//for production
	public static final String host = "http://pegel-online.appspot.com";

	private PointStore pointStore;

	private Map<String, Drawable> imageCache = null;

	private boolean emulator = false;
	private Tracker mTracker;
	private RefWatcher refWatcher;

	public boolean isEmulator()
	{
		return this.emulator ;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		/*ACRAConfiguration config=ACRA.getNewDefaultConfig(this);
		config.setFormUri(getString(R.string.acra_form_uri));
		//config.setFormUriBasicAuthLogin(getString(R.string.acra_form_user));
		//config.setFormUriBasicAuthPassword(getString(R.string.acra_form_pwd));
		config.setReportType(HttpSender.Type.JSON);
		config.setHttpMethod(HttpSender.Method.POST);
		ACRA.setConfig(config);*/
		ACRA.init(this);

		refWatcher = LeakCanary.install(this);

		GoogleAnalytics myInstance = GoogleAnalytics.getInstance(this);
		this.mTracker = myInstance.newTracker(R.xml.analytics);

		this.pointStore = new PointStore();

		

		//detect emulator, 						
		if(Build.PRODUCT.equalsIgnoreCase("sdk") || Build.PRODUCT.equalsIgnoreCase("google_sdk"))
		{
			StringBuilder deviceInfo = new StringBuilder(Build.PRODUCT).append(' ').append(Build.MANUFACTURER).append(' ').append(Build.DEVICE);
			Log.v(TAG, "Detected emulator, turn off tracker for: " + deviceInfo);
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
		Map<String, String> params = new HashMap<String, String>();
		params.put(string, string2);
		if(this.emulator)
			Log.v(TAG, "dropping event, running on emulator "+string+string2+string3+i);
		else
			mTracker.send(params);
	}

	public void trackPageView(String string) {
		if(this.emulator)
			Log.v(TAG, "dropping pageView, running on emulator "+string);
		else
			mTracker.setPage(string);

	}


	public static RefWatcher getRefWatcher(Context context) {
		PegelApplication application = (PegelApplication) context.getApplicationContext();
		return application.refWatcher;
	}



}
