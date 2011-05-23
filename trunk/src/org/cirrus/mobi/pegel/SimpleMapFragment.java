package org.cirrus.mobi.pegel;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class SimpleMapFragment extends Fragment {

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

	public PegelApplication pegelApp;

	private PegelDataProvider pegelDataProvider;
	
	

	public SimpleMapFragment() { //for framework use

	}
	public static SimpleMapFragment getInstance(String pnr)
	{
		SimpleMapFragment mdf = new SimpleMapFragment();
		Bundle args = new Bundle();
		args.putString("pnr", pnr);
		mdf.setArguments(args);
		return mdf;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater,
			ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		
		View moreDetailsView  = inflater.inflate(R.layout.map_view, container, false);
		
		return moreDetailsView;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		this.pegelApp = (PegelApplication) getActivity().getApplication();
		
		getActivity().setProgressBarIndeterminateVisibility(true);
		pdrDataMap = new PegelDataResultReciever(new Handler());
		pdrDataMap.setReceiver(new MapHandler());

		this.pegelDataProvider = PegelDataProvider.getInstance((PegelApplication) getActivity().getApplication());

		
		final ImageView map = (ImageView)  getActivity().findViewById(R.id.map_image);
		final ViewTreeObserver vto = map.getViewTreeObserver();
		// we need to let the layout finish first in order to get the correct size of the image, then request the map
		vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
		    public boolean onPreDraw() {
		        int finalHeight = map.getMeasuredHeight();
		        int finalWidth = map.getMeasuredWidth();
		        int size = Math.min(finalHeight, finalWidth);
		        pegelDataProvider.showData(getArguments().getString("pnr"), null, null, null, pdrDataMap, size );
		        
		        final ViewTreeObserver vto = map.getViewTreeObserver();
		        vto.removeOnPreDrawListener(this);
		        return true;
		    }
		});

	}

	private int getSize() {
		ImageView map = (ImageView) getActivity().findViewById(R.id.map_image);
		int size = Math.min(map.getWidth(), map.getHeight());
		return size;
	}

	protected void updateMapinUi() {
		ImageView img = (ImageView)getActivity().findViewById(R.id.map_image);
		img.setImageDrawable(((PegelApplication) getActivity().getApplication()).getCachedDrawable("map"));
		Animation fadeIn = AnimationUtils.loadAnimation(getActivity(), R.anim.imagealpha);
		img.startAnimation(fadeIn);				
	}
	
	private void updateNoMapinUi() {
		ImageView img = (ImageView) getActivity().findViewById(R.id.map_image);
		TextView tv = (TextView) getActivity().findViewById(R.id.noMap);
		img.setVisibility(View.GONE);		
		Animation fadeIn = AnimationUtils.loadAnimation(getActivity(), R.anim.imagealpha);
		tv.startAnimation(fadeIn);
		tv.setVisibility(View.VISIBLE);
	}
		
	class MapHandler implements PegelDataResultReciever.Receiver
	{
		@Override
		public void onReceiveResult(int resultCode, final Bundle resultData) {
			switch (resultCode) {
			case PegelDataProvider.STATUS_FINISHED:								
				getActivity().runOnUiThread(mUpdateDatenMap);
				break;
			case PegelDataProvider.STATUS_NO_MAP:
				getActivity().runOnUiThread(mUpdateNoMap);
				pegelApp.tracker.trackEvent("Map", "NoMap3", "Sorry", 0);
				break;
			default:
				Toast.makeText(getActivity().getApplicationContext(), getResources().getText(R.string.connection_error), Toast.LENGTH_LONG).show();
				pegelApp.tracker.trackEvent("ERROR-Visible", "ShowMap", "Toast", 0);
				break;
			}
			getActivity().setProgressBarIndeterminateVisibility(false);
		}
	}


}
