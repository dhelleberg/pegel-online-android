package org.cirrus.mobi.pegel;

import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.TabHost;
import android.widget.TextView;

public class TabbedDataActivity extends TabActivity {

	private PegelDataProvider abstractPegelDetail;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);

		getWindow().requestFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setProgressBarIndeterminateVisibility(true);
		
		setContentView(R.layout.tabbed_pegel_data);

		this.abstractPegelDetail = PegelDataProvider.getInstance((PegelApplication) getApplication());
		
		Resources res = getResources(); // Resource object to get Drawables
		TabHost tabHost = getTabHost();  // The activity TabHost
		TabHost.TabSpec spec;  // Resusable TabSpec for each tab
		Intent intent;  // Reusable Intent for each tab

		// Create an Intent to launch an Activity for the tab (to be reused)
		intent = new Intent().setClass(this, PegelDataView.class);
		//pass data
		intent.putExtras(getIntent());

		LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		View tab = vi.inflate(R.layout.tab_entry, null);
		TextView t = (TextView) tab.findViewById(R.id.tabText);
		t.setText(R.string.tab1);	    	    
		// Initialize a TabSpec for each tab and add it to the TabHost
		spec = tabHost.newTabSpec("measure").setIndicator(tab).setContent(intent);
		tabHost.addTab(spec);

		tab = vi.inflate(R.layout.tab_entry, null);
		t = (TextView) tab.findViewById(R.id.tabText);
		t.setText(R.string.tab2);	    
		spec = tabHost.newTabSpec("details").setIndicator(tab).setContent(intent);
		tabHost.addTab(spec);

		tab = vi.inflate(R.layout.tab_entry, null);
		t = (TextView) tab.findViewById(R.id.tabText);
		t.setText(R.string.tab3);	    	    
		spec = tabHost.newTabSpec("forecast").setIndicator(tab).setContent(intent);
		tabHost.addTab(spec);

	}
}
