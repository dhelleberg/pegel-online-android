package org.cirrus.mobi.pegel;

import java.util.ArrayList;
import java.util.List;

import org.cirrus.mobi.pegel.util.IabHelper;
import org.cirrus.mobi.pegel.util.IabResult;
import org.cirrus.mobi.pegel.util.Inventory;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

public class DonateActivity extends Activity implements OnItemSelectedListener {

	private static final String TAG = "DonateActivity";
	protected static final String SKU_DONATE_PEGEL_1 = "sku_donate_pegel_1";
	protected static final String SKU_DONATE_PEGEL_2 = "sku_donate_pegel_2";
	protected static final String SKU_DONATE_PEGEL_3 = "sku_donate_pegel_3";
	private IabHelper mHelper;
	private PegelApplication pa;
	private Spinner mSpinner;
	private Button mButton;
	private DonateActivity mContext;
	private int selectedItem;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.donate);

		this.mSpinner = (Spinner) findViewById(R.id.donate_option_spinner);
		mSpinner.setOnItemSelectedListener(this);

		this.mButton = (Button) findViewById(R.id.donate_button);

		pa = (PegelApplication) getApplication();

		mContext = this;

		mHelper = new IabHelper(this, getString(R.string.lkey));
		mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
			public void onIabSetupFinished(IabResult result) {
				if (!result.isSuccess()) {
					// Oh noes, there was a problem.
					Log.d(TAG, "Problem setting up In-app Billing: " + result);
					pa.trackEvent("Donate-Error", "Problem setting up Billing", result.toString(), 1);
				}
				else
				{
					//query items
					List<String> additionalSkuList = new ArrayList<String>();
					additionalSkuList.add(SKU_DONATE_PEGEL_1);
					additionalSkuList.add(SKU_DONATE_PEGEL_2);
					additionalSkuList.add(SKU_DONATE_PEGEL_3);
					mHelper.queryInventoryAsync(true, additionalSkuList, mQueryFinishedListener);

				}

			}
		});
	}


	IabHelper.QueryInventoryFinishedListener 
	mQueryFinishedListener = new IabHelper.QueryInventoryFinishedListener() {
		public void onQueryInventoryFinished(IabResult result, Inventory inventory)   
		{
			if (result.isFailure()) {
				// handle error
				return;
			}
			// update the UI
			String price_1 = inventory.getSkuDetails(SKU_DONATE_PEGEL_1).getPrice();
			String price_2 = inventory.getSkuDetails(SKU_DONATE_PEGEL_2).getPrice();
			String price_3 = inventory.getSkuDetails(SKU_DONATE_PEGEL_3).getPrice();
			if(mSpinner != null)
			{
				String[] donateItems = new String[3];
				donateItems[0] = String.format(mContext.getString(R.string.donate_1), price_1);
				donateItems[1] = String.format(mContext.getString(R.string.donate_2), price_2);
				donateItems[2] = String.format(mContext.getString(R.string.donate_3), price_3);
				ArrayAdapter<String> adapter = new ArrayAdapter<String>(DonateActivity.this, android.R.layout.simple_spinner_item, donateItems);
				mSpinner.setAdapter(adapter); 
				mButton.setEnabled(true);
			}
		}
	};
	

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mHelper != null) 
			mHelper.dispose();
		mHelper = null;
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int pos,
			long id) {
		this.selectedItem = pos;
		Log.d(TAG, "Select: "+selectedItem);
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		// TODO Auto-generated method stub

	}

}
