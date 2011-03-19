package org.cirrus.mobi.pegel;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class StartupActivity extends Activity {
	
	private static final int HONEYCOMB = 11;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		
		Intent i = new Intent();
		if(android.os.Build.VERSION.SDK_INT >= HONEYCOMB)
			i.setClass(getApplicationContext(),PegelFragmentsActivity.class);
		else
			i.setClass(getApplicationContext(), SelectRiver.class);

		startActivity(i);		
		
	}

}
