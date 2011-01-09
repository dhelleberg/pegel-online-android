package org.cirrus.mobi.pegel.widget;

import org.cirrus.mobi.pegel.PegelApplication;
import org.cirrus.mobi.pegel.R;
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

		Log.v(TAG, "update!");

		//start service to handle updates on click
		context.startService(new Intent(context, UpdateService.class));
	}

	@Override
	public void onReceive(Context ctxt, Intent intent) {
		//if we recieve a refresh-intent, update the widget!
		if (REFRESH_ACTION.equals(intent.getAction())) {
			ctxt.startService(new Intent(ctxt, UpdateService.class));
		}
		else {
			super.onReceive(ctxt, intent);
		}
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
				String[] data = null;
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
					Log.v(TAG, "got new data...");
					StringBuilder measureText = new StringBuilder();
					measureText.append("Messung: ").append(data[0]).append("\n");
					measureText.append("Tendenz: ").append(getTendenz(data[1])).append("\n");					
					measureText.append("Zeit: ").append(data[2]);
					updateViews.setTextViewText(R.id.widget_data, measureText);
				}
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
