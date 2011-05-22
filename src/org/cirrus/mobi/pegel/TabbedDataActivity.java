package org.cirrus.mobi.pegel;

import org.cirrus.mobi.pegel.widget.PegelWidgetProvider.UpdateService;

import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.TabHost;
import android.widget.TextView;

public class TabbedDataActivity extends TabActivity {

	private PegelDataProvider abstractPegelDetail;
	private boolean norefresh = false;

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
		intent.putExtras(getIntent());

		LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		View tab = vi.inflate(R.layout.tab_entry, null);
		TextView t = (TextView) tab.findViewById(R.id.tabTitle);
		t.setText(R.string.tab1);	    	    
		// Initialize a TabSpec for each tab and add it to the TabHost
		spec = tabHost.newTabSpec("measure").setIndicator(tab).setContent(intent);
		tabHost.addTab(spec);

		intent = new Intent().setClass(this, MoreDetailsActivity.class);
		intent.putExtras(getIntent());
		tab = vi.inflate(R.layout.tab_entry, null);
		t = (TextView) tab.findViewById(R.id.tabTitle);
		t.setText(R.string.tab2);	    
		spec = tabHost.newTabSpec("details").setIndicator(tab).setContent(intent);
		tabHost.addTab(spec);

		intent = new Intent().setClass(this, MapActivity.class);
		intent.putExtras(getIntent());
		tab = vi.inflate(R.layout.tab_entry, null);
		t = (TextView) tab.findViewById(R.id.tabTitle);
		t.setText(R.string.tab3);	    	    
		spec = tabHost.newTabSpec("map").setIndicator(tab).setContent(intent);
		tabHost.addTab(spec);

	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK)) {
			//delete preferences
			SharedPreferences settings = getSharedPreferences("prefs", Context.MODE_WORLD_WRITEABLE);
			SharedPreferences.Editor edit = settings.edit();
			edit.clear();
			edit.commit();
			this.norefresh = true; //do not refresh service
			
		}
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		// update the widget if we exit
		if(!norefresh) 
			this.startService(new Intent(this, UpdateService.class));
		norefresh = false;
	}
	
}
