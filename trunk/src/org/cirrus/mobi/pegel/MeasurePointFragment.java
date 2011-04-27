package org.cirrus.mobi.pegel;

import org.cirrus.mobi.pegel.data.PointStore;

import android.app.ListFragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class MeasurePointFragment extends ListFragment {

	private static final String PREFS_NAME = "prefs";

	private String[][] measure_points;
	private String river;


	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		PointStore ps = ((PegelApplication) getActivity().getApplication()).getPointStore();
		//this.river = getIntent().getStringExtra("river");

		String[] plain_points = null;
		try {
			measure_points = ps.getMeasurePoints(getActivity(),river);
			plain_points = new String[measure_points.length];
			for (int i = 0; i < plain_points.length; i++) {
				plain_points[i] = measure_points[i][0];
			}

		} catch (Exception e) {
			//TODO: Error handling
		}		
		setListAdapter(new ArrayAdapter<String>(getActivity(),R.layout.list_item, R.id.SequenceTextView01, plain_points));

		TextView head = (TextView) getActivity().findViewById(R.id.list_head);
		head.setText(river);
	}
	
	//listView handler
	public void onListItemClick(ListView parent, View v, int position, long id) { 
		/*Intent i = new Intent();
		i.setClass(getApplicationContext(),PegelDataView.class);
		i.putExtra("river", river);
		i.putExtra("pnr", this.measure_points[position][1]);
		i.putExtra("mpoint", this.measure_points[position][0]);*/

		SharedPreferences settings = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_WORLD_WRITEABLE);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString("river", river);
		editor.putString("pnr", this.measure_points[position][1]);
		editor.putString("mpoint", this.measure_points[position][0]);
		editor.commit();

		//startActivity(i);		
	}

}
