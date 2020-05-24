package org.cirrus.mobi.pegel;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TabActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;

import org.cirrus.mobi.pegel.widget.PegelWidgetProvider.UpdateService;

public class TabbedDataActivity extends TabActivity {

	private boolean norefresh = false;
	private String app_ver;
	private PegelApplication pegelApp;
	private PegelDataProvider pegelDataProvider;

	private static final int DIALOG_ABOUT = 1;
	private static final int DIALOG_NOT_FOUND = 2;


	@Override
	protected void onCreate(Bundle savedInstanceState) {

		try {
			this.app_ver = this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName;

		} catch (NameNotFoundException e) {
			this.app_ver = "unknown";
		}


		this.pegelApp = (PegelApplication) getApplication();
		this.pegelDataProvider = PegelDataProvider.getInstance(pegelApp);


		super.onCreate(savedInstanceState);

		getWindow().requestFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setProgressBarIndeterminateVisibility(true);

		setContentView(R.layout.tabbed_pegel_data);

		//initialize the Data Provider Singleton whith this activity
		PegelDataProvider.getInstance((PegelApplication) getApplication());

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

		intent = new Intent().setClass(this, DataDetailsActivity.class);
		intent.putExtras(getIntent());
		tab = vi.inflate(R.layout.tab_entry, null);
		t = (TextView) tab.findViewById(R.id.tabTitle);
		t.setText(R.string.tab4);	    
		spec = tabHost.newTabSpec("data").setIndicator(tab).setContent(intent);
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
			SharedPreferences settings = getSharedPreferences("prefs", Context.MODE_PRIVATE);
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

	//Menu Inflater for fixture selection
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.detailmenu, menu);
		SharedPreferences settings = getSharedPreferences("prefs", Context.MODE_PRIVATE);
		if(settings.getString("mode","").equals(PegelApplication.ScreenMode.FORCE_SMARTPHONE.toString())){
			inflater.inflate(R.menu.smartphonemenu,menu);
		}
		return true;
	}

	// This method is called once a menu item is selected
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.m_refresh:
			((AbstractPegelDetailsActivity)getCurrentActivity()).refreshFromOptionsMenu();
			return true;
		case R.id.m_feedback:
			final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
			emailIntent.setType("message/rfc822");
			emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{"dominik.helleberg@googlemail.com"});
			emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Pegel-Online Feedback" );  
			this.pegelApp.trackEvent("PegelDataView", "feedback", "feedback", 1);
			startActivity(Intent.createChooser(emailIntent, "Email senden..."));
			return true;
		case R.id.m_about:
			showDialog(DIALOG_ABOUT);
			this.pegelApp.trackEvent("PegelDataView", "about", "about", 1);
			return true;
		case R.id.m_donate:
			Intent i = new Intent(this, DonateActivity.class);			
			this.pegelApp.trackEvent("PegelDataView", "donate", "donate", 1);
			startActivity(i);
			return true;
		case R.id.m_noForceSmartphone:
			SharedPreferences settings = getSharedPreferences("prefs", Context.MODE_PRIVATE);
			settings.edit().remove("mode").commit();
			Intent intent = new Intent(this, StartupActivity.class);
			startActivity(intent);
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
				SharedPreferences settings = getSharedPreferences("prefs", Context.MODE_PRIVATE);
				SharedPreferences.Editor edit = settings.edit();
				edit.clear();
				edit.commit();
				//go back
				Intent i = new Intent(TabbedDataActivity.this, StartupActivity.class);
				startActivity(i);
			}
		});

		return builder.create();
	}
	private Dialog createAboutDialog() {

		final Dialog dialog = new Dialog(this);

		dialog.setContentView(R.layout.about_dialog);
		dialog.setTitle("About Pegel-Online v."+app_ver);

		TextView text = (TextView) dialog.findViewById(R.id.about_d_text);
		text.setText(R.string.about);
		ImageView image = (ImageView) dialog.findViewById(R.id.about_d_logo);
		image.setImageResource(R.drawable.icon);

		return dialog;
	}
	

	public void showNotFoundDialog() {
		showDialog(this.DIALOG_NOT_FOUND);		
	}

}
