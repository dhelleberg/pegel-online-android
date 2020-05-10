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


import android.annotation.TargetApi;
import android.app.Fragment;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Detailed View of the data
 */
@TargetApi(11)
public class DetailDataFragment extends Fragment {

	private PegelDataProvider pegelDataProvider;
	private PegelDetailHelper pegelDetailHelper;

	public DetailDataFragment() {// for Framework use	
	}

	public static DetailDataFragment getInstance(String pnr, String river, String mpoint) {

		DetailDataFragment df = new DetailDataFragment();

		Bundle args = new Bundle();
		args.putString("pnr", pnr);
		args.putString("river", river);
		args.putString("mpoint", mpoint);
		df.setArguments(args);
		return df;		
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		this.pegelDetailHelper = new PegelDetailHelper(getActivity());
		
		StringBuilder headline = new StringBuilder(getArguments().getString("river"));
		getResources().getConfiguration();
		if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
			headline.append(' ').append(getArguments().getString("mpoint"));
		else
			headline.append('\n').append(getArguments().getString("mpoint"));
		
		TextView headlineView = (TextView) getActivity().findViewById(R.id.data_headline);
		headlineView.setText(headline);
		
		getActivity().setProgressBarIndeterminateVisibility(true);
		this.pegelDataProvider.showData(getArguments().getString("pnr"), pegelDetailHelper.pdrData, pegelDetailHelper.pdrImage, pegelDetailHelper.pdrDataDetails, null,pegelDetailHelper.pdrRealDataDetails, 0);

	}

	public void refresh()
	{
		this.pegelDataProvider.refresh(getArguments().getString("pnr"), pegelDetailHelper.pdrData, pegelDetailHelper.pdrImage, pegelDetailHelper.pdrDataDetails, null,pegelDetailHelper.pdrRealDataDetails, 0);
	}

	@Override
	public View onCreateView(LayoutInflater inflater,
			ViewGroup container, Bundle savedInstanceState) {

		super.onCreateView(inflater, container, savedInstanceState);

		View dataView  = inflater.inflate(R.layout.pegel_data, container, false);
		this.pegelDataProvider = PegelDataProvider.getInstance((PegelApplication) getActivity().getApplication());
	
		return dataView;

	}

	@Override
	public void onStart() {
		super.onStart();
		//refresh options menu
		getActivity().invalidateOptionsMenu();
	}

	@Override
	public void onStop() {
		super.onStop();
		//refresh options menu
		getActivity().invalidateOptionsMenu();
	}
}
