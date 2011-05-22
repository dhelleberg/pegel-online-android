package org.cirrus.mobi.pegel;

import org.cirrus.mobi.pegel.widget.PegelWidgetProvider.UpdateService;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

public class AbstractPegelDetailsActivity extends Activity {

	private static final int DIALOG_ABOUT = 1;

	protected PegelApplication pa;
	private String app_ver;

	protected String pnr = null;

	protected PegelDataProvider pegelDataProvider;

	protected PegelDetailHelper pegelDetailHelper;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		try {
			this.app_ver = this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			this.app_ver = "unknown";
		}
	}
	
	//Menu Inflater for fixture selection
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.layout.detailmenu, menu);
		return true;
	}

	// This method is called once a menu item is selected
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.m_refresh:
			this.pegelDataProvider.refresh(pnr, pegelDetailHelper.pdrData, pegelDetailHelper.pdrImage, pegelDetailHelper.pdrDataDetails, null, 0);
			this.pa.tracker.trackEvent("PegelDataView", "refresh", "refresh", 1);
			return true;
		case R.id.m_feedback:
			final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
			emailIntent.setType("message/rfc822");
			emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{"dominik.helleberg@googlemail.com"});
			emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Pegel-Online Feedback" );  
			this.pa.tracker.trackEvent("PegelDataView", "feedback", "feedback", 1);
			startActivity(Intent.createChooser(emailIntent, "Email senden..."));
			return true;
		case R.id.m_about:
			showDialog(DIALOG_ABOUT);
			this.pa.tracker.trackEvent("PegelDataView", "about", "about", 1);
			return true;
		default:
			return super.onOptionsItemSelected(item);

		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
	    Dialog dialog;
	    switch(id) {
	    case DIALOG_ABOUT:
	        dialog = createAboutDialog();
	        break;
	    default:
	        dialog = null;
	    }
	    return dialog;
	}
	
	private Dialog createAboutDialog() {
		
		Dialog dialog = new Dialog(this);

		dialog.setContentView(R.layout.about_dialog);
		dialog.setTitle("About Pegel-Online v."+app_ver);

		TextView text = (TextView) dialog.findViewById(R.id.about_d_text);
		text.setText(R.string.about);
		ImageView image = (ImageView) dialog.findViewById(R.id.about_d_logo);
		image.setImageResource(R.drawable.icon);
		
		return dialog;
	}

}
