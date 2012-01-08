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


import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MapActivity extends AbstractPegelDetailsActivity {

	private PegelDataResultReciever pdrDataMap;	

	// Create runnable for posting
	final Runnable mUpdateDatenMap = new Runnable() {
		public void run() {
			updateMapinUi();
		}
	};

	// Create runnable for posting
	final Runnable mUpdateNoMap = new Runnable() {
		public void run() {
			updateNoMapinUi();
		}		
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getWindow().requestFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

		setContentView(R.layout.map_view);

		this.pegelDetailHelper = new PegelDetailHelper(this);

		this.pegelApp = (PegelApplication) getApplication();		
		pegelApp.trackPageView("/PegelDataMapView");

		this.pnr = getIntent().getStringExtra("pnr");

		pdrDataMap = new PegelDataResultReciever(new Handler());
		pdrDataMap.setReceiver(new MapHandler());

		setProgressBarIndeterminateVisibility(true);
		this.pegelDataProvider = PegelDataProvider.getInstance((PegelApplication) getApplication());

	}


	@Override
	protected void onStart() {	
		super.onStart();

		final ImageView map = (ImageView) findViewById(R.id.map_image);
		final ViewTreeObserver vto = map.getViewTreeObserver();
		// we need to let the layout finish first in order to get the correct size of the image, then request the map
		vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
			public boolean onPreDraw() {
				int finalHeight = map.getMeasuredHeight();
				int finalWidth = map.getMeasuredWidth();
				int size = Math.min(finalHeight, finalWidth);
				pegelDataProvider.showData(pnr, null, null, null, pdrDataMap, size );

				final ViewTreeObserver vto = map.getViewTreeObserver();
				vto.removeOnPreDrawListener(this);
				return true;
			}
		});


	}

	private int getSize() {
		ImageView map = (ImageView) findViewById(R.id.map_image);
		int size = Math.min(map.getWidth(), map.getHeight());
		return size;
	}

	protected void updateMapinUi() {
		ImageView img = (ImageView) findViewById(R.id.map_image);
		img.setImageDrawable(((PegelApplication) getApplication()).getCachedDrawable("map"));
		Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.imagealpha);
		img.startAnimation(fadeIn);				
	}

	private void updateNoMapinUi() {
		ImageView img = (ImageView) findViewById(R.id.map_image);
		TextView tv = (TextView) findViewById(R.id.noMap);
		img.setVisibility(View.GONE);		
		Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.imagealpha);
		tv.startAnimation(fadeIn);
		tv.setVisibility(View.VISIBLE);
	}



	@Override	
	public void refreshFromOptionsMenu()
	{
		setProgressBarIndeterminateVisibility(true);
		this.pegelDataProvider.refresh(pnr, null, null, null, pdrDataMap, getSize());
		this.pegelApp.trackEvent("MapActivity", "refresh", "refresh", 1);
	}


	class MapHandler implements PegelDataResultReciever.Receiver
	{
		@Override
		public void onReceiveResult(int resultCode, final Bundle resultData) {
			switch (resultCode) {
			case PegelDataProvider.STATUS_FINISHED:								
				runOnUiThread(mUpdateDatenMap);
				break;
			case PegelDataProvider.STATUS_NO_MAP:
				runOnUiThread(mUpdateNoMap);
				pegelApp.trackEvent("Map", "NoMap", "Sorry", 1);
				break;
			default:
				Toast.makeText(getApplicationContext(), getResources().getText(R.string.connection_error), Toast.LENGTH_LONG).show();
				pegelApp.trackEvent("ERROR-Visible", "ShowMap", "Toast", 1);
				break;
			}
			setProgressBarIndeterminateVisibility(false);
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(getParent() != null)
			return getParent().onKeyDown(keyCode, event);
		else
			return super.onKeyDown(keyCode, event);
	}

}
