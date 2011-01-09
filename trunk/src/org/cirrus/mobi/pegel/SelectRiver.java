package org.cirrus.mobi.pegel;


import java.util.Arrays;

import org.cirrus.mobi.pegel.data.PointStore;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class SelectRiver extends ListActivity {

	private static final String PREFS_NAME = "prefs";

	// Need handler for callbacks to the UI thread
	final Handler mHandler = new Handler();

	// Create runnable for posting
	final Runnable mUpdateDaten = new Runnable() {
		public void run() {
			updateDataInUi();
		}
	};
	
	String[] rivers = null;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//check if we have a saved preference, then we jump to detailview already
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, Context.MODE_WORLD_WRITEABLE);
		String river = settings.getString("river", "");
		if(river.length() > 0)
		{
			Intent i = new Intent();
			i.setClass(getApplicationContext(),PegelDataView.class);
			i.putExtra("river", river);
			i.putExtra("pnr", settings.getString("pnr", ""));
			i.putExtra("mpoint", settings.getString("mpoint", ""));
			startActivity(i);		
		}

		
		getWindow().requestFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.main);
		setProgressBarIndeterminateVisibility(true);

		getRivers();
	
	}

	protected void updateDataInUi() {
		setListAdapter(new ArrayAdapter<String>(this,R.layout.list_item, R.id.SequenceTextView01, rivers));
		setProgressBarIndeterminateVisibility(false);
	}

	private void getRivers() {
		// Fire off a thread to do some work that we shouldn't do directly in the UI thread
		Thread t = new Thread() {
			public void run() {
				PointStore ps = ((PegelApplication) getApplication()).getPointStore();
				
				try {
					rivers = ps.getRivers(getApplicationContext());
					Arrays.sort(rivers);
					//TODO: might take time, display progress dialog...
				} catch (Exception e) {
					//TODO: Error handling
				}		
				mHandler.post(mUpdateDaten);
			}
		};
		t.start();
	}

	
	
	//listView handler
	public void onListItemClick(ListView parent, View v, int position, long id) { 
		Intent i = new Intent();
		i.setClass(getApplicationContext(),SelectMeasurePoint.class);
		i.putExtra("river", rivers[position]);
		startActivity(i);		
	}
}