package org.cirrus.mobi.pegel.md;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PersistableBundle;
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

import org.cirrus.mobi.pegel.AbstractPegelDetailsActivity;
import org.cirrus.mobi.pegel.PegelApplication;
import org.cirrus.mobi.pegel.R;

/**
 * Created by dhelleberg on 25/07/15.
 */
public class PegelDataActivity extends AbstractPegelDetailsActivity {


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
        public int getCount() {
            return TAB_COUNT;
        }
    }
}
