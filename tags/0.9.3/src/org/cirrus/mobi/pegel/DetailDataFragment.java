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


import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Detailed View of the data
 */
public class DetailDataFragment extends Fragment {

	private AbstractPegelDetail abstractPegelDetail;
	
	
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
		abstractPegelDetail.showData(getArguments().getString("pnr"), getArguments().getString("river"), getArguments().getString("mpoint"));		
	}
	
	public void refresh()
	{
		abstractPegelDetail.showData(getArguments().getString("pnr"), getArguments().getString("river"), getArguments().getString("mpoint"));
	}

	@Override
	public View onCreateView(LayoutInflater inflater,
			ViewGroup container, Bundle savedInstanceState) {

		super.onCreateView(inflater, container, savedInstanceState);
		
		View dataView  = inflater.inflate(R.layout.data, container, false);
		PegelGrafikView pgv = (PegelGrafikView) dataView.findViewById(R.id.PegelGrafikView);
		this.abstractPegelDetail = new AbstractPegelDetail(getActivity(), pgv);

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
