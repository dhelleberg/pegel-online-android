package org.cirrus.mobi.pegel;

import java.util.Set;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

public class DataDetailsActivity extends AbstractPegelDetailsActivity {

	// Create runnable for posting
	final Runnable mUpdateDatenDetails = new Runnable() {
		public void run() {
			updateDataDetailInUi();
		}
	};
	private Bundle data = null;

	private PegelDataResultReciever pdrDataDetails;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getWindow().requestFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

		setContentView(R.layout.data_details);

		this.pegelDetailHelper = new PegelDetailHelper(this);

		this.pegelApp = (PegelApplication) getApplication();		
		pegelApp.trackPageView("/PegelRealDataDetailsView");

		this.pnr = getIntent().getStringExtra("pnr");

		pdrDataDetails = new PegelDataResultReciever(new Handler());
		pdrDataDetails.setReceiver(new DataDetailHandler());

		setProgressBarIndeterminateVisibility(true);
		this.pegelDataProvider = PegelDataProvider.getInstance((PegelApplication) getApplication());		
	}

	@Override
	protected void onStart() {	
		super.onStart();
		this.pegelDataProvider.showData(pnr, null, null, null, null, pdrDataDetails,0 );
	}


	protected void updateDataDetailInUi() {	
		Set<String> keys = data.keySet();
		TableLayout tl = (TableLayout) findViewById(R.id.datatable);
		if(tl.getChildCount() > 1)//clean up the rows
			tl.removeViews(1, tl.getChildCount()-1);
		LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		for (String key : keys) {

			String[] dat = data.getStringArray(key);
			
			View rowView = inflater.inflate(R.layout.table_data_row, null);

			TextView tv = (TextView) rowView.findViewById(R.id.tableTextP);
			tv.setText(dat[0]);

			TextView tv2 = (TextView) rowView.findViewById(R.id.tableTextM);
			String value = dat[1];//(Math.round(Float.parseFloat(dat[3])*100.0) / 100.0)+dat[2];
			tv2.setText(value);

			TextView tv3 = (TextView) rowView.findViewById(R.id.tableTextD);
			tv3.setText(dat[2]);

			tl.addView(rowView);
		}
	}	
	
	
	@Override	
	public void refreshFromOptionsMenu()
	{
		setProgressBarIndeterminateVisibility(true);
		this.pegelDataProvider.refresh(pnr, null, null, null, null, pdrDataDetails, 0);
		this.pegelApp.trackEvent("MoreDetailsActivity", "refresh", "refresh", 1);
	}


	class DataDetailHandler implements PegelDataResultReciever.Receiver
	{
		@Override
		public void onReceiveResult(int resultCode, final Bundle resultData) {
			switch (resultCode) {
			case PegelDataProvider.STATUS_FINISHED:
				data  = resultData;				
				runOnUiThread(mUpdateDatenDetails);
				break;
			default:
				Toast.makeText(getApplicationContext(), getResources().getText(R.string.connection_error), Toast.LENGTH_LONG).show();
				pegelApp.trackEvent("ERROR-Visible", "MoreDataDetail", "Toast", 1);
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
