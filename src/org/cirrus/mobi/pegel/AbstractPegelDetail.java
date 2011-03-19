package org.cirrus.mobi.pegel;

import java.io.InputStream;
import java.net.URL;

import org.cirrus.mobi.pegel.data.PointStore;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class AbstractPegelDetail {
	protected static final String TAG = "DetailFragment";

	private static final String HSW = "HSW";

	private Activity parentActivity = null;

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

	private String pnr;


	public AbstractPegelDetail(Activity activity, PegelGrafikView pgv) {
		this.parentActivity  = activity;
		this.pgv = pgv;
	}

	public void showData(String pnr, String river, String mpoint)
	{
		StringBuilder headline = new StringBuilder(river).append('\n').append(mpoint);
		TextView headlineView = (TextView) parentActivity.findViewById(R.id.data_headline);
		headlineView.setText(headline);
		this.pnr = pnr;
		this.fetchData();

	}

	private void fetchData() {
		parentActivity.setProgressBarIndeterminateVisibility(true);

		// Fire off a thread to do some work that we shouldn't do directly in the UI thread
		Thread t = new Thread() {
			public void run() {
				PointStore ps = ((PegelApplication) parentActivity.getApplication()).getPointStore();
				try {
					data = ps.getPointData(pnr);
				} catch (Exception e) {
					Log.w(TAG, "Error fecthing data from server, retry...", e);					

					try {
						data = ps.getPointData(pnr);
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
				PointStore ps = ((PegelApplication) parentActivity.getApplication()).getPointStore();
				try {
					imgurl = ps.getURLData(pnr);
				} catch (Exception e) {
					Log.w(TAG, "Error fecthing data from server, retry...", e);					

					try {
						imgurl = ps.getURLData(pnr);
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
				PointStore ps = ((PegelApplication) parentActivity.getApplication()).getPointStore();
				try {
					dataDetails = ps.getMeasurePointDetails(parentActivity.getApplicationContext(), pnr);
				} catch (Exception e) {
					Log.w(TAG, "Error fecthing data from server, retry...", e);					

					try {
						dataDetails = ps.getMeasurePointDetails(parentActivity.getApplicationContext(), pnr);
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
			ImageView img = (ImageView) parentActivity.findViewById(R.id.data_image);
			img.setImageDrawable(d);
			Animation fadeIn = AnimationUtils.loadAnimation(parentActivity, R.anim.imagealpha);
			img.startAnimation(fadeIn);
		}
		else
		{
			Toast.makeText(parentActivity,"Verbindungsfehler zum Server, kann das Bild nicht laden, bitte noch einmal \"Refresh\" im options-menu probieren. Sorry!", Toast.LENGTH_LONG).show();
		}
		parentActivity.setProgressBarIndeterminateVisibility(false);

	}
	protected void updateDataInUi() {
		if(data != null)
		{
			((TextView) parentActivity.findViewById(R.id.data_table_measure)).setText(data[0]);
			float measure = Float.parseFloat(data[0]);
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
			((TextView) parentActivity.findViewById(R.id.data_table_tendency)).setText(tendency);
			((TextView) parentActivity.findViewById(R.id.data_table_time)).setText(data[2].replace(' ', '\n'));

			SharedPreferences settings = parentActivity.getSharedPreferences("prefs", Context.MODE_WORLD_WRITEABLE);
			SharedPreferences.Editor edit = settings.edit();
			edit.putString("measure", data[0]);
			edit.commit();

			this.pgv.setMeasure(measure);
		}
		else //ERROR
		{
			Toast.makeText(parentActivity,"Verbindungsfehler zum Server, kann die Daten nicht laden, bitte noch einmal \"Refresh\" im options-menu probieren. Sorry!", Toast.LENGTH_LONG).show();
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
			Toast.makeText(parentActivity,"Verbindungsfehler zum Server, nicht alle Daten laden, bitte noch einmal \"Refresh\" im options-menu probieren. Sorry!", Toast.LENGTH_LONG).show();
		}

	}

}
