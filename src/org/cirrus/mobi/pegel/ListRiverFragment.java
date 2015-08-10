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
import android.app.Activity;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

@TargetApi(11)
public class ListRiverFragment extends android.support.v4.app.ListFragment implements RiverCallBack {

	int mCurCheckPosition = 0;	
	private AbstractSelectRiver abstractSR;
	private PegelApplication pegelApp;
	
	//private static ListRiverFragment lfr = null;
	
	public static ListRiverFragment getInstance(String river, String mpoint, String pnr)
	{
		//if(lfr == null)
		ListRiverFragment lfr = new ListRiverFragment();
		if(river != null)
		{
			Bundle args = new Bundle();
			args.putString("river", river);
			args.putString("mpoint", mpoint);
			args.putString("pnr", pnr);
			lfr.setArguments(args);
		}		
		return lfr;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		getActivity().setProgressBarIndeterminateVisibility(true);
		this.pegelApp = (PegelApplication) getActivity().getApplication();
		if (savedInstanceState != null) {
			// Restore last state for checked position.
			mCurCheckPosition = savedInstanceState.getInt("curChoice", 0);
		}
		this.abstractSR = new AbstractSelectRiver(getActivity(), this);
		abstractSR.getRivers();			        
	}

	//listView handler
	public void onListItemClick(ListView parent, View v, int position, long id) { 
		//delete preferences
		SharedPreferences settings = getActivity().getSharedPreferences("prefs", Context.MODE_WORLD_WRITEABLE);
		SharedPreferences.Editor edit = settings.edit();
		edit.clear();
		edit.commit();		
		
		showDetails(position);
	}


	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("curChoice", mCurCheckPosition);
	}

	private void showDetails(int position) {

		getListView().setItemChecked(position, true);

		if(position != mCurCheckPosition)
		{
			//transit to new Fragment
			MeasurePointFragment mpf = MeasurePointFragment.newInstance(this.abstractSR.rivers[position]);

			android.support.v4.app.FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();

			// Replace whatever is in the fragment_container view with this fragment,
			// and add the transaction to the back stack
			transaction.replace(R.id.MeasurePoints, mpf);
			//transaction.addToBackStack(null);
			// Commit the transaction
			transaction.commit();
			
			mCurCheckPosition = position;
		}
	}

	@Override
	public void setRivers(String[] rivers) {

		if(rivers != null)
			setListAdapter(new ArrayAdapter<String>(getActivity(),R.layout.list_item, R.id.SequenceTextView01, rivers));
		else
		{
			Toast.makeText(getActivity(),getResources().getText(R.string.connection_error), Toast.LENGTH_LONG).show();
			pegelApp.trackEvent("ERROR-Visible", "ShowRivers-3", "Toast", 1);
		}

		getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		getActivity().setProgressBarIndeterminateVisibility(false);
		
		if(getArguments() != null && getArguments().getString("river") != null && getListAdapter() != null)
		{
			int pos = ((ArrayAdapter<String>) (getListAdapter())).getPosition(getArguments().getString("river"));
			this.showDetails( pos );
			this.setSelection(pos);
		}	
	}
}

