package org.cirrus.mobi.pegel;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;

public class PegelFragmentsActivity extends Activity {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getWindow().requestFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

		String pnr = getIntent().getStringExtra("pnr");
		String river = getIntent().getStringExtra("river");
		String mpoint = getIntent().getStringExtra("mpoint");
		
		setContentView(R.layout.fragment_view);
		
		DetailDataFragment fragment = (DetailDataFragment) getFragmentManager().findFragmentById(R.id.DetailDataFragment);
		fragment.showData(pnr, river, mpoint);
		
		setProgressBarIndeterminateVisibility(true);
		
	}
	
	

}
