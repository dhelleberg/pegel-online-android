package org.cirrus.mobi.pegel;

import java.util.ArrayList;
import java.util.List;

import org.cirrus.mobi.pegel.util.IabHelper;
import org.cirrus.mobi.pegel.util.IabHelper.OnConsumeFinishedListener;
import org.cirrus.mobi.pegel.util.IabHelper.OnIabPurchaseFinishedListener;
import org.cirrus.mobi.pegel.util.IabResult;
import org.cirrus.mobi.pegel.util.Inventory;
import org.cirrus.mobi.pegel.util.Purchase;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

public class DonateActivity extends Activity implements OnItemSelectedListener, OnIabPurchaseFinishedListener, OnConsumeFinishedListener {

	private static final String TAG = "DonateActivity";
	protected static final String[] SKUS = {"sku_donate_pegel_1","sku_donate_pegel_2","sku_donate_pegel_3"};
	private static final int REQ_CODE = 100;
	private IabHelper mHelper;
	private PegelApplication pa;
	private Spinner mSpinner;
	private Button mButton;
	private DonateActivity mContext;
	private int selectedItem;
	private Inventory mInventory = null;

	private ArrayAdapter<String> mAdapter;	

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
					for (int i = 0; i < SKUS.length; i++) {
						additionalSkuList.add(SKUS[i]);
					}
					mHelper.queryInventoryAsync(true, additionalSkuList, mQueryFinishedListener);

				}

			}
		});

		this.mButton.setOnClickListener(new  View.OnClickListener(){

			@Override
			public void onClick(View v) {
				//start purchase flow
				mHelper.launchPurchaseFlow(DonateActivity.this, SKUS[selectedItem], REQ_CODE+selectedItem, DonateActivity.this);				
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
			mInventory = inventory;
			// update the UI
			if(mSpinner != null)
			{
				
				String[] donateItems = new String[SKUS.length];
				for (int i = 0; i < SKUS.length; i++) {
					donateItems[i] = String.format(mContext.getString(R.string.donate_1), inventory.getSkuDetails(SKUS[i]).getPrice()); 
				}
				mAdapter = new ArrayAdapter<String>(DonateActivity.this, android.R.layout.simple_spinner_item, donateItems);
				mSpinner.setAdapter(mAdapter); 
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

	@Override
	public void onIabPurchaseFinished(IabResult result, Purchase info) {
		if (result.isFailure()) {
			Log.d(TAG, "Error purchasing: " + result);
			return;
		}      
		else
		{
			//consume purchase
			mHelper.consumeAsync(mInventory.getPurchase(info.getSku()),  this);
		}


	}

	@Override
	public void onConsumeFinished(Purchase purchase, IabResult result) {
		//forward to thanks
		Intent i = new Intent(this, DonateThanksActivity.class);
		startActivity(i);
	}

}
