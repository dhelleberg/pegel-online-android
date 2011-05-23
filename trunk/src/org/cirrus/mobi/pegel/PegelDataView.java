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

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Window;
import android.widget.TextView;

public class PegelDataView extends AbstractPegelDetailsActivity {

	protected static final String TAG = "PegelDataView";
	

		
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		getWindow().requestFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		
		setContentView(R.layout.pegel_data);	
		
		pegelApp = (PegelApplication) getApplication();
		pegelApp.tracker.trackPageView("/PegelDataView");	
		
		this.pnr = getIntent().getStringExtra("pnr");
		String river = getIntent().getStringExtra("river");
		String mpoint = getIntent().getStringExtra("mpoint");

		StringBuilder headline = new StringBuilder(river);
		getResources().getConfiguration();
		if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
			headline.append(' ').append(mpoint);
		else
			headline.append('\n').append(mpoint);
		
		TextView headlineView = (TextView) findViewById(R.id.data_headline);
		headlineView.setText(headline);
		
		
		this.pegelDataProvider = PegelDataProvider.getInstance((PegelApplication) getApplication());
		this.pegelDetailHelper = new PegelDetailHelper(this);

		setProgressBarIndeterminateVisibility(true);
		this.pegelDataProvider.showData(pnr, pegelDetailHelper.pdrData, pegelDetailHelper.pdrImage, pegelDetailHelper.pdrDataDetails, null, 0);

	}



}
