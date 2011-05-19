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
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class SelectRiver extends ListActivity implements RiverCallBack{

	private static final String PREFS_NAME = "prefs";
	private AbstractSelectRiver abstractSR;

	

	
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
		else
		{   //only fire tracker if we do not forward
			PegelApplication pa = (PegelApplication) getApplication();
			pa.tracker.trackPageView("/SelectRiver");
		}
		
		getWindow().requestFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.main);
		setProgressBarIndeterminateVisibility(true);
		
		this.abstractSR = new AbstractSelectRiver(this, this);
		abstractSR.getRivers();		
	
	}


	//listView handler
	public void onListItemClick(ListView parent, View v, int position, long id) { 
		Intent i = new Intent();
		i.setClass(getApplicationContext(),SelectMeasurePoint.class);
		i.putExtra("river", this.abstractSR.rivers[position]);
		startActivity(i);		
	}

	@Override
	public void setRivers(String[] rivers) {
		
		if(rivers != null)
			setListAdapter(new ArrayAdapter<String>(this,R.layout.list_item, R.id.SequenceTextView01, rivers));
		else
			Toast.makeText(this,getResources().getText(R.string.connection_error), Toast.LENGTH_LONG).show();
		
		setProgressBarIndeterminateVisibility(false);	
	}


}