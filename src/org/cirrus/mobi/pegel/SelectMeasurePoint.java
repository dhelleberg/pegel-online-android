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


import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.acra.ACRA;
import org.cirrus.mobi.pegel.data.PegelEntry;
import org.cirrus.mobi.pegel.data.PointStore;
import org.cirrus.mobi.pegel.md.PegelDataActivity;

public class SelectMeasurePoint extends ListActivity {
	

	private static final String PREFS_NAME = "prefs";

	private PegelEntry[] entries;
	private String river;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		this.river = getIntent().getStringExtra("river");
		
		TextView head = (TextView) findViewById(R.id.list_head);
		head.setText(river);	
				
		PegelApplication pa = (PegelApplication) getApplication();
		pa.trackPageView("/SelectMeasurePoint");

	}
	@Override
	protected void onStart() {
		super.onStart();
		Log.d(">>>>", "startup, do load");
		PointsLoader ps = new PointsLoader();
		ps.execute();
	}


	//listView handler
	public void onListItemClick(ListView parent, View v, int position, long id) { 
		Intent i = new Intent();
		i.setClass(getApplicationContext(), PegelDataActivity.class);
		i.putExtra("river", river);
		i.putExtra("pnr", this.entries[position].getPegelnummer());
		i.putExtra("mpoint", this.entries[position].getPegelname());

		SharedPreferences settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString("river", river);
		editor.putString("pnr", this.entries[position].getPegelnummer());
		editor.putString("mpoint", this.entries[position].getPegelname());
		editor.commit();

		startActivity(i);		
	}
	
	class PointsLoader extends AsyncTask<Void, Void, Void>
	{
		PointStore ps = ((PegelApplication) getApplication()).getPointStore();
		String[] plain_points = null;

		@Override
		protected Void doInBackground(Void... params) {
			
			try {
				entries = ps.getMeasurePoints(SelectMeasurePoint.this ,river);
				plain_points = new String[entries.length];
				for (int i = 0; i < plain_points.length; i++) {
					Log.d(">>>>"," entry:"+entries[i].getPegelname());
					plain_points[i] = entries[i].getPegelname();
				}
						
			} catch (Exception e) {			
				ACRA.getErrorReporter().putCustomData("river", river);
				ACRA.getErrorReporter().putCustomData("reason", "Exception during getMeasurePoints()");
				ACRA.getErrorReporter().handleException(e);
			}		
			if(plain_points == null)
			{
				ACRA.getErrorReporter().putCustomData("river", river);
				ACRA.getErrorReporter().putCustomData("reason", "River Points are null??");
				ACRA.getErrorReporter().handleException(null);
				plain_points = new String[0];
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			setListAdapter(new ArrayAdapter<String>(SelectMeasurePoint.this,R.layout.list_item, R.id.SequenceTextView01, plain_points));
		}
		
	}
	

}
