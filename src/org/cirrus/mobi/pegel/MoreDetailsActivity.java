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

import java.util.Set;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MoreDetailsActivity extends AbstractPegelDetailsActivity {

	// Create runnable for posting
	final Runnable mUpdateDatenDetails = new Runnable() {
		public void run() {
			updateDataDetailInUi();
		}
	};
	private Bundle data = null;

	private PegelDataResultReciever pdrDataDetails;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		getWindow().requestFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

		setContentView(R.layout.more_details);

		this.pegelDetailHelper = new PegelDetailHelper(this);

		this.pa = (PegelApplication) getApplication();		
		pa.tracker.trackPageView("/PegelDataDetailsView");

		this.pnr = getIntent().getStringExtra("pnr");

		pdrDataDetails = new PegelDataResultReciever(new Handler());
		pdrDataDetails.setReceiver(new DataDetailHandler());
				
		setProgressBarIndeterminateVisibility(true);
		this.pegelDataProvider = PegelDataProvider.getInstance((PegelApplication) getApplication());		
	}
	
	@Override
	protected void onStart() {	
		super.onStart();
		this.pegelDataProvider.showData(pnr, null, null, pdrDataDetails, null,0 );
	}


	protected void updateDataDetailInUi() {	
		Set<String> keys = data.keySet();
		TableLayout tl = (TableLayout) findViewById(R.id.measurepointtable);
		if(tl.getChildCount() > 1)//clean up the rows
			tl.removeViews(1, tl.getChildCount()-1);
		for (String key : keys) {
			
			String[] dat = data.getStringArray(key);
			
			LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View rowView = inflater.inflate(R.layout.table_row, null);

			TextView tv = (TextView) rowView.findViewById(R.id.tableTextP);
			tv.setText(dat[0]);
			
			TextView tv2 = (TextView) rowView.findViewById(R.id.tableTextM);
			String value = (Math.round(Float.parseFloat(dat[3])*100.0) / 100.0)+dat[2];
			tv2.setText(value);
			
			TextView tv3 = (TextView) rowView.findViewById(R.id.tableTextD);
			tv3.setText(dat[1]);
			
			tl.addView(rowView);
		}
	}	
	// This method is called once a menu item is selected
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.m_refresh:
			this.pegelDataProvider.refresh(pnr, null, null, pdrDataDetails, null, 0);
			this.pa.tracker.trackEvent("PegelDataView", "refresh", "refresh", 1);
			return true;
		default:
			return super.onOptionsItemSelected(item);

		}
	}
	
	
	class DataDetailHandler implements PegelDataResultReciever.Receiver
	{
		@Override
		public void onReceiveResult(int resultCode, final Bundle resultData) {
			switch (resultCode) {
			case PegelDataProvider.STATUS_FINISHED:
				data  = resultData;				
				runOnUiThread(mUpdateDatenDetails);
				break;
			default:
				Toast.makeText(getApplicationContext(), getResources().getText(R.string.connection_error), Toast.LENGTH_LONG).show();
				break;
			}
			setProgressBarIndeterminateVisibility(false);
		}
	}

	
}
