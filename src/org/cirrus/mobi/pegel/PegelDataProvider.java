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

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;

import org.cirrus.mobi.pegel.data.MeasureEntry;
import org.cirrus.mobi.pegel.data.MeasurePointDataDetails;
import org.cirrus.mobi.pegel.data.MeasureStationDetails;
import org.cirrus.mobi.pegel.data.PointStore;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class PegelDataProvider {

	protected static final String TAG = "PegelDataProvider";

	public static final int STATUS_ERROR = 0x2;
	public static final int STATUS_FINISHED = 0x3;
	public static final int STATUS_NO_MAP = 0x4;
	public static final int STATUS_NOTFOUND = 0x5;
	
	protected static final String MAPS_URL = "http://maps.google.com/maps/api/staticmap?";

	


	private MeasureEntry data = null;
	protected String imgurl = null;

	protected MeasureStationDetails[] dataDetails = null;
	protected MeasurePointDataDetails[] realDataDetails = null;

	private String pnr;

	private boolean updateing = false;

	private PegelDataResultReciever pdrPegel;

	private PegelApplication pegelApp;

	private PegelDataResultReciever pdrPegelDetail;
	private PegelDataResultReciever pdrPegelImage;
	private PegelDataResultReciever pdrPegelMap;
	private PegelDataResultReciever pdrPegelDaten;

	
	private int mapsize;


	private static PegelDataProvider abstractPegelDetail = null; 

	private PegelDataProvider(PegelApplication pa) {
		this.pegelApp = pa;
	}

	public static PegelDataProvider getInstance(PegelApplication pa) {
		if(abstractPegelDetail == null)
			abstractPegelDetail = new PegelDataProvider(pa);
		return abstractPegelDetail;
	}

	public void showData(String pnr, 
			PegelDataResultReciever pdrPegel,
			PegelDataResultReciever pdrPegelImage, 
			PegelDataResultReciever pdrPegelDetails,
			PegelDataResultReciever pdrPegelMap,
			PegelDataResultReciever pdrPegelDaten,
			int mapsize)
	{
		if(updateing)
			return;
		boolean refresh = false;
		if(this.pnr != null && this.pnr != pnr)
			refresh  = true;
		this.pdrPegel = pdrPegel;
		this.pdrPegelImage = pdrPegelImage;
		this.pdrPegelDetail = pdrPegelDetails;
		this.pdrPegelMap = pdrPegelMap;
		this.pdrPegelDaten = pdrPegelDaten;
		this.mapsize = mapsize;
		this.pnr = pnr;
		this.fetchData(refresh);
	}

	public void refresh(String pnr, PegelDataResultReciever pdrPegel, 
			PegelDataResultReciever pdrPegelImage, 
			PegelDataResultReciever pdrPegelDetails,
			PegelDataResultReciever pdrPegelMap,
			PegelDataResultReciever pdrPegelDaten,
			int mapsize) {
		if(updateing)
			return;
		this.pdrPegel = pdrPegel;
		this.pdrPegelImage = pdrPegelImage;
		this.pdrPegelDetail = pdrPegelDetails;
		this.pdrPegelMap = pdrPegelMap;
		this.pdrPegelDaten = pdrPegelDaten;
		this.mapsize = mapsize;
		this.pnr = pnr;
		this.fetchData(true);
	}


	private void fetchData(final boolean refresh) {

		updateing = true;

		// Fire off a thread to do some work that we shouldn't do directly in the UI thread
		Thread t = new Thread() {
			public void run() {
				if( (refresh || data == null) && pdrPegel != null)
				{
					PointStore ps = pegelApp.getPointStore();
					try {
						data = ps.getPointData(pnr);
					} catch (Exception e) {
						Log.w(TAG, "Error fecthing data from server, retry...", e);					
						pegelApp.trackEvent("ERROR", "getPointData-hidden", e.getMessage(), 1);
						try {
							data = ps.getPointData(pnr);
						} catch (Exception e1) {
							Log.w(TAG, "Error fecthing data from server, giving up...", e);
							pegelApp.trackEvent("ERROR", "getPointData-hidden", e.getMessage(), 1);
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
				if((refresh || pegelApp.getCachedDrawable("pegel") == null) && pdrPegelImage != null )
				{
					/*PointStore ps = pegelApp.getPointStore();
					try {
						imgurl = ps.getURLData(pnr);
					} catch (Exception e) {
						Log.w(TAG, "Error fecthing data from server, retry...", e);
						pegelApp.trackEvent("ERROR", "getURLData-hidden", e.getMessage(), 1);
						try {
							imgurl = ps.getURLData(pnr);
						} catch (Exception e1) {
							Log.w(TAG, "Error fecthing data from server, giving up...", e);
							pegelApp.trackEvent("ERROR", "getURLData-hidden", e.getMessage(), 1);
						}
					} */
					imgurl = pegelApp.getPointStore().getImageURL(pnr);
					Log.v(TAG, "Got image URL: " +imgurl);
					//fecht image 
					URL imgu;
					try {
						imgu = new URL(imgurl);
						InputStream is = (InputStream) imgu.openConnection().getInputStream();
						pegelApp.setCachedImage("pegel", Drawable.createFromStream(is, "src"));					

					} catch (Exception e) {
						Log.w(TAG, "Error fecthing image from server, giving up...", e);
						pegelApp.trackEvent("ERROR", "fetchImage-hidden", e.getMessage(), 1);
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
				if((refresh || dataDetails == null) && pdrPegelDetail != null )
				{
					PointStore ps = pegelApp.getPointStore();
					try {
						dataDetails = ps.getMeasurePointDetails(pegelApp.getApplicationContext(), pnr);
					} catch (Exception e) {
						Log.w(TAG, "Error fecthing data from server, retry...", e);					
						pegelApp.trackEvent("ERROR", "getMeasurePointDetails-hidden", e.getMessage(), 1);
						try {
							dataDetails = ps.getMeasurePointDetails(pegelApp.getApplicationContext(), pnr);
						} catch (Exception e1) {
							Log.w(TAG, "Error fecthing data from server, giving up...", e);
							pegelApp.trackEvent("ERROR", "getMeasurePointDetails-hidden", e.getMessage(), 1);
						}
					}
				}
				updateDetails();
			}
		};
		t3.start();
		
		//fetch details of the measure point
		// Fire off a thread to do some work that we shouldn't do directly in the UI thread
		Thread t5 = new Thread() {
			public void run() {
				if((refresh || realDataDetails == null) && pdrPegelDaten != null )
				{
					PointStore ps = pegelApp.getPointStore();
					try {
						realDataDetails = ps.getMeasurePointDataDetails(pegelApp.getApplicationContext(), pnr);
					} catch (Exception e) {
						Log.w(TAG, "Error fecthing data from server, retry...", e);					
						pegelApp.trackEvent("ERROR", "getMeasurePointDataDetails-hidden", e.getMessage(), 1);
						try {
							realDataDetails = ps.getMeasurePointDataDetails(pegelApp.getApplicationContext(), pnr);
						} catch (Exception e1) {
							Log.w(TAG, "Error fecthing data from server, giving up...", e);
							pegelApp.trackEvent("ERROR", "getMeasurePointDataDetails-hidden", e.getMessage(), 1);
						}
					}
				}
				updateDataDetails();
			}
		};
		t5.start();

		//fetch details of the measure point
		// Fire off a thread to do some work that we shouldn't do directly in the UI thread
		Thread t4 = new Thread() {


			public void run() {
				if((refresh || pegelApp.getCachedDrawable("map")== null) && pdrPegelMap != null )
				{
					MeasureEntry pdata = null;
					PointStore ps = pegelApp.getPointStore();
					try {
						pdata = ps.getPointData(pnr);
					} catch (Exception e) {
						Log.w(TAG, "Error fecthing data from server, retry...", e);					
						pegelApp.trackEvent("ERROR", "getPointData-hidden", e.getMessage(), 1);
						try {
							pdata = ps.getPointData(pnr);
						} catch (Exception e1) {
							Log.w(TAG, "Error fecthing data from server, giving up...", e);
							pegelApp.trackEvent("ERROR", "getPointData-hidden", e.getMessage(), 1);
						}
					}
					if(pdata != null && pdata.getLat() != null)
					{

						StringBuilder mapsUrl = new StringBuilder(MAPS_URL);
						mapsUrl.append("center=").append(pdata.getLat()).append(',').append(pdata.getLon());
						mapsUrl.append("&zoom=12&size=").append(mapsize).append("x").append(mapsize).append("&sensor=false");
						mapsUrl.append("&markers=color:blue|label:M|").append(pdata.getLat()).append(',').append(pdata.getLon());
						Log.v(TAG, "will request map URL: "+mapsUrl.toString());

						URL imgu;
						InputStream is = null;
						try {
							imgu = new URL(mapsUrl.toString());
							is = (InputStream) imgu.openConnection().getInputStream();
							pegelApp.setCachedImage("map",Drawable.createFromStream(is, "src"));	
						} catch (Exception e1) {
							Log.w(TAG, "Error fetching maps-URL",e1);
							pegelApp.trackEvent("ERROR", "fetchMap-hidden", e1.getMessage(), 1);
						}
						finally
						{
							if(is != null)
								try {    is.close();		} catch (IOException e) {	}
						}
					}
					else
						pegelApp.setCachedImage("map", null);
				}
				updateMap();
			}
		};
		t4.start();

	}

	protected void updateMap() {
		updateing = false;
		if(pdrPegelMap != null)
		{
			if(pegelApp.getCachedDrawable("map") != null)
				pdrPegelMap.send(STATUS_FINISHED, Bundle.EMPTY);
			else
				pdrPegelMap.send(STATUS_NO_MAP, Bundle.EMPTY);
		}
	}

	protected void updateImage() {
		updateing = false;		
		if(pegelApp.getCachedDrawable("pegel") != null && pdrPegelImage != null)
		{
			if(pdrPegelImage != null)
			{
				pdrPegelImage.send(STATUS_FINISHED, Bundle.EMPTY);
			}
		}
		else
		{
			if(pdrPegelImage != null)
				pdrPegelImage.send(STATUS_ERROR, Bundle.EMPTY);
		}

	}
	protected void updateData() {

		updateing = false;
		if(data != null && this.pdrPegel != null && data.getStatus() == MeasureEntry.STATUS_OK)
		{
			float measure = Float.parseFloat(data.getMessung());
			String tendency = "";
			int t = Integer.parseInt(data.getTendenz());
			switch (t) {
			case 0:
				tendency = pegelApp.getResources().getText(R.string.tendency_constant).toString();
				break;
			case 1:
				tendency = pegelApp.getResources().getText(R.string.tendency_up).toString();
				break;
			case -1:
				tendency = pegelApp.getResources().getText(R.string.tendency_down).toString();
				break;

			default:
				tendency = pegelApp.getResources().getText(R.string.tendency_unknown).toString();
				break;
			}

			Bundle b = new Bundle();
			b.putString("tendency", tendency);
			b.putFloat("pegel", measure);
			b.putString("time", data.getZeit().replace(' ', '\n'));
			pdrPegel.send(STATUS_FINISHED, b);

			SharedPreferences settings = pegelApp.getSharedPreferences("prefs", Context.MODE_PRIVATE);
			SharedPreferences.Editor edit = settings.edit();
			edit.putString("measure", data.getMessung());
			edit.commit();
		}
		else //ERROR
		{
			if(data != null && data.getStatus() == MeasureEntry.STATUS_NOT_FOUND && pdrPegel != null)
				pdrPegel.send(STATUS_NOTFOUND, Bundle.EMPTY);
			else if(pdrPegel != null)
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
				bundle.putStringArray(dataDetails[i].getName(), dataDetails[i].toStringArray());
			}
			pdrPegelDetail.send(STATUS_FINISHED, bundle);
		}
		else
		{
			if(pdrPegelDetail != null)
				pdrPegelDetail.send(STATUS_ERROR, Bundle.EMPTY);			
		}
	}
	
	private void updateDataDetails() {
		updateing = false;
		if(this.realDataDetails != null && pdrPegelDaten != null)
		{
			Bundle bundle = new Bundle();
			//extract data and put it into the PegelGrafikView
			for (int i = 0; i < realDataDetails.length; i++) {				
				bundle.putStringArray(realDataDetails[i].dataType, realDataDetails[i].toStringArray());
			}
			pdrPegelDaten.send(STATUS_FINISHED, bundle);
		}
		else
		{
			if(pdrPegelDaten != null)
				pdrPegelDaten.send(STATUS_ERROR, Bundle.EMPTY);			
		}
	}

}
