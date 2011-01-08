package org.cirrus.mobi.pegel.data;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class PointStore {

	private static final String TAG = "PointStore";

	private static final String PREFS_NAME = "store";
	private static final long POINT_CACHE_TIME = (24*60*60*1000); //one day
	private static final String POINT_STORE_URL = "http://pegel-online.appspot.com/pegelmeasurepoints";
	private static final String POINT_DATA_URL = "http://pegel-online.appspot.com/pegeldata?pn=";
	
	private static final String POINT_CACHE_FILE = "point_cache.json";
	private static final String LAST_P_UPDATE = "lpu";

	private static final String POINT_DATA_IMAGE_URL = "http://pegel-online.appspot.com/pegeldataimage?pn=";

	
	private JSONObject jo_points = null;

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
			m_points[i][0] = mp.getString("pegelname");
			m_points[i][1] = mp.getString("pegelnummer");
		}
		
		return m_points;
	}

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
			BufferedWriter fileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(cacheFile)));

			URL url = new URL(POINT_STORE_URL);
			URLConnection urlConnection = url.openConnection();

			BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), Charset.forName("ISO-8859-1")));

			String inputLine;
			StringBuilder sb = new StringBuilder();
			while ((inputLine = in.readLine()) != null)
			{
				fileWriter.write(inputLine);
				sb.append(inputLine);
			}
			in.close();
			fileWriter.flush();
			fileWriter.close();

			SharedPreferences.Editor editor = settings.edit();
			editor.putLong(LAST_P_UPDATE, System.currentTimeMillis());
			editor.commit();

			Log.v(TAG, "last update time: "+lastUpdate);


			points = sb.toString();
		}
		else
		{
			Log.v(TAG, "reading points from cache.");
			File cacheFile = new File(context.getCacheDir(), POINT_CACHE_FILE);
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(cacheFile)));

			String inputLine;
			StringBuilder sb = new StringBuilder();
			while ((inputLine = in.readLine()) != null)
				sb.append(inputLine);

			in.close();
			points = sb.toString();
		}
		return points;
	}
	
	public String[] getPointData(String pnr) throws Exception
	{
		String[] results = new String[3];
		
		URL url = new URL(POINT_DATA_URL+pnr);
		URLConnection urlConnection = url.openConnection();
		BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), Charset.forName("ISO-8859-1")));

		String inputLine;
		StringBuilder sb = new StringBuilder();
		while ((inputLine = in.readLine()) != null)
		{			
			sb.append(inputLine);
		}
		in.close();
		
		JSONObject data = new JSONObject(sb.toString());
		results[0] = data.getString("messung");
		results[1] = data.getString("tendenz");
		results[2] = data.getString("zeit");
		
		return results;
	}

	public String getURLData(String pegelNummer) throws Exception {
		URL url = new URL(POINT_DATA_IMAGE_URL+pegelNummer);
		URLConnection urlConnection = url.openConnection();
		BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), Charset.forName("ISO-8859-1")));

		String inputLine;
		StringBuilder sb = new StringBuilder();
		while ((inputLine = in.readLine()) != null)
		{			
			sb.append(inputLine);
		}
		in.close();
		
		JSONObject data = new JSONObject(sb.toString());
		String imageurl = data.getString("imgurl");
		
		return imageurl;
	}
}
