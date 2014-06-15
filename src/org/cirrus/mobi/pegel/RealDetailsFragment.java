package org.cirrus.mobi.pegel;

import java.util.Set;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

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


@TargetApi(11)
public class RealDetailsFragment extends Fragment {

	// Create runnable for posting
	final Runnable mUpdateDatenDetails = new Runnable() {
		public void run() {
			updateDataDetailInUi();
		}
	};
	private Bundle data = null;

	private PegelDataResultReciever pdrDataDetails;
	private PegelDataProvider pegelDataProvider;

	public PegelApplication pegelApp;


	public RealDetailsFragment() { //for Framework use

	}
	public static RealDetailsFragment getInstance(String pnr)
	{
		RealDetailsFragment mdf = new RealDetailsFragment();
		Bundle args = new Bundle();
		args.putString("pnr", pnr);
		mdf.setArguments(args);
		return mdf;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		pdrDataDetails = new PegelDataResultReciever(new Handler());
		pdrDataDetails.setReceiver(new DataDetailHandler());
		
		this.pegelApp = (PegelApplication) getActivity().getApplication();
		
		this.pegelDataProvider = PegelDataProvider.getInstance((PegelApplication) getActivity().getApplication());
		
		this.pegelDataProvider.showData(getArguments().getString("pnr"), null, null, null, null, pdrDataDetails,0 );
		
		getActivity().setProgressBarIndeterminateVisibility(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater,
			ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		
		View moreDetailsView  = inflater.inflate(R.layout.data_details, container, false);
		
		return moreDetailsView;
	}

	protected void updateDataDetailInUi() {	
		Set<String> keys = data.keySet();
		TableLayout tl = (TableLayout) getActivity().findViewById(R.id.datatable);
		if(tl.getChildCount() > 1)//clean up the rows
			tl.removeViews(1, tl.getChildCount()-1);
		LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		for (String key : keys) {

			String[] dat = data.getStringArray(key);
			
			View rowView = inflater.inflate(R.layout.table_data_row, tl,false);

			TextView tv = (TextView) rowView.findViewById(R.id.tableTextP);
			tv.setText(dat[0]);

			TextView tv2 = (TextView) rowView.findViewById(R.id.tableTextM);
			String value = dat[1];//(Math.round(Float.parseFloat(dat[3])*100.0) / 100.0)+dat[2];
			tv2.setText(value);

			TextView tv3 = (TextView) rowView.findViewById(R.id.tableTextD);
			tv3.setText(dat[2]);

			tl.addView(rowView);
		}
	}	
	
	class DataDetailHandler implements PegelDataResultReciever.Receiver
	{
		@Override
		public void onReceiveResult(int resultCode, final Bundle resultData) {
			switch (resultCode) {
			case PegelDataProvider.STATUS_FINISHED:
				data  = resultData;
				if(getActivity() != null)
					getActivity().runOnUiThread(mUpdateDatenDetails);
				break;
			default:
				Toast.makeText(getActivity().getApplicationContext(), getResources().getText(R.string.connection_error), Toast.LENGTH_LONG).show();
				pegelApp.trackEvent("ERROR-Visible", "MoreDataDetail3", "Toast", 1);
				break;
			}
			if(getActivity() != null)
				getActivity().setProgressBarIndeterminateVisibility(false);
		}
	}
}
