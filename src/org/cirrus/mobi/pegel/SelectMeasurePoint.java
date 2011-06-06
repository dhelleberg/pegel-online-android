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


import org.cirrus.mobi.pegel.data.PointStore;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class SelectMeasurePoint extends ListActivity {
	

	private static final String PREFS_NAME = "prefs";

	private String[][] measure_points;
	private String river;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		PointStore ps = ((PegelApplication) getApplication()).getPointStore();
		this.river = getIntent().getStringExtra("river");
		
		String[] plain_points = null;
		try {
			measure_points = ps.getMeasurePoints(this,river);
			plain_points = new String[measure_points.length];
			for (int i = 0; i < plain_points.length; i++) {
				plain_points[i] = measure_points[i][0];
			}
					
		} catch (Exception e) {
			//TODO: Error handling
		}		
		setListAdapter(new ArrayAdapter<String>(this,R.layout.list_item, R.id.SequenceTextView01, plain_points));
		
		TextView head = (TextView) findViewById(R.id.list_head);
		head.setText(river);
		
		PegelApplication pa = (PegelApplication) getApplication();
		pa.trackPageView("/SelectMeasurePoint");

	}


	//listView handler
	public void onListItemClick(ListView parent, View v, int position, long id) { 
		Intent i = new Intent();
		i.setClass(getApplicationContext(),TabbedDataActivity.class);
		i.putExtra("river", river);
		i.putExtra("pnr", this.measure_points[position][1]);
		i.putExtra("mpoint", this.measure_points[position][0]);

		SharedPreferences settings = getSharedPreferences(PREFS_NAME, Context.MODE_WORLD_WRITEABLE);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString("river", river);
		editor.putString("pnr", this.measure_points[position][1]);
		editor.putString("mpoint", this.measure_points[position][0]);
		editor.commit();

		startActivity(i);		
	}

}
