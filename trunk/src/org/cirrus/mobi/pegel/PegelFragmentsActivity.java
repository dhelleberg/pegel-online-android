package org.cirrus.mobi.pegel;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Window;

public class PegelFragmentsActivity extends Activity {

	private static final String PREFS_NAME = "prefs";
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getWindow().requestFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		
		//check if we have a saved preference, then we jump to detailview already
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, Context.MODE_WORLD_WRITEABLE);
		String river = settings.getString("river", "");		
		
		ListRiverFragment lrf; 
		
		if(river.length() > 0)
		{
			//saved preferences, show details
			String pnr = settings.getString("pnr", "");
			String mpoint = settings.getString("mpoint", "");			
			lrf = ListRiverFragment.getInstance(river, mpoint, pnr);
		}
		else
			lrf = ListRiverFragment.getInstance(null, null, null);
		getFragmentManager().beginTransaction().add(R.id.ListRiverFragment, lrf).commit();
		
		setContentView(R.layout.fragment_view);
		
	}
	
	

}
