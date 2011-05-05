package org.cirrus.mobi.pegel;
/*	Copyright (C) 2011	Dominik Helleberg

This file is part of pegel-online.

pegel-online is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

pegel-online is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with pegel-online.  If not, see <http://www.gnu.org/licenses/>.
*/

import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.maps.MapActivity;

public class PegelFragmentsActivity extends MapActivity {

	private static final String PREFS_NAME = "prefs";
	private static final int DIALOG_ABOUT = 1;

	private String app_ver;
	private PegelApplication pa;
	
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
		{
			lrf = ListRiverFragment.getInstance(null, null, null);
			//show map as well
			MapFragment mf = new MapFragment();
			FragmentTransaction transaction = getFragmentManager().beginTransaction();

			// Replace whatever is in the fragment_container view with this fragment,
			// and add the transaction to the back stack
			transaction.replace(R.id.details, mf);
			transaction.addToBackStack(null);
			// Commit the transaction
			transaction.commit();
	
		}
		getFragmentManager().beginTransaction().add(R.id.ListRiverFragment, lrf).commit();
		
		setContentView(R.layout.fragment_view);
		
		this.pa = (PegelApplication) getApplication();
		pa.tracker.trackPageView("/PegelFragmentsActivity");
		
		try {
			this.app_ver = this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			this.app_ver = "unknown";
		}		
	}
	
	//Menu Inflater action bar
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.layout.detailmenu, menu);
		Fragment f = getFragmentManager().findFragmentById(R.id.details); 
		//check if the pegel-details are shown, only then show the refresh action
		if( (!(f instanceof DetailDataFragment)) || f.isRemoving())
			menu.removeItem(R.id.m_refresh);
		return true;
	}

	// This method is called once a menu item is selected
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.m_refresh:
			DetailDataFragment df = (DetailDataFragment) getFragmentManager().findFragmentById(R.id.details);
			df.refresh();
			this.pa.tracker.trackEvent("PegelDataView", "refresh", "refresh", 1);
			return true;
		case R.id.m_feedback:
			final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
			emailIntent.setType("message/rfc822");
			emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{"dominik.helleberg@googlemail.com"});
			emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Pegel-Online Feedback" );  
			this.pa.tracker.trackEvent("PegelDataView", "feedback", "feedback", 1);
			startActivity(Intent.createChooser(emailIntent, "Email senden..."));
			return true;
		case R.id.m_about:
			showDialog(DIALOG_ABOUT);
			this.pa.tracker.trackEvent("PegelDataView", "about", "about", 1);
			return true;
		default:
			return super.onOptionsItemSelected(item);

		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
	    Dialog dialog;
	    switch(id) {
	    case DIALOG_ABOUT:
	        dialog = createAboutDialog();
	        break;
	    default:
	        dialog = null;
	    }
	    return dialog;
	}
	
	private Dialog createAboutDialog() {
		
		Dialog dialog = new Dialog(this);

		dialog.setContentView(R.layout.about_dialog);
		dialog.setTitle("About Pegel-Online v."+app_ver);

		TextView text = (TextView) dialog.findViewById(R.id.about_d_text);
		text.setText(R.string.about);
		ImageView image = (ImageView) dialog.findViewById(R.id.about_d_logo);
		image.setImageResource(R.drawable.icon);
		
		return dialog;
	}

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}

}
