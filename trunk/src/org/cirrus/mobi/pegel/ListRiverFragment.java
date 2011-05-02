package org.cirrus.mobi.pegel;

import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class ListRiverFragment extends ListFragment implements RiverCallBack {

	int mCurCheckPosition = 0;	
	private AbstractSelectRiver abstractSR;

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
			MeasurePointFragment mpf = new MeasurePointFragment(this.abstractSR.rivers[position]);
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
		this.showDetails(mCurCheckPosition);
	}
}

