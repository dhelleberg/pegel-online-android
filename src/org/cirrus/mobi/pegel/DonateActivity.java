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
import android.app.ProgressDialog;
import android.content.Intent;
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
	protected static final String[] SKUS = {"sku_donate_pegel_1","sku_donate_pegel_2","sku_donate_pegel_3"};
	private static final int REQ_CODE = 100;
	private IabHelper mHelper;
	private PegelApplication pa;
	private Spinner mSpinner;
	private Button mButton;
	private DonateActivity mContext;
	private int selectedItem;
	private Inventory mInventory = null;

	ProgressDialog progress;
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
					progress = ProgressDialog.show(DonateActivity.this, getString(R.string.donate_progress_dialog_title),
						    getString(R.string.donate_progress_dialog_message), true);
				}

			}
		});

		this.mButton.setOnClickListener(new  View.OnClickListener(){

			@Override
			public void onClick(View v) {
				//start purchase flow
				mHelper.launchPurchaseFlow(DonateActivity.this, SKUS[selectedItem], REQ_CODE+selectedItem, purchaseListener, "");
				pa.trackEvent("Donate-Start", "Start Purchase", SKUS[selectedItem], 1);
			}

		});

	}


	IabHelper.QueryInventoryFinishedListener 
	mQueryFinishedListener = new IabHelper.QueryInventoryFinishedListener() {

		public void onQueryInventoryFinished(IabResult result, Inventory inventory)   
		{
			progress.dismiss();
			if (result.isFailure()) {
				// handle error
				pa.trackEvent("Donate-Error", "Could not query inventor", result.getMessage(), 1);
				return;
			}
			mInventory = inventory;
			// update the UI
			if(mSpinner != null)
			{

				String[] donateItems = new String[SKUS.length];
				for (int i = 0; i < SKUS.length; i++) {

					donateItems[i] = inventory.getSkuDetails(SKUS[i]).getTitle()+" "+inventory.getSkuDetails(SKUS[i]).getPrice(); 
				}
				mAdapter = new ArrayAdapter<String>(DonateActivity.this, R.layout.donate_item, donateItems);
				mSpinner.setAdapter(mAdapter); 
				mButton.setEnabled(true);
			}
			//do we have uncosumed purchases? then: CONSUME!
			for (int i = 0; i < SKUS.length; i++) {
				Purchase purchase = inventory.getPurchase(SKUS[i]);
				if(purchase != null)
				{
					Log.v(TAG, "Unconsumed purchase pending! Start consumption "+purchase);
					mHelper.consumeAsync(purchase, silentConsumer);
				}

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
		Log.i(TAG, "Select: "+selectedItem);

	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		// TODO Auto-generated method stub

	}

	OnIabPurchaseFinishedListener purchaseListener = new OnIabPurchaseFinishedListener() {

		@Override
		public void onIabPurchaseFinished(IabResult result, Purchase info) {
			// TODO Auto-generated method stub
			Log.e(TAG, "Purchased! " + result+ "info: "+info);
			pa.trackEvent("Donate-Purchase", "Purchase Done", SKUS[selectedItem]+" "+info, 1);
			if (result.isFailure()) {
				Log.e(TAG, "Error purchasing: " + result);
				pa.trackEvent("Purchase-ERROR", "Purchase ERROR"+result.getMessage(), SKUS[selectedItem]+" "+info, 1);
				return;
			}      
			else
			{
				//consume purchase
				Log.e(TAG, "now consuming purchase...");
				mHelper.consumeAsync(mInventory.getPurchase(info.getSku()),  consumeListener);
			}

		}
	};

	OnConsumeFinishedListener consumeListener = new OnConsumeFinishedListener() {

		@Override
		public void onConsumeFinished(Purchase purchase, IabResult result) {
			//forward to thanks
			Log.e(TAG, "consume done... forward to Thanks Activity....");
			Intent i = new Intent(mContext, DonateThanksActivity.class);
			startActivity(i);
		}


	};

	OnConsumeFinishedListener silentConsumer = new OnConsumeFinishedListener() {

		@Override
		public void onConsumeFinished(Purchase purchase, IabResult result) {
			Log.v(TAG, "Consumed... silent!");
			Log.e(TAG, "SILENCE!");
		}
	};

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// Pass on the activity result to the helper for handling
		if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
			// not handled, so handle it ourselves (here's where you'd
			// perform any handling of activity results not related to in-app
			// billing...
			super.onActivityResult(requestCode, resultCode, data);
		}
		else {
			Log.d(TAG, "onActivityResult handled by IABUtil.");
		}

	}
}
