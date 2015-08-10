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

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import org.cirrus.mobi.pegel.md.PegelDataFragment;
import org.cirrus.mobi.pegel.md.RefreshIndicatorInterface;


@TargetApi(11)
public class PegelFragmentsActivity extends AppCompatActivity implements RefreshIndicatorInterface {

	private static final String PREFS_NAME = "prefs";
	private static final int DIALOG_ABOUT = 1;
	private static final int DIALOG_NOT_FOUND = 2;

	private String app_ver;
	private PegelApplication pa;
	private int index;
	private ListRiverFragment lrf = null;


	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//getWindow().requestFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

		setContentView(R.layout.fragment_view);

		//check if we have a saved preference, then we jump to detailview already
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, Context.MODE_WORLD_WRITEABLE);
		String river = settings.getString("river", "");		

		if(!findViewById(R.id.ListRiverFragment).isShown())
		{
			if(this.lrf == null)
			{
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
				}
				if(lrf != null)
					getFragmentManager().beginTransaction().replace(R.id.ListRiverFragment, lrf).commit();
			}
		}

		this.pa = (PegelApplication) getApplication();
		pa.trackPageView("/PegelFragmentsActivity");

		try {
			this.app_ver = this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			this.app_ver = "unknown";
		}		
	}


	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		getFragmentManager().beginTransaction().remove(lrf).commit();
		super.onSaveInstanceState(outState);

	}

	//Menu Inflater action bar
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.detailmenu, menu);
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
			this.pa.trackEvent("PegelDataView", "refresh", "refresh3", 1);
			return true;
		case R.id.m_feedback:
			final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
			emailIntent.setType("message/rfc822");
			emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{"dominik.helleberg@googlemail.com"});
			emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Pegel-Online Feedback" );  
			this.pa.trackEvent("PegelDataView", "feedback", "feedback3", 1);
			startActivity(Intent.createChooser(emailIntent, "Email senden..."));
			return true;
		case R.id.m_about:
			showDialog(DIALOG_ABOUT);
			this.pa.trackEvent("PegelDataView", "about", "about3", 1);
			return true;
		case R.id.m_donate:
			Intent i = new Intent(this, DonateActivity.class);			
			this.pa.trackEvent("PegelDataView", "donate", "donate", 1);
			startActivity(i);
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
		case DIALOG_NOT_FOUND:
			dialog = createNotFoundDialog();
			break;

		default:
			dialog = null;
		}
		return dialog;
	}


	private Dialog createNotFoundDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(R.string.pegelNotFound)
		.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				//delete preferences
				SharedPreferences settings = getSharedPreferences("prefs", Context.MODE_WORLD_WRITEABLE);
				SharedPreferences.Editor edit = settings.edit();
				edit.clear();
				edit.commit();
				//clear selection, remove fragments
				Intent i = new Intent(PegelFragmentsActivity.this, PegelFragmentsActivity.class);
				startActivity(i);

			}
		});

		return builder.create();
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

	public void showDetails(String pnr, String river, String mpoint)
	{
		showTabs(pnr, river, mpoint);
	}

	private void showTabs(String pnr, String river, String mpoint)
	{
		getSupportFragmentManager().findFragmentById(R.id.details);
		getSupportFragmentManager().beginTransaction().replace(R.id.details, PegelDataFragment.getNewInstance(pnr,river,mpoint)).commit();





	}

	@Override
	public void isRefreshing(boolean refreshing) {

	}


	/*private void showTabs(String pnr, String river, String mpoint)
	{
		// setup Action Bar for tabs
		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		actionBar.removeAllTabs();

		// add a new tab and set its title text and tab listener
		actionBar.addTab(actionBar.newTab().setText(R.string.tab1)
				.setTabListener(new MyTabListener(DetailDataFragment.getInstance(pnr, river, mpoint),0)),false);

		actionBar.addTab(actionBar.newTab().setText(R.string.tab4)
				.setTabListener(new MyTabListener(RealDetailsFragment.getInstance(pnr),1)),false);

		actionBar.addTab(actionBar.newTab().setText(R.string.tab2)
				.setTabListener(new MyTabListener(MoreDetailsFragment.getInstance(pnr),2)),false);

		actionBar.addTab(actionBar.newTab().setText(R.string.tab3)
				.setTabListener(new MyTabListener(SimpleMapFragment.getInstance(pnr),3)),false);

		actionBar.selectTab(actionBar.getTabAt(index));

	}*/

	private class MyTabListener implements ActionBar.TabListener {
		private Fragment mFragment;
		private int mindex;

		// Called to create an instance of the listener when adding a new tab
		public MyTabListener(Fragment fragment, int mindex) {
			mFragment = fragment;
			this.mindex = mindex;			
		}

		public void onTabSelected(Tab tab, FragmentTransaction ft) {
			if(getFragmentManager() != null) // I have seen NPEs here :(
			{
				Fragment f = getFragmentManager().findFragmentById(R.id.details);
				if(f != mFragment)
				{
					ft.replace(R.id.details, mFragment);			
					index = this.mindex;
				}
			}
		}

		public void onTabReselected(Tab tab, FragmentTransaction ft) {
			// do nothing
		}

		@Override
		public void onTabUnselected(Tab tab, FragmentTransaction ft) {
			// do nothing

		}

	}

	public void pegelNotFound() {
		showDialog(this.DIALOG_NOT_FOUND);
	}
}
