package org.cirrus.mobi.pegel;

import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class ListRiverFragment extends ListFragment implements RiverCallBack {

	int mCurCheckPosition = 0;	
	private AbstractSelectRiver abstractSR;
	
	public static ListRiverFragment getInstance(String river, String mpoint, String pnr)
	{
		ListRiverFragment lrf = new ListRiverFragment();
		if(river != null)
		{
			Bundle args = new Bundle();
			args.putString("river", river);
			args.putString("mpoint", mpoint);
			args.putString("pnr", pnr);
			lrf.setArguments(args);
		}		
		return lrf;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		getActivity().setProgressBarIndeterminateVisibility(true);

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
			FragmentTransaction transaction = getFragmentManager().beginTransaction();

			// Replace whatever is in the fragment_container view with this fragment,
			// and add the transaction to the back stack
			transaction.replace(R.id.MeasurePoints, mpf);
			transaction.addToBackStack(null);
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
			Toast.makeText(getActivity(),"Verbindungsfehler zum Server, kann die Daten nicht laden, bitte später nochmal probieren. Sorry!", Toast.LENGTH_LONG).show();

		getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		getActivity().setProgressBarIndeterminateVisibility(false);
		
		if(getArguments() != null && getArguments().getString("river") != null)
		{
			int pos = ((ArrayAdapter<String>) (getListAdapter())).getPosition(getArguments().getString("river"));
			this.showDetails( pos );
			this.setSelection(pos);
		}	
	}
}

