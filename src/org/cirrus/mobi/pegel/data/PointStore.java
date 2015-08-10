package org.cirrus.mobi.pegel.data;

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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Map;

import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import org.cirrus.mobi.pegel.PegelApplication;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import rx.Observable;
import rx.functions.Func0;

public class PointStore {

	private static final String VALUE = "value";
	private static final String UNIT = "unit";
	private static final String DESCRIPTION = "description";
	private static final String NAME = "name";
	private static final String IMGURL = "imgurl";
	private static final String ZEIT = "zeit";
	private static final String TENDENZ = "tendenz";
	private static final String MESSUNG = "messung";
	private static final String PEGELNUMMER = "pegelnummer";
	private static final String PEGELNAME = "pegelname";

	private static final String MAPS_URL = "http://maps.google.com/maps/api/staticmap?";

	private static final String TAG = "PointStore";

	private static final String PREFS_NAME = "store";

	private static final long POINT_CACHE_TIME = (2*60*60*1000); //2 hours

	private static final String POINT_STORE_URL = PegelApplication.host+"/pegelmeasurepoints";
	private static final String POINT_DETAILS_URL = PegelApplication.host+"/pegelmeasurepointdetail?pn=";
	private static final String POINT_ALL_DATA_URL = PegelApplication.host+"/pegelmoredata?pn=";
	private static final String POINT_DATA_URL = PegelApplication.host+"/pegeldata?pn=";
	private static final String POINT_DATA_IMAGE_URL = PegelApplication.host+"/pegeldataimage?pn=";

	private static final String IMAGE_URL_SERVER_URL_1 = "http://www.pegelonline.wsv.de/webservices/rest/v1/locations/";
	private static final String IMAGE_URL_SERVER_URL_2 = "/timeseries/W.png";

	private static final String POINT_CACHE_FILE = "point_cache.json";
	private static final String LAST_P_UPDATE = "lpu";

	private static final int DEFAULT_BUFFER = 30720; //30k
	private static final String LON = "lon";
	private static final String LAT = "lat";


	//simple cache in memory
	private Map<String, PegelEntry[]> riverPoints = null;
	Type riverPointType = new TypeToken<Map<String, PegelEntry[]>>() {}.getType();


    /**
	 * Returns all available rivers
	 * 
	 * @param context
	 * @return String[] rivernames
	 * @throws Exception
	 */
	public synchronized String[] getRivers(Context context) throws Exception
	{
		if(this.riverPoints == null)
		{
			String points = getPointData(context);
			Log.v(TAG, "got points from store: "+points);
			Gson gson = new Gson();
			riverPoints = gson.fromJson(points, riverPointType);
		}
		else
			Log.v(TAG, "JSON already parsed...");

		String[]rivers = new String[riverPoints.size()];
		Iterator<String> it  = riverPoints.keySet().iterator();
		int i = 0;
		while (it.hasNext()) {
			rivers[i] = it.next();
			i++;
		}
		return rivers;
	}

	/**
	 * Returns the measure points of a river
	 * 
	 * @param context
	 * @param river - the rivername
	 * @return {@link PegelEntry} = {pegelname, pegelnummer};
	 * @throws Exception
	 */
	public synchronized PegelEntry[] getMeasurePoints(Context context, String river) throws Exception
	{
		if(this.riverPoints == null)
		{
			String points = getPointData(context);
			Log.v(TAG, "got points from store: "+points);
			Gson gson = new Gson();
			riverPoints = gson.fromJson(points, riverPointType);
		}
		else
			Log.v(TAG, "JSON already parsed...");

		return this.riverPoints.get(river);
	}

	/**
	 * Fetches the pointinfo (rivers / points) from the server and caches the plain result in the file system
	 * @param context
	 * @return
	 * @throws Exception
	 */
	private String getPointData(Context context) throws Exception
	{
		// Restore preferences
		SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_WORLD_WRITEABLE);
		long lastUpdate = settings.getLong(LAST_P_UPDATE, 0);

        boolean isConnected = isConnected(context);

