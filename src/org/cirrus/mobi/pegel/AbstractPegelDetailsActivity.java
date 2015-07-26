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


import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class AbstractPegelDetailsActivity extends AppCompatActivity {
	protected static final int DIALOG_TIP = 2;


	protected PegelApplication pegelApp;
	protected String app_ver;

	protected String pnr = null;

	protected PegelDataProvider pegelDataProvider;

	protected PegelDetailHelper pegelDetailHelper;

	protected boolean firstRunThisVersion = false;

	protected static final String PREFS_NAME_RUN = "prefs_run";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		try {
			this.app_ver = this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName;

			SharedPreferences settings = getSharedPreferences(PREFS_NAME_RUN, Context.MODE_WORLD_WRITEABLE);
			if(settings.contains("run_"+app_ver))
				firstRunThisVersion = false;
			else
				firstRunThisVersion = true;

		} catch (NameNotFoundException e) {
			this.app_ver = "unknown";
		}

	}

	private Dialog createTipHomeDialog() {

		final Dialog dialog = new Dialog(this);

		dialog.setContentView(R.layout.tip_home_dialog);
		dialog.setTitle(getResources().getText(R.string.dialog_home_tip_header));

		Button okButton = (Button) dialog.findViewById(R.id.tip_home_button_ok);
		okButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});

		return dialog;
	}

	
	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog;
		switch(id) {
		case DIALOG_TIP:
			dialog = createTipHomeDialog();
			break;	        
		default:
			dialog = null;
		}
		return dialog;
	}

	protected void refreshFromOptionsMenu()
	{
		this.pegelApp.trackEvent("PegelDataView", "refresh", "refresh", 1);

	}
}
