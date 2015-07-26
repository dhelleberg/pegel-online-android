package org.cirrus.mobi.pegel.md;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import org.cirrus.mobi.pegel.AbstractPegelDetailsActivity;
import org.cirrus.mobi.pegel.DonateActivity;
import org.cirrus.mobi.pegel.PegelApplication;
import org.cirrus.mobi.pegel.R;
import org.cirrus.mobi.pegel.StartupActivity;

/**
 * Created by dhelleberg on 25/07/15.
 */
public class PegelDataActivity extends AbstractPegelDetailsActivity {
    private static final int DIALOG_ABOUT = 1;
    private static final int DIALOG_NOT_FOUND = 2;


    private ViewPager mPager;
    private PagerAdapter mPagerAdapter;
    private String mpoint;
    private String river;
    private TabLayout mtabLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pegel_data_viewpager);

        mPagerAdapter = new PegelDetailsTabsAdapter(getSupportFragmentManager());
        mPager = (ViewPager) findViewById(R.id.pegel_data_pager);
        mPager.setAdapter(mPagerAdapter);
        mtabLayout = (TabLayout) findViewById(R.id.tab_layout);
        mtabLayout.setupWithViewPager(mPager);
        mPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mtabLayout));

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        pegelApp = (PegelApplication) getApplication();

        this.pnr = getIntent().getStringExtra("pnr");
        this.river = getIntent().getStringExtra("river");
        this.mpoint = getIntent().getStringExtra("mpoint");

        if(firstRunThisVersion)
        {
            showDialog(DIALOG_TIP);
            this.pegelApp.trackEvent("PegelDataView", "firstrundialog", "show", 1);
            SharedPreferences settings = getSharedPreferences(PREFS_NAME_RUN, Context.MODE_WORLD_WRITEABLE);
            SharedPreferences.Editor edit = settings.edit();
            edit.putBoolean("run_"+app_ver, true);
            edit.commit();
        }
    }

    //Menu Inflater for fixture selection
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.detailmenu, menu);
        return true;
    }

    // This method is called once a menu item is selected
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.m_refresh:
                refreshFromOptionsMenu();
                return true;
            case R.id.m_feedback:
                final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
                emailIntent.setType("message/rfc822");
                emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{"dominik.helleberg@googlemail.com"});
                emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Pegel-Online Feedback" );
                this.pegelApp.trackEvent("PegelDataView", "feedback", "feedback", 1);
                startActivity(Intent.createChooser(emailIntent, "Email senden..."));
                return true;
            case R.id.m_about:
                showDialog(DIALOG_ABOUT);
                this.pegelApp.trackEvent("PegelDataView", "about", "about", 1);
                return true;
            case R.id.m_donate:
                Intent i = new Intent(this, DonateActivity.class);
                this.pegelApp.trackEvent("PegelDataView", "donate", "donate", 1);
                startActivity(i);
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
            case DIALOG_NOT_FOUND:
                dialog = createNotFoundDialog();
                break;
            default:
                dialog = null;
        }
        return dialog;
    }

    private Dialog createNotFoundDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.pegelNotFound)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //delete preferences
                        SharedPreferences settings = getSharedPreferences("prefs", Context.MODE_WORLD_WRITEABLE);
                        SharedPreferences.Editor edit = settings.edit();
                        edit.clear();
                        edit.commit();
                        //go back
                        Intent i = new Intent(PegelDataActivity.this, StartupActivity.class);
                        startActivity(i);
                    }
                });

        return builder.create();
    }
    private Dialog createAboutDialog() {

        final Dialog dialog = new Dialog(this);

        dialog.setContentView(R.layout.about_dialog);
        dialog.setTitle("About Pegel-Online v."+app_ver);

        TextView text = (TextView) dialog.findViewById(R.id.about_d_text);
        text.setText(R.string.about);
        ImageView image = (ImageView) dialog.findViewById(R.id.about_d_logo);
        image.setImageResource(R.drawable.icon);

        return dialog;
    }


    class PegelDetailsTabsAdapter extends FragmentPagerAdapter {

        private static final int TAB_COUNT = 4;

        public PegelDetailsTabsAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return PegelDataFragment.getNewInstance(pnr, river, mpoint);
                case 1:
                    return PegelDataFragment.getNewInstance(pnr, river, mpoint);
                case 2:
                    return PegelDataFragment.getNewInstance(pnr, river, mpoint);
                case 3:
                    return PegelDataFragment.getNewInstance(pnr, river, mpoint);
            }
            return null;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.tab1);
                case 1:
                    return getString(R.string.tab2);
                case 2:
                    return getString(R.string.tab3);
                case 3:
                    return getString(R.string.tab4);
            }
            return null;
        }

        @Override
        public int getCount() {
            return TAB_COUNT;
        }
    }
}
