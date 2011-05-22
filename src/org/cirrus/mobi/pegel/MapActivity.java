package org.cirrus.mobi.pegel;


import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

public class MapActivity extends AbstractPegelDetailsActivity {

	private PegelDataResultReciever pdrDataMap;	

	// Create runnable for posting
	final Runnable mUpdateDatenMap = new Runnable() {
		public void run() {
			updateMapinUi();
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getWindow().requestFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		
		setContentView(R.layout.map_view);

		this.pegelDetailHelper = new PegelDetailHelper(this);

		this.pa = (PegelApplication) getApplication();		
		pa.tracker.trackPageView("/PegelDataMapView");

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
		//Display display = getWindowManager().getDefaultDisplay();
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
	
	// This method is called once a menu item is selected
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.m_refresh:
			this.pegelDataProvider.refresh(pnr, null, null, null, pdrDataMap, getSize());
			this.pa.tracker.trackEvent("PegelDataView", "refresh", "refresh", 1);
			return true;
		default:
			return super.onOptionsItemSelected(item);

		}
	}

	
	
	class MapHandler implements PegelDataResultReciever.Receiver
	{
		@Override
		public void onReceiveResult(int resultCode, final Bundle resultData) {
			switch (resultCode) {
			case PegelDataProvider.STATUS_FINISHED:								
				runOnUiThread(mUpdateDatenMap);
				break;
			default:
				Toast.makeText(getApplicationContext(), getResources().getText(R.string.connection_error), Toast.LENGTH_LONG).show();
				break;
			}
			setProgressBarIndeterminateVisibility(false);
		}
	}

}
