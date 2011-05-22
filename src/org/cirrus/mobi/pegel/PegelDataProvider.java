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

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;

public class PegelDataProvider {
	
	protected static final String TAG = "AbstractPegelDetail";

	public static final int STATUS_ERROR = 0x2;
	public static final int STATUS_FINISHED = 0x3;


	private String[] data = null;
	protected String imgurl = null;
	protected Drawable d = null;
	protected String[][] dataDetails = null;

	private String pnr;

	private boolean updateing = false;

	private PegelDataResultReciever pdrPegel;

	private PegelApplication pegelApp;

	private PegelDataResultReciever pdrPegelDetail;
	private PegelDataResultReciever pdrPegelImage;

	private static PegelDataProvider abstractPegelDetail = null; 

	private PegelDataProvider(PegelApplication pa) {
		this.pegelApp = pa;
	}

	public static PegelDataProvider getInstance(PegelApplication pa) {
		if(abstractPegelDetail == null)
			abstractPegelDetail = new PegelDataProvider(pa);
		return abstractPegelDetail;
	}

	public void showData(String pnr, PegelDataResultReciever pdrPegel, PegelDataResultReciever pdrPegelImage, PegelDataResultReciever pdrPegelDetails)
	{
		if(updateing)
			return;
		boolean refresh = false;
		if(this.pnr != null && this.pnr != pnr)
			refresh  = true;
		this.pdrPegel = pdrPegel;
		this.pdrPegelImage = pdrPegelImage;
		this.pdrPegelDetail = pdrPegelDetails;
		this.pnr = pnr;
		this.fetchData(refresh);
	}

	public void refresh(String pnr, PegelDataResultReciever pdrPegel, PegelDataResultReciever pdrPegelImage, PegelDataResultReciever pdrPegelDetails) {
		if(updateing)
			return;
		this.pdrPegel = pdrPegel;
		this.pdrPegelImage = pdrPegelImage;
		this.pdrPegelDetail = pdrPegelDetails;
		this.pnr = pnr;
		this.fetchData(true);
	}


	private void fetchData(final boolean refresh) {

		updateing = true;

		//		

		// Fire off a thread to do some work that we shouldn't do directly in the UI thread
		Thread t = new Thread() {
			public void run() {
				if(refresh || data == null)
				{
					PointStore ps = pegelApp.getPointStore();
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
				} 				
				updateData();
			}
		};
		t.start();

		//fetch image
		// Fire off a thread to do some work that we shouldn't do directly in the UI thread
		Thread t2 = new Thread() {
			public void run() {
				if(refresh || d == null)
				{
					PointStore ps = pegelApp.getPointStore();
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
				}
				updateImage();
			}
		};
		t2.start();

		//fetch details of the measure point
		// Fire off a thread to do some work that we shouldn't do directly in the UI thread
		Thread t3 = new Thread() {
			public void run() {
				if(refresh || dataDetails == null)
				{
					PointStore ps = pegelApp.getPointStore();
					try {
						dataDetails = ps.getMeasurePointDetails(pegelApp.getApplicationContext(), pnr);
					} catch (Exception e) {
						Log.w(TAG, "Error fecthing data from server, retry...", e);					

						try {
							dataDetails = ps.getMeasurePointDetails(pegelApp.getApplicationContext(), pnr);
						} catch (Exception e1) {
							Log.w(TAG, "Error fecthing data from server, giving up...", e);
						}
					}
				}
				updateDetails();
			}
		};
		t3.start();
	}
	protected void updateImage() {
		updateing = false;		
		if(d != null && pdrPegelImage != null)
		{
			if(pdrPegelImage != null)
			{
				pegelApp.setCachedImage(d);
				pdrPegelImage.send(STATUS_FINISHED, Bundle.EMPTY);
			}
		}
		else
		{
			if(pdrPegelImage != null)
				pdrPegelImage.send(STATUS_ERROR, Bundle.EMPTY);
		}
		//		parentActivity.setProgressBarIndeterminateVisibility(false);

	}
	protected void updateData() {

		updateing = false;
		if(data != null && this.pdrPegel != null)
		{
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

			Bundle b = new Bundle();
			b.putString("tendency", tendency);
			b.putFloat("pegel", measure);
			b.putString("time", data[2].replace(' ', '\n'));
			pdrPegel.send(STATUS_FINISHED, b);

			SharedPreferences settings = pegelApp.getSharedPreferences("prefs", Context.MODE_WORLD_WRITEABLE);
			SharedPreferences.Editor edit = settings.edit();
			edit.putString("measure", data[0]);
			edit.commit();
		}
		else //ERROR
		{
			if(pdrPegel != null)
				pdrPegel.send(STATUS_ERROR, Bundle.EMPTY);
		}
	}

	private void updateDetails() {
		updateing = false;
		if(this.dataDetails != null && pdrPegelDetail != null)
		{
			Bundle bundle = new Bundle();
			//extract data and put it into the PegelGrafikView
			for (int i = 0; i < dataDetails.length; i++) {				
				bundle.putStringArray(dataDetails[i][0], dataDetails[i]);
			}
			pdrPegelDetail.send(STATUS_FINISHED, bundle);
		}
		else
		{
			if(pdrPegelDetail != null)
				pdrPegelDetail.send(STATUS_ERROR, Bundle.EMPTY);			
		}
	}

}
