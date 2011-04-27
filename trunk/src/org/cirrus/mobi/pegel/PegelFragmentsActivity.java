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
		
		setContentView(R.layout.fragment_view);
		
		if(river.length() > 0)
		{		
			String pnr = settings.getString("pnr", "");
			String mpoint = settings.getString("mpoint", "");
		
			
		
			/*FragmentManager fragmentManager = getFragmentManager();
			FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
			
			DetailDataFragment detailPegelFragment = new DetailDataFragment();
			fragmentTransaction.add(R.id.FragementLayout, detailPegelFragment);
			fragmentTransaction.commit();*/
			
			//DetailDataFragment fragment = (DetailDataFragment) getFragmentManager().findFragmentById(R.id.DetailDataFragment);
			//fragment.showData(pnr, river, mpoint);
		
			setProgressBarIndeterminateVisibility(true);
		}
		
		
	}
	
	

}
