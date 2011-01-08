package org.cirrus.mobi.pegel;

import java.io.InputStream;
import java.net.URL;

import org.cirrus.mobi.pegel.data.PointStore;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

public class PegelDataView extends Activity {


	protected static final String TAG = "PegelDataView";

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
	final Runnable mUpdateImage= new Runnable() {
		public void run() {
			updateImageInUi();
		}
	};

	private String[] data = null;

	protected String imgurl;

	protected Drawable d;


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

		if(this.data == null)
			this.fetchData();
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
			return true;
		default:
			return super.onOptionsItemSelected(item);

		}
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
	}

	protected void updateImageInUi() {
		ImageView img = (ImageView) findViewById(R.id.data_image);
		img.setImageDrawable(d);
		setProgressBarIndeterminateVisibility(false);
	}
	protected void updateDataInUi() {
		((TextView) findViewById(R.id.data_table_measure)).setText(data[0]);
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
		((TextView) findViewById(R.id.data_table_time)).setText(data[2]);
	}
}
