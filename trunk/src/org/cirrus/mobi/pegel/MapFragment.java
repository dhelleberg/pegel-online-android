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

import java.util.ArrayList;
import java.util.List;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.OverlayItem;

import android.app.Fragment;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;




public class MapFragment extends Fragment {
	private MapView map = null;
	private MyLocationOverlay me = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return(new FrameLayout(getActivity()));
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		map=new MapView(getActivity(), "0_TJoFsNZL51hKZkjncLKaFLfmZSsj-cJzAw0wA");
		map.setClickable(true);

		map.getController().setCenter(getPoint(51.16333333, 10.44777778));
		map.getController().setZoom(7);
		map.setBuiltInZoomControls(true);

		Drawable marker=getResources().getDrawable(R.drawable.refresh);

		marker.setBounds(0, 0, marker.getIntrinsicWidth(),marker.getIntrinsicHeight());

		map.getOverlays().add(new SitesOverlay(marker));

		me=new MyLocationOverlay(getActivity(), map);
		map.getOverlays().add(me);

		((ViewGroup)getView()).addView(map);
	}

	@Override
	public void onResume() {
		super.onResume();

		me.enableCompass();
	}		

	@Override
	public void onPause() {
		super.onPause();

		me.disableCompass();
	}		

	private GeoPoint getPoint(double lat, double lon) {
		return(new GeoPoint((int)(lat*1000000.0),
				(int)(lon*1000000.0)));
	}

	private class SitesOverlay extends ItemizedOverlay<OverlayItem> {
		private List<OverlayItem> items=new ArrayList<OverlayItem>();
		private Drawable marker=null;

		public SitesOverlay(Drawable marker) {
			super(marker);
			this.marker=marker;

			boundCenterBottom(marker);

			items.add(new OverlayItem(getPoint(40.748963847316034,
					-73.96807193756104),
					"UN", "United Nations"));
			items.add(new OverlayItem(getPoint(40.76866299974387,
					-73.98268461227417),
					"Lincoln Center",
			"Home of Jazz at Lincoln Center"));
			items.add(new OverlayItem(getPoint(40.765136435316755,
					-73.97989511489868),
					"Carnegie Hall",
					"Where you go with practice, practice, practice"));
			items.add(new OverlayItem(getPoint(40.70686417491799,
					-74.01572942733765),
					"The Downtown Club",
			"Original home of the Heisman Trophy"));

			populate();
		}

		@Override
		protected OverlayItem createItem(int i) {
			return(items.get(i));
		}

		@Override
		protected boolean onTap(int i) {
			Toast.makeText(getActivity(),
					items.get(i).getSnippet(),
					Toast.LENGTH_SHORT).show();

			return(true);
		}

		@Override
		public int size() {
			return(items.size());
		}
	}
}