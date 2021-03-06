package org.cirrus.mobi.pegel;

import java.util.Set;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

public class PegelDetailHelper {

	protected PegelDataResultReciever pdrData;
	protected PegelDataResultReciever pdrImage;
	protected PegelDataResultReciever pdrDataDetails;
	protected PegelDataResultReciever pdrRealDataDetails;

	// Need handler for callbacks to the UI thread
	final Handler mHandler = new Handler();
	
	public float pegel;
	public String tendency;
	public String time;
	public Bundle data;
	public float hsw;

	private static final String M_I = "M_I";
	private static final String HSW = "HSW";

	
	final Runnable mUpdateImage= new Runnable() {
		public void run() {
			updateImageInUi();
		}
	};

	// Create runnable for posting
	final Runnable mUpdateDaten = new Runnable() {
		public void run() {
			updateDataInUi();
		}
	};

	// Create runnable for posting
	final Runnable mUpdateDatenDetails = new Runnable() {
		public void run() {
			updateDataDetailInUi();
		}
	};

	private Activity activity;
	private PegelApplication pegelApp;

	public PegelDetailHelper(Activity activity) {
		this.activity = activity;
		this.pegelApp = (PegelApplication) activity.getApplication();
		pdrImage = new PegelDataResultReciever(mHandler);
		pdrImage.setReceiver(new ImageHandler());
		
		pdrData = new PegelDataResultReciever(mHandler);
		pdrData.setReceiver(new DataHandler());

		pdrDataDetails = new PegelDataResultReciever(mHandler);
		pdrDataDetails.setReceiver(new DataDetailHandler());

	}
	
	protected void updateDataDetailInUi() {
		PegelGrafikView pgv = (PegelGrafikView) activity.findViewById(R.id.PegelGrafikView);	
		Set<String> keys = data.keySet();
		boolean show = false;
		for (String key : keys) {
			if(key.equals(HSW))
			{
				String[]dat = data.getStringArray(key);
				pgv.setHSW(Float.parseFloat(dat[3]));
				show = true;
			}
			else if(key.equals(M_I))
			{
				String[]dat = data.getStringArray(key);
				pgv.addAdditionalData(dat[0], dat[3]);
			}
		}		
		//we have data, show it!
		if(show)
			pgv.setVisibility(View.VISIBLE);
	}
	
	protected void updateDataInUi() {

        TextView textView = (TextView) activity.findViewById(R.id.data_table_measure);
		if(textView != null)
        {
            textView.setText(pegel+"");
            PegelGrafikView pgv = (PegelGrafikView) activity.findViewById(R.id.PegelGrafikView);
            pgv.setMeasure(pegel);
            ((TextView) activity.findViewById(R.id.data_table_tendency)).setText(tendency);
            ((TextView) activity.findViewById(R.id.data_table_time)).setText(time);
        }
	}

	protected void updateImageInUi() {
		ImageView img = (ImageView) activity.findViewById(R.id.data_image);
		img.setImageDrawable(((PegelApplication) activity.getApplication()).getCachedDrawable("pegel"));
		Animation fadeIn = AnimationUtils.loadAnimation(activity, R.anim.imagealpha);
		img.startAnimation(fadeIn);		
		activity.setProgressBarIndeterminateVisibility(false);
	}
	
	class DataHandler implements PegelDataResultReciever.Receiver
	{
		@Override
		public void onReceiveResult(int resultCode, Bundle resultData) {
			switch (resultCode) {
			case PegelDataProvider.STATUS_FINISHED:
				pegel = resultData.getFloat("pegel");
				tendency = resultData.getString("tendency");
				time = resultData.getString("time");
				activity.runOnUiThread(mUpdateDaten);
				break;
			case PegelDataProvider.STATUS_NOTFOUND:
				data = null;
				if(activity instanceof PegelFragmentsActivity)
					((PegelFragmentsActivity)activity).pegelNotFound();
				else if(activity instanceof PegelDataView)
					((PegelDataView)activity).pegelNotFound();
				
			default:
				Toast.makeText(activity, activity.getResources().getText(R.string.connection_error), Toast.LENGTH_LONG).show();
				pegelApp.trackEvent("ERROR-Visible", "Data", "Toast", 1);
				break;
			}
		}
	}
	
	class DataDetailHandler implements PegelDataResultReciever.Receiver
	{
		@Override
		public void onReceiveResult(int resultCode, final Bundle resultData) {
			switch (resultCode) {
			case PegelDataProvider.STATUS_FINISHED:
				data  = resultData;				
				activity.runOnUiThread(mUpdateDatenDetails);
				break;
			default:
				Toast.makeText(activity, activity.getResources().getText(R.string.connection_error), Toast.LENGTH_LONG).show();
				pegelApp.trackEvent("ERROR-Visible", "DataDetail", "Toast", 1);
				break;
			}
		}
	}

	
	// update the image in the view
	class ImageHandler implements PegelDataResultReciever.Receiver
	{
		@Override
		public void onReceiveResult(int resultCode, Bundle resultData) {
			switch (resultCode) {
			case PegelDataProvider.STATUS_FINISHED:
				activity.runOnUiThread(mUpdateImage);
				break;

			default:
				Toast.makeText(activity, activity.getResources().getText(R.string.connection_error), Toast.LENGTH_LONG).show();
				pegelApp.trackEvent("ERROR-Visible", "ShowImage", "Toast", 1);
				break;
			}			
		}
	}

}