		String points = "";
		if( (System.currentTimeMillis() - POINT_CACHE_TIME) > lastUpdate && isConnected)
		{
			Log.v(TAG, "Cache invalid, re-fetch points from server!");
			File cacheFile = new File(context.getCacheDir(), POINT_CACHE_FILE);
			if(cacheFile.exists())
				cacheFile.delete();

			BufferedWriter fileWriter = null;
			BufferedReader in = null;

			try
			{
				fileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(cacheFile)),DEFAULT_BUFFER);

				URL url = new URL(POINT_STORE_URL);
				URLConnection urlConnection = url.openConnection();

				in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), Charset.forName("ISO-8859-1")), DEFAULT_BUFFER);

				String inputLine;
				StringBuilder sb = new StringBuilder();
				while ((inputLine = in.readLine()) != null)
				{
					fileWriter.write(inputLine);
					sb.append(inputLine);
				}				
				fileWriter.flush();

				SharedPreferences.Editor editor = settings.edit();
				editor.putLong(LAST_P_UPDATE, System.currentTimeMillis());
				editor.commit();

				Log.v(TAG, "last update time: "+lastUpdate);

				points = sb.toString();
			}
			finally
			{
				if(in != null)
					try {	in.close(); } catch(Exception ex){ }
				if(fileWriter != null)
					try {	fileWriter.close(); } catch(Exception ex){ }
			}
		}
		else
		{
			BufferedReader in = null;
			try
			{
				Log.v(TAG, "reading points from cache.");
				File cacheFile = new File(context.getCacheDir(), POINT_CACHE_FILE);
				in = new BufferedReader(new InputStreamReader(new FileInputStream(cacheFile)), DEFAULT_BUFFER);

				String inputLine;
				StringBuilder sb = new StringBuilder();
				while ((inputLine = in.readLine()) != null)
					sb.append(inputLine);

				points = sb.toString();
			}
			catch(Exception e)
			{
				Log.e(TAG, "Error reading back cache! "+e );
				//kill cache
				SharedPreferences.Editor editor = settings.edit();
				editor.remove(LAST_P_UPDATE);
				editor.commit();
			}
			finally
			{
				if(in != null)
					try { in.close(); } catch(Exception e) {}
			}
		}
		return points;
	}

    private boolean isConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo aNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if(aNetworkInfo != null && aNetworkInfo.isConnected())
            return true;
        return false;
    }

    /**
	 * Returns the measure details for a point 
	 * 
	 * @param pnr
	 * @return String[3] = { measure, tendency, time };
	 * @throws Exception
	 */
	public MeasureEntry getPointData(String pnr) throws Exception
	{
		MeasureEntry entry = null;

		BufferedReader in = null;
		try 
		{
			URL url = new URL(POINT_DATA_URL+pnr);
			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
			if(urlConnection.getResponseCode() == 200)
			{
				in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), Charset.forName("ISO-8859-1")), DEFAULT_BUFFER);

				String inputLine;
				StringBuilder sb = new StringBuilder();
				while ((inputLine = in.readLine()) != null)
				{			
					sb.append(inputLine);
				}

				Gson gson = new Gson();
				entry = gson.fromJson(sb.toString(), MeasureEntry.class);
				entry.status = MeasureEntry.STATUS_OK;
			}
			else if(urlConnection.getResponseCode() == 404)
			{
				//this pegel number is no longer valid, we need to invalidate our cache an
				//force a new choice :(
				entry = new MeasureEntry(pnr, null, null, null, null);
				entry.status = MeasureEntry.STATUS_NOT_FOUND;
			}
		}
		finally
		{
			if(in != null)
				try { in.close(); } catch(Exception e) {}
		}

		return entry;
	}


	/**
	 * Return the url where to fetch the image for the "ganglinie" from
	 * 
	 * @param pegelNummer
	 * @return String containing the one-time URL for the image
	 * @throws Exception
	 */
	public String getURLData(String pegelNummer) throws Exception {

		BufferedReader in = null;
		String imageurl = "";
		try 
		{
			URL url = new URL(POINT_DATA_IMAGE_URL+pegelNummer);
			URLConnection urlConnection = url.openConnection();
			in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), Charset.forName("ISO-8859-1")), DEFAULT_BUFFER);

			String inputLine;
			StringBuilder sb = new StringBuilder();
			while ((inputLine = in.readLine()) != null)
			{			
				sb.append(inputLine);
			}
			Gson gson = new Gson();
			ImageEntry entry = gson.fromJson(sb.toString(), ImageEntry.class);
			imageurl = entry.getImgurl();
		}
		finally
		{
			if(in != null)
				try { in.close(); } catch(Exception e) {}
		}
		return imageurl;
	}

	/**
	 * New implementation to get a image-URL for the so called "Ganglinie" 
	 * Uses the new (undocumented) server-interface, no app-engine call needed anymore
	 * @param pegelNummer
	 * @return
	 * @throws Exception
	 */

	public String getImageURL(String pegelNummer) {
		StringBuilder sb = new StringBuilder(IMAGE_URL_SERVER_URL_1);
		sb.append(pegelNummer);
		sb.append(IMAGE_URL_SERVER_URL_2);

		return sb.toString();
	}

	/**
	 * Returns details for a specific measure station like HSW etc.
	 * @param context	the app context
	 * @param pegelNummer	the pegelnummer of the station you want
	 * @return	String[][4] = { name, description, unit, value };
	 * @throws Exception
	 */
	public MeasureStationDetails[] getMeasurePointDetails(Context context, String pegelNummer) throws Exception {
		MeasureStationDetails[] details = null;

		//check the cache first
		SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_WORLD_WRITEABLE);
		long lastUpdate = settings.getLong(LAST_P_UPDATE+pegelNummer, 0);

		String serverResp = null;

		if( (System.currentTimeMillis() - POINT_CACHE_TIME) > lastUpdate )
		{
			Log.v(TAG, "Cache invalid, re-fetch point-details from server!");
			File cacheFile = new File(context.getCacheDir(), pegelNummer+"_"+POINT_CACHE_FILE);
			if(cacheFile.exists())
				cacheFile.delete();

			BufferedReader in = null;
			BufferedWriter fileWriter = null;

			try
			{
				fileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(cacheFile)),DEFAULT_BUFFER);

				URL url = new URL(POINT_DETAILS_URL+pegelNummer);
				URLConnection urlConnection = url.openConnection();

				in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), Charset.forName("ISO-8859-1")), DEFAULT_BUFFER);

				String inputLine;
				StringBuilder sb = new StringBuilder();
				while ((inputLine = in.readLine()) != null)
				{
					fileWriter.write(inputLine);
					sb.append(inputLine);
				}				
				fileWriter.flush();

				SharedPreferences.Editor editor = settings.edit();
				editor.putLong(LAST_P_UPDATE+pegelNummer, System.currentTimeMillis());
				editor.commit();

				Log.v(TAG, "last update time: "+lastUpdate);

				serverResp = sb.toString();

			}
			finally
			{
				if(in != null)
				{
					try { in.close(); } catch(Exception e) {}
				}
				if(fileWriter != null)
					try {	fileWriter.close(); } catch(Exception ex){ }
			}
		}
		else
		{
			//read from cache
			BufferedReader in = null;
			try
			{
				Log.v(TAG, "reading point-details from cache.");
				File cacheFile = new File(context.getCacheDir(), pegelNummer+"_"+POINT_CACHE_FILE);
				in = new BufferedReader(new InputStreamReader(new FileInputStream(cacheFile)), DEFAULT_BUFFER);

				String inputLine;
				StringBuilder sb = new StringBuilder();
				while ((inputLine = in.readLine()) != null)
					sb.append(inputLine);

				serverResp = sb.toString();
			}
			catch(Exception e)
			{
				Log.e(TAG, "Error reading back cache! "+e );
				//kill cache entry
				SharedPreferences.Editor editor = settings.edit();
				editor.remove(LAST_P_UPDATE+pegelNummer);
				editor.commit();
			}
			finally
			{
				if(in != null)
					try { in.close(); } catch(Exception e) {}
			}
		}
		//extract data
		Gson gson = new Gson();
		details = gson.fromJson(serverResp, MeasureStationDetails[].class);


		return details;
	}


	public MeasurePointDataDetails[] getMeasurePointDataDetails(
			Context applicationContext, String pnr) throws Exception {

		MeasurePointDataDetails[] mpdd = null;
		BufferedReader in = null;
		try {
			URL url = new URL(POINT_ALL_DATA_URL+pnr);
			URLConnection urlConnection = url.openConnection();

			in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), Charset.forName("ISO-8859-1")), DEFAULT_BUFFER);

			String inputLine;
			StringBuilder sb = new StringBuilder();
			while ((inputLine = in.readLine()) != null)
			{			
				sb.append(inputLine);
			}

			Gson gson = new Gson();
			mpdd = gson.fromJson(sb.toString(), MeasurePointDataDetails[].class);

		} 
		finally
		{
			if(in != null)
				try { in.close(); } catch(Exception e) {}
		}

		return mpdd;
	}
	
	public void clearPointCache(Context context) {
		File cacheFile = new File(context.getCacheDir(), POINT_CACHE_FILE);
		if(cacheFile.exists())
			cacheFile.delete();
		SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_WORLD_WRITEABLE);
		SharedPreferences.Editor editor = settings.edit();
		editor.remove(LAST_P_UPDATE);
		editor.commit();
		
	}

	public Observable<MeasureEntry> getMeasureEntry(final String pnr) {
		return Observable.defer(new Func0<Observable<MeasureEntry>>() {
			@Override
			public Observable<MeasureEntry> call() {
				try {

					MeasureEntry measureEntry = getPointData(pnr);
					if(measureEntry == null)
						return Observable.error(new Exception("Point Not found!"));
					return Observable.just(measureEntry);
				} catch (Exception e) {
					Log.e(TAG,"Error",e);
					return Observable.error(e);
				}
			}
		});
	}

	public Observable<String> getMeasureLineImageURL(final String pnr) {
		return Observable.defer(new Func0<Observable<String>>() {
			@Override
			public Observable<String> call() {
				final String imgurl = getImageURL(pnr);
				Log.v(TAG, "Got image URL: " +imgurl);
				return Observable.just(imgurl);
			}
		});

	}
	public Observable<String> getMapURL(final String pnr, final int mapsize) {
		return Observable.defer(new Func0<Observable<String>>() {
			@Override
			public Observable<String> call() {
				MeasureEntry measureEntry = null;
				try {
					measureEntry = getPointData(pnr);
				} catch (Exception e) {
					Log.w(TAG, "Error fecthing data from server, retry...", e);
					try {
						measureEntry = getPointData(pnr);
					} catch (Exception e1) {
						Log.w(TAG, "Error fecthing data from server, giving up...", e);
					}
				}
				if(measureEntry != null && measureEntry.getLat() != null) {
					StringBuilder mapsUrl = new StringBuilder(MAPS_URL);
					mapsUrl.append("center=").append(measureEntry.getLat()).append(',').append(measureEntry.getLon());
					mapsUrl.append("&zoom=12&size=").append(mapsize).append("x").append(mapsize).append("&sensor=false");
					mapsUrl.append("&markers=color:blue|label:M|").append(measureEntry.getLat()).append(',').append(measureEntry.getLon());
					Log.v(TAG, "will request map URL: "+mapsUrl.toString());

					return Observable.just(mapsUrl.toString());
				}

				return Observable.error(new Exception("could not find point or map"));
			}
		});

	}

	public Observable<MeasureStationDetails[]> getMeasureStationDetails(final Context context, final String pnr) {
		return Observable.defer(new Func0<Observable<MeasureStationDetails[]>>() {
			@Override
			public Observable<MeasureStationDetails[]> call() {
				try {
					MeasureStationDetails[] measureStationDetailses = getMeasurePointDetails(context, pnr);
					if(measureStationDetailses != null)
						return Observable.just(measureStationDetailses);
					else
						return Observable.error(new Exception("Point nor found"));
				}
				catch (Exception e) {
					return Observable.error(e);
				}
			}
		});
	}

	public Observable<MeasurePointDataDetails[]> getMeasurePointDetailsObserver(final Context context, final String pnr) {
		return Observable.defer(new Func0<Observable<MeasurePointDataDetails[]>>() {
			@Override
			public Observable<MeasurePointDataDetails[]> call() {
				try {
					MeasurePointDataDetails[] measureStationDetailses = getMeasurePointDataDetails(context, pnr);
					if(measureStationDetailses != null)
						return Observable.just(measureStationDetailses);
					else
						return Observable.error(new Exception("Point nor found"));
				}
				catch (Exception e) {
					return Observable.error(e);
				}
			}
		});
	}

}
