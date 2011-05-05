package org.cirrus.mobi.pegel;

import org.cirrus.mobi.pegel.data.PointStore;

import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class MeasurePointFragment extends ListFragment {

	private static final String PREFS_NAME = "prefs";

	private String[][] measure_points;
	
	int mCurCheckPosition = -1;	

	public static MeasurePointFragment newInstance(String river) {
		MeasurePointFragment mpf = new MeasurePointFragment();
		 // Supply index input as an argument.
        Bundle args = new Bundle();
        args.putString("river",river);        
        mpf.setArguments(args);
        return mpf;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		PointStore ps = ((PegelApplication) getActivity().getApplication()).getPointStore();

		String[] plain_points = null;
		try {
			measure_points = ps.getMeasurePoints(getActivity(),getArguments().getString("river"));
			plain_points = new String[measure_points.length];
			for (int i = 0; i < plain_points.length; i++) {
				plain_points[i] = measure_points[i][0];
			}

		} catch (Exception e) {
			//TODO: Error handling
		}		
		setListAdapter(new ArrayAdapter<String>(getActivity(),R.layout.list_item, R.id.SequenceTextView01, plain_points));
		
		getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		
        
        //check if we have a saved preference, then we jump to detailview already
		SharedPreferences settings = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_WORLD_WRITEABLE);
		String mpoint = settings.getString("mpoint", "");
		if(mpoint.length() > 0)
		{
			int pos = ((ArrayAdapter<String> )getListAdapter()).getPosition(mpoint);
			this.select(pos);
			this.setSelection(pos);
		}

	}
	
	//listView handler
	public void onListItemClick(ListView parent, View v, int position, long id) { 	
		this.select(position);
	}

	private void select(int position) {
		getListView().setItemChecked(position, true);
		
		/** save settings */
		SharedPreferences settings = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_WORLD_WRITEABLE);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString("river", getArguments().getString("river"));
		editor.putString("pnr", this.measure_points[position][1]);
		editor.putString("mpoint", this.measure_points[position][0]);
		editor.commit();

		if(position !=  mCurCheckPosition)
		{
			DetailDataFragment df = DetailDataFragment.getInstance(this.measure_points[position][1], getArguments().getString("river") ,this.measure_points[position][0]);			
			
			FragmentTransaction transaction = getFragmentManager().beginTransaction();

			// Replace whatever is in the fragment_container view with this fragment,
			// and add the transaction to the back stack
			transaction.replace(R.id.details, df);
			transaction.addToBackStack(null);
			// Commit the transaction
			transaction.commit();
			
			mCurCheckPosition = position;
			
		}	
	}
}
