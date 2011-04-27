package org.cirrus.mobi.pegel;

import android.app.ListFragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class ListRiverFragment extends ListFragment implements RiverCallBack {

	int mCurCheckPosition = 0;
	int mShownCheckPosition = -1;
	private AbstractSelectRiver abstractSR;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		getActivity().setProgressBarIndeterminateVisibility(true);
		this.abstractSR = new AbstractSelectRiver(getActivity(), this);
		abstractSR.getRivers();			        
	}

	//listView handler
	public void onListItemClick(ListView parent, View v, int position, long id) { 

		//TODO: show things
		//i.putExtra("river", this.abstractSR.rivers[position]);
		//startActivity(i);		
	}

	@Override
	public void setRivers(String[] rivers) {
		
		if(rivers != null)
			setListAdapter(new ArrayAdapter<String>(getActivity(),R.layout.list_item, R.id.SequenceTextView01, rivers));
		else
			Toast.makeText(getActivity(),"Verbindungsfehler zum Server, kann die Daten nicht laden, bitte später nochmal probieren. Sorry!", Toast.LENGTH_LONG).show();
		
		getActivity().setProgressBarIndeterminateVisibility(false);	
	}
}

