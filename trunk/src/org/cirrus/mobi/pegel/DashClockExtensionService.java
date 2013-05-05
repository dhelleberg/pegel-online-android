package org.cirrus.mobi.pegel;

import org.cirrus.mobi.pegel.data.MeasureEntry;
import org.cirrus.mobi.pegel.data.PointStore;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.android.apps.dashclock.api.DashClockExtension;
import com.google.android.apps.dashclock.api.ExtensionData;

public class DashClockExtensionService extends DashClockExtension {

	private static final String PREFS_NAME = "prefs";
	private static final String TAG= "DashClockExtensionService";

	@Override
	protected void onUpdateData(int arg0) {

		PegelApplication pa = (PegelApplication) getApplication();
		pa.trackEvent("DashClock", "refresh", "refresh", 1);

		
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, Context.MODE_WORLD_WRITEABLE);
		String river = settings.getString("river", "");
		if(river.length() > 0)
		{
			String mpoint = settings.getString("mpoint", "");
			String measure = settings.getString("measure", "");
			String pnr = settings.getString("pnr", "");			

			Log.v(TAG, "updateing widget text...");

			PointStore ps = ((PegelApplication) getApplication()).getPointStore();
			MeasureEntry data = null;
			try {
				data = ps.getPointData(pnr);
			} catch (Exception e) {
				Log.w(TAG, "Error fecthing data from server, retry...", e);					

				try {
					data = ps.getPointData(pnr);
				} catch (Exception e1) {
					Log.w(TAG, "Error fecthing data from server, giving up...", e);
					//TODO: display error msg
				}
			}
			if(data !=null)
			{
				if(data.getStatus() == MeasureEntry.STATUS_OK)
				{
					StringBuilder measureText = new StringBuilder();
					measureText.append("Tendenz: ").append(getTendenz(data.getTendenz())).append("\n");					
					measureText.append("Zeit: ").append(data.getZeit());

					
					ExtensionData extdata = new ExtensionData().visible(true)
							.icon(R.drawable.icon)
							.status(data.getMessung())
							.expandedTitle(river+ ": "+data.getMessung())
							.expandedBody(measureText.toString())
							.clickIntent(new Intent(this, StartupActivity.class));

					publishUpdate(extdata);

				}
			}

		}


	}
	
	private String getTendenz(String tendenz) {
		String tendency = "";
		int t = Integer.parseInt(tendenz);
		switch (t) {
		case 0:
			tendency = "konstant";
			break;
		case 1:
			tendency = "steigend";
			break;
		case -1:
			tendency = "fallend";
			break;

		default:
			tendency = "unbekannt";
			break;
		}
		return tendency;
	}



}
