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
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.Iterator;

import org.cirrus.mobi.pegel.PegelApplication;
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

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

	private static final String TAG = "PointStore";

	private static final String PREFS_NAME = "store";

	private static final long POINT_CACHE_TIME = (96*60*60*1000); //four days
	
	private static final String POINT_STORE_URL = PegelApplication.host+"/pegelmeasurepoints";
	private static final String POINT_DETAILS_URL = PegelApplication.host+"/pegelmeasurepointdetail?pn=";
	private static final String POINT_DATA_URL = PegelApplication.host+"/pegeldata?pn=";
	private static final String POINT_DATA_IMAGE_URL = PegelApplication.host+"/pegeldataimage?pn=";

	private static final String POINT_CACHE_FILE = "point_cache.json";
	private static final String LAST_P_UPDATE = "lpu";
	
	private static final int DEFAULT_BUFFER = 131072; //128k
	private static final String LON = "lon";
	private static final String LAT = "lat";

	//simple cache in memory
	private JSONObject jo_points = null;

	/**
	 * Returns all available rivers
	 * 
	 * @param context
	 * @return String[] rivernames
	 * @throws Exception
	 */
	public synchronized String[] getRivers(Context context) throws Exception
	{
		if(this.jo_points == null)
		{
			String points = getPointData(context);
			Log.v(TAG, "got points from store: "+points);
			this.jo_points= new JSONObject(points);
		}
		else
			Log.v(TAG, "JSON already parsed...");

		String[]rivers = new String[jo_points.length()];
		Iterator<String> it  = jo_points.keys();
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
	 * @return String[][2] = {pegelname, pegelnummer};
	 * @throws Exception
	 */
	public synchronized String[][] getMeasurePoints(Context context, String river) throws Exception
	{
		if(this.jo_points == null)
		{
			String points = getPointData(context);
			Log.v(TAG, "got points from store: "+points);
			this.jo_points= new JSONObject(points);
		}
		else
			Log.v(TAG, "JSON already parsed...");

		JSONArray measurepoints = (JSONArray) jo_points.get(river);
		String[][]m_points = new String[measurepoints.length()][2];
		for (int i = 0; i < measurepoints.length(); i++) {
			JSONObject mp = measurepoints.getJSONObject(i);
			m_points[i][0] = mp.getString(PEGELNAME);
			m_points[i][1] = mp.getString(PEGELNUMMER);
		}

		return m_points;
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

		String points = "";
		if( (System.currentTimeMillis() - POINT_CACHE_TIME) > lastUpdate )
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

	/**
	 * Returns the measure details for a point 
	 * 
	 * @param pnr
	 * @return String[3] = { measure, tendency, time };
	 * @throws Exception
	 */
	public String[] getPointData(String pnr) throws Exception
	{
		String[] results = new String[5];

		BufferedReader in = null;
		try 
		{
			URL url = new URL(POINT_DATA_URL+pnr);
			URLConnection urlConnection = url.openConnection();
			in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), Charset.forName("ISO-8859-1")), DEFAULT_BUFFER);

			String inputLine;
			StringBuilder sb = new StringBuilder();
			while ((inputLine = in.readLine()) != null)
			{			
				sb.append(inputLine);
			}

			JSONObject data = new JSONObject(sb.toString());
			results[0] = data.getString(MESSUNG);
			results[1] = data.getString(TENDENZ);
			results[2] = data.getString(ZEIT);
			if(data.getString(LAT) != null)
				results[3] = data.getString(LAT);
			if(data.getString(LON) != null)
				results[4] = data.getString(LON);
		}
		finally
		{
			if(in != null)
				try { in.close(); } catch(Exception e) {}
		}

		return results;
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

			JSONObject data = new JSONObject(sb.toString());
			imageurl = data.getString(IMGURL);
		}
		finally
		{
			if(in != null)
				try { in.close(); } catch(Exception e) {}
		}
		return imageurl;
	}

	/**
	 * Returns details for a specific measure station like HSW etc.
	 * @param context	the app context
	 * @param pegelNummer	the pegelnummer of the station you want
	 * @return	String[][4] = { name, description, unit, value };
	 * @throws Exception
	 */
	public String[][] getMeasurePointDetails(Context context, String pegelNummer) throws Exception {
		String[][] details = null;

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
		JSONArray ja = new JSONArray(serverResp);
		details = new String[ja.length()][4];
		for (int i = 0; i < ja.length(); i++) {
			JSONObject jo = (JSONObject) ja.get(i);
			details[i][0] = jo.getString(NAME);
			details[i][1] = jo.getString(DESCRIPTION);
			details[i][2] = jo.getString(UNIT);
			details[i][3] = jo.getString(VALUE);
		}
		
		return details;
	}
}
