package org.cirrus.mobi.pegel.widget;

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

import org.cirrus.mobi.pegel.PegelApplication;
import org.cirrus.mobi.pegel.R;
import org.cirrus.mobi.pegel.StartupActivity;
import org.cirrus.mobi.pegel.data.MeasureEntry;
import org.cirrus.mobi.pegel.data.PointStore;

import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RemoteViews;

public class PegelWidgetProvider extends AppWidgetProvider {

	private static final String PREFS_NAME = "prefs";
	private static final String TAG = "PegelWidgetProvider";

	public static final String REFRESH_ACTION ="org.cirrus.mobi.pegel.widget.REFRESH";

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		final int N = appWidgetIds.length;
		Log.v(TAG, "on update");		
		// Perform this loop procedure for each App Widget that belongs to this provider
		for (int i=0; i<N; i++) {
			int appWidgetId = appWidgetIds[i];
			Log.v(TAG, "update!");
			//start service to handle updates on click
			context.startService(new Intent(context, UpdateService.class));
		}
	}

	@Override
	public void onReceive(Context ctxt, Intent intent) {
		Log.v(TAG, "on Recieve! "+intent.getAction() );
		//if we recieve a refresh-intent, update the widget!
		if (REFRESH_ACTION.equals(intent.getAction())) {
			ctxt.startService(new Intent(ctxt, UpdateService.class));
		}
		else {
			super.onReceive(ctxt, intent);
		}
	}

	@Override
	public void onDeleted(Context ctxt, int[] ids)
	{
		Log.v(TAG, "on deleted");		
	}


	@Override
	public void onEnabled(Context ctxt)
	{
		Log.v(TAG, "on enabled!");		
	}

	@Override
	public void onDisabled(Context ctxt)
	{
		Log.v(TAG, "on disabled, free ressources!");
		ctxt.stopService(new Intent(ctxt, UpdateService.class));
	}


	public static class UpdateService extends IntentService {

		private static final String TAG = "PegelWidgetProvider$UpdateService";

		public UpdateService() {
			super(".widget.PegelWidgetProvider$UpdateService");
		}

		@Override
		protected void onHandleIntent(Intent intent) {
			generateUpdate(this);
		}

		private void generateUpdate(Context context)
		{
			Log.v(TAG, "upadte in service!");
			ComponentName me = new ComponentName(this, PegelWidgetProvider.class);

			AppWidgetManager mgr = AppWidgetManager.getInstance(this);			

			PegelApplication pa = (PegelApplication) getApplication();
			pa.trackEvent("WidgetView", "refresh", "refresh", 1);

			// Get the layout for the App Widget and attach an on-click listener to the button
			RemoteViews updateViews = new RemoteViews(context.getPackageName(), R.layout.pegel_widget);

			SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_WORLD_WRITEABLE);
			String river = settings.getString("river", "");
			if(river.length() > 0)
			{

				//show progress indicator
				updateViews.setViewVisibility(R.id.widget_update, View.VISIBLE);
				//update wigdet
				mgr.updateAppWidget(me, updateViews);

				String mpoint = settings.getString("mpoint", "");
				String measure = settings.getString("measure", "");
				String pnr = settings.getString("pnr", "");			
				updateViews.setTextViewText(R.id.widget_head, river +" "+ mpoint);

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
						Log.v(TAG, "got new data...");
						StringBuilder measureText = new StringBuilder();
						measureText.append("Messung: ").append(data.getMessung()).append("\n");
						measureText.append("Tendenz: ").append(getTendenz(data.getTendenz())).append("\n");					
						measureText.append("Zeit: ").append(data.getZeit());
						updateViews.setTextViewText(R.id.widget_data, measureText);
					}
					else if(data.getStatus() == MeasureEntry.STATUS_NOT_FOUND)
					{
						//show main Application and let the user select a river
						Intent i = new Intent(this, StartupActivity.class);
						i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						startActivity(i);
			
					}
				}
			}
			else
			{
				//show main Application and let the user select a river
				Intent i = new Intent(this, StartupActivity.class);
				i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(i);
			}


			//register Intent to update on click, that will be passed to the "onRevieve" method of the PegelWidgetProvider who call us then
			Intent i=new Intent(this, PegelWidgetProvider.class);
			i.setAction(REFRESH_ACTION);
			PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
			updateViews.setOnClickPendingIntent(R.id.widget_layout, pi);

			//show progress indicator
			updateViews.setViewVisibility(R.id.widget_update, View.INVISIBLE);

			mgr.updateAppWidget(me, updateViews);

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
}
