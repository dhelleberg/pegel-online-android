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

import java.io.InputStream;
import java.net.URL;

import org.cirrus.mobi.pegel.data.PointStore;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class PegelDataView extends Activity {


	private static final String HSW = "HSW";

	protected static final String TAG = "PegelDataView";

	private static final int DIALOG_ABOUT = 1;

	private String pegelNummer;

	// Need handler for callbacks to the UI thread
	final Handler mHandler = new Handler();

	// Create runnable for posting
	final Runnable mUpdateDaten = new Runnable() {
		public void run() {
			updateDataInUi();
		}
	};

	// Create runnable for posting
	final Runnable mUpdateImage = new Runnable() {
		public void run() {
			updateImageInUi();
		}
	};
	
	final Runnable mUpdateDetails = new Runnable() {
		public void run() {
			updateDetailInUi();
		}
	};

	private String[] data = null;
	protected String imgurl = null;
	protected Drawable d = null;

	protected String[][] dataDetails = null;

	private PegelGrafikView pgv;

	private float measure = -1f;

	private PegelApplication pa;

	private String app_ver;


	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getWindow().requestFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

		setContentView(R.layout.data);

		setProgressBarIndeterminateVisibility(true);

		this.pegelNummer = getIntent().getStringExtra("pnr");
		String river = getIntent().getStringExtra("river");
		String mpoint = getIntent().getStringExtra("mpoint");

		StringBuilder headline = new StringBuilder(river).append('\n').append(mpoint);
		TextView headlineView = (TextView) findViewById(R.id.data_headline);
		headlineView.setText(headline);
		
		this.pgv = (PegelGrafikView) findViewById(R.id.PegelGrafikView);

		if(this.data == null)
			this.fetchData();
		
		this.pa = (PegelApplication) getApplication();
		pa.tracker.trackPageView("/PegelDataView");
		
		
		try {
			this.app_ver = this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			this.app_ver = "unknown";
		}

	}

	//Menu Inflater for fixture selection
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.layout.detailmenu, menu);
		return true;
	}

	// This method is called once a menu item is selected
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.m_refresh:
			this.fetchData();
			this.pa.tracker.trackEvent("PegelDataView", "refresh", "refresh", 1);
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
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK)) {
			//delete preferences
			SharedPreferences settings = getSharedPreferences("prefs", Context.MODE_WORLD_WRITEABLE);
			SharedPreferences.Editor edit = settings.edit();
			edit.clear();
			edit.commit();			
		}
		return super.onKeyDown(keyCode, event);
	}


	private void fetchData() {
		setProgressBarIndeterminateVisibility(true);
		
		// Fire off a thread to do some work that we shouldn't do directly in the UI thread
		Thread t = new Thread() {
			public void run() {
				PointStore ps = ((PegelApplication) getApplication()).getPointStore();
				try {
					data = ps.getPointData(pegelNummer);
				} catch (Exception e) {
					Log.w(TAG, "Error fecthing data from server, retry...", e);					

					try {
						data = ps.getPointData(pegelNummer);
					} catch (Exception e1) {
						Log.w(TAG, "Error fecthing data from server, giving up...", e);
						//TODO: display error msg
					}
				} 
				mHandler.post(mUpdateDaten);
			}
		};
		t.start();

		//fetch image
		// Fire off a thread to do some work that we shouldn't do directly in the UI thread
		Thread t2 = new Thread() {
			public void run() {
				PointStore ps = ((PegelApplication) getApplication()).getPointStore();
				try {
					imgurl = ps.getURLData(pegelNummer);
				} catch (Exception e) {
					Log.w(TAG, "Error fecthing data from server, retry...", e);					

					try {
						imgurl = ps.getURLData(pegelNummer);
					} catch (Exception e1) {
						Log.w(TAG, "Error fecthing data from server, giving up...", e);
						//TODO: display error msg
					}
				} 
				//fecht image 
				URL imgu;
				try {
					imgu = new URL(imgurl);
					InputStream is = (InputStream) imgu.openConnection().getInputStream();
					d = Drawable.createFromStream(is, "src");					

				} catch (Exception e) {
					Log.w(TAG, "Error fecthing image from server, giving up...", e);
				}
				mHandler.post(mUpdateImage);
			}
		};
		t2.start();

		//fetch details of the measure point
		// Fire off a thread to do some work that we shouldn't do directly in the UI thread
		Thread t3 = new Thread() {
			public void run() {
				PointStore ps = ((PegelApplication) getApplication()).getPointStore();
				try {
					dataDetails = ps.getMeasurePointDetails(getApplicationContext(), pegelNummer);
				} catch (Exception e) {
					Log.w(TAG, "Error fecthing data from server, retry...", e);					

					try {
						dataDetails = ps.getMeasurePointDetails(getApplicationContext(), pegelNummer);
					} catch (Exception e1) {
						Log.w(TAG, "Error fecthing data from server, giving up...", e);
					}
				} 
				mHandler.post(mUpdateDetails);
			}
		};
		t3.start();

		
	}

	protected void updateImageInUi() {
		if(d != null)
		{
			ImageView img = (ImageView) findViewById(R.id.data_image);
			img.setImageDrawable(d);
			Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.imagealpha);
			img.startAnimation(fadeIn);
		}
		else
		{
			Toast.makeText(this,"Verbindungsfehler zum Server, kann das Bild nicht laden, bitte noch einmal \"Refresh\" im options-menu probieren. Sorry!", Toast.LENGTH_LONG).show();
		}
		setProgressBarIndeterminateVisibility(false);

	}
	protected void updateDataInUi() {
		if(data != null)
		{
			((TextView) findViewById(R.id.data_table_measure)).setText(data[0]);
			this.measure = Float.parseFloat(data[0]);
			String tendency = "";
			int t = Integer.parseInt(data[1]);
			switch (t) {
			case 0:
				tendency = "konstant";
				break;
			case 1:
				tendency = "steigend";
				break;
			case -1:
				tendency = "fallend";
				break;
	
			default:
				tendency = "unbekannt";
				break;
			}
			((TextView) findViewById(R.id.data_table_tendency)).setText(tendency);
			((TextView) findViewById(R.id.data_table_time)).setText(data[2].replace(' ', '\n'));
			
			SharedPreferences settings = getSharedPreferences("prefs", Context.MODE_WORLD_WRITEABLE);
			SharedPreferences.Editor edit = settings.edit();
			edit.putString("measure", data[0]);
			edit.commit();
			
			this.pgv.setMeasure(measure);
		}
		else //ERROR
		{
			Toast.makeText(this,"Verbindungsfehler zum Server, kann die Daten nicht laden, bitte noch einmal \"Refresh\" im options-menu probieren. Sorry!", Toast.LENGTH_LONG).show();
		}
	}
	
	private void updateDetailInUi() {
		if(this.dataDetails != null)
		{
			float hsw = 0f;
			//extract data and put it into the PegelGrafikView
			for (int i = 0; i < dataDetails.length; i++) {
				if(dataDetails[i][0].equals(HSW))
				{
					hsw = Float.parseFloat(dataDetails[i][3]);
					this.pgv.setHSW(hsw);
				}
				else if(dataDetails[i][2].equals("cm"))
				{
					//only put "M_I" to the view as more info looks too complicated for now :(
					if(dataDetails[i][0].equals("M_I"))
						this.pgv.addAdditionalData(dataDetails[i][0], dataDetails[i][3]);
				}
			}
			
		}
		else
		{
			Toast.makeText(this,"Verbindungsfehler zum Server, nicht alle Daten laden, bitte noch einmal \"Refresh\" im options-menu probieren. Sorry!", Toast.LENGTH_LONG).show();
		}
		
	}

}
